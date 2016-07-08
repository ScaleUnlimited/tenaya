package com.scaleunlimited.tenaya;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.scaleunlimited.tenaya.SignatureGenerationToolOptions.GenerationMethod;
import com.scaleunlimited.tenaya.data.Signature;
import com.scaleunlimited.tenaya.metadata.ExperimentMetadata;
import com.scaleunlimited.tenaya.nio.*;
import com.scaleunlimited.tenaya.sample.FileSampleReader;
import com.scaleunlimited.tenaya.sample.Sample;
import com.scaleunlimited.tenaya.sample.FileOptions;
import com.scaleunlimited.tenaya.data.ChunkedCountMinSketch;
import com.scaleunlimited.tenaya.data.CountMinSketch;
import com.scaleunlimited.tenaya.data.Kmer;
import com.scaleunlimited.tenaya.data.KmerCounter;
import com.scaleunlimited.tenaya.data.LongQueue;
import com.scaleunlimited.tenaya.data.MurmurHash3;

public class SignatureGenerationTool {
	
	public static final String HEADER_FORMAT_STRING = "%15s%15s%12s%n";
	public static final String LINE_FORMAT_STRING = "%,15d%,15d%,10dms%n";

	public static void main(String[] args) {
		SignatureGenerationToolOptions options = new SignatureGenerationToolOptions();
		CmdLineParser cmdParser = new CmdLineParser(options);
		
		try {
			cmdParser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			printUsageAndExit(cmdParser);
		}
		try {
			GenerationMethod method = options.getMethod();
			switch (method) {
			case SIMPLE:
				generateSignatures(options);
				break;
			case PARTITION:
				generateSignaturesPartitioned(options);
			}
		} catch (Throwable t) {
			System.err.println("Tool failed: " + t.getMessage());
			t.printStackTrace();
			System.exit(-1);
		}
	}
	
	private static void printUsageAndExit(CmdLineParser parser) {
		parser.printUsage(System.err);
		System.exit(-1);
	}
	
	public static int getPid() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		return Integer.parseInt(name.split("@")[0]);
	}
	
	public static void generateSignaturesPartitioned(SignatureGenerationToolOptions options) throws Exception {
		File[] sources = options.getInputFiles();
		File dest = options.getOutputFile();
		int ksize = options.getKsize();
		int cutoff = options.getCutoff();
		
		String identifierRegex = options.getFilter().equals("sra") ? ExperimentMetadata.SRA_IDENTIFIER_REGEX : "";
		
		int bufferSize = options.getBufferSize();
		int threadCount = options.getThreadCount();
		int queueSize = options.getQueueSize();
		if (queueSize == 0) {
			queueSize = 100;
		}
		
		long maxMemory = options.getMaxMemory();
		int memoryPerThread = (int) (maxMemory / threadCount);
		int depth = options.getDepth();
		int width = memoryPerThread / depth;
		
		boolean displayPid = options.getPid();
		int pid = getPid();
		
		long[] hashesSent = new long[threadCount];
		
		PartitionProcessor[] threads = new PartitionProcessor[threadCount];
		for (int i = 0; i < threadCount; i++) {
			LongQueue queue = new LongQueue(queueSize);
			Signature sig = new Signature(ksize, options.getSignatureSize(), cutoff, "");
			KmerCounter counter = new CountMinSketch(depth, width);
			threads[i] = new PartitionProcessor(queue, sig, counter);
			threads[i].start();
		}
		
		long totalTime = 0;
		
		System.out.println();
		
		System.out.println("Files queued:");
		
		final int ONE_GB = 1024 * 1024 * 1024;
		DecimalFormat sizeFormat = new DecimalFormat("###.0");
		for (int j = 0; j < sources.length; j++) {
			File source = sources[j];
			System.out.println(source.getName() + " (" + sizeFormat.format(((double) source.length()) / ONE_GB) + "GB)");
		}
		
		System.out.println();
		
		for (int j = 0; j < sources.length; j++) {
			File source = sources[j];
			
			System.out.println("Reading from file " + source.getName());
			
			// TODO make this handle compressed and FASTQ files
			NIOSampleReader reader = new NIOSampleReader(source, bufferSize, identifierRegex);
			
			NIOEncoder encoder;
			NIOSample sample;
			while ((sample = reader.readSample()) != null) {
				System.out.println("Reading sample " + sample.getIdentifier());
				
				System.out.println();
				
				System.out.println("Total");
				
				long start = System.currentTimeMillis();
				long kmers = 0;
				
				encoder = new NIOEncoder(ksize, sample);
				while (encoder.hasNext()) {
					long encodedKmer = encoder.next();
					int threadId = (int) ((Kmer.UNSIGNED_INT_MASK & MurmurHash3.fmix64(encodedKmer)) % threadCount);
					LongQueue queue = threads[threadId].getQueue();
					while (queue.isFull()) {
						Thread.sleep(1);
					}
					hashesSent[threadId] += 1;
					queue.add(encodedKmer);
					if (kmers % 10000000 == 0) {
						if (displayPid) System.out.print(pid + ": ");
						System.out.print(kmers + " (" + (System.currentTimeMillis() - start) + " ms)");
						for (int i = 0; i < threadCount; i++) {
							System.out.print("\t" + threads[i].getQueue().size());
						}
						System.out.println();
					}
					kmers++;
				}
			
				boolean allEmpty = false;
				while (!allEmpty) {
					Thread.sleep(100);
					allEmpty = true;
					for (int i = 0; i < threadCount; i++) {
						allEmpty = (threads[i].getQueue().isEmpty() && allEmpty);
					}
				}
				
				double meanError = 0.0;
				long occupancy = 0;
				
				Signature all = new Signature(ksize, options.getSignatureSize(), cutoff, sample.getIdentifier());
				for (int i = 0; i < threadCount; i++) {
					all.addAll(threads[i].getSignature());
				
					CountMinSketch sketch = ((CountMinSketch) threads[i].getKmerCounter());
					meanError += sketch.getErrorRate();
					occupancy += sketch.getOccupancy();
					
					threads[i].reset();
				}
				
				meanError /= threadCount;
				
				File sigFile = new File(dest.toPath().toString().replace("#id", sample.getIdentifier()));
				
				all.writeToFile(sigFile);
				
				System.out.println();
				
				System.out.println("Wrote signature to " + sigFile.toPath());
				
				System.out.println("Mean error rate: " + meanError);
				
				System.out.println("Occupancy: " + occupancy);
				
				long diff = System.currentTimeMillis() - start;
				
				System.out.println("Execution time: " + diff + " ms");
				
				totalTime += diff;
				
				System.out.println();
				
			}
			
			reader.close();
			
		}
		
		for (int k = 0; k < threadCount; k++) {
			threads[k].halt();
		}
		
		System.out.println("Total time: " + totalTime + " ms");
		
		System.out.println("Thread distribution statistics:");
		
		for (int i = 0; i < threadCount; i++) {
			System.out.println(hashesSent[i]);
		}
		
			
	}
	
	public static void generateSignatures(SignatureGenerationToolOptions options) throws Exception {
		File[] sources = options.getInputFiles();
		File dest = options.getOutputFile();
		int ksize = options.getKsize();
		int cutoff = options.getCutoff();
		
		String identifierRegex = options.getFilter().equals("sra") ? ExperimentMetadata.SRA_IDENTIFIER_REGEX : "";
		
		int threadCount = options.getThreadCount();
		int queueSize = options.getQueueSize();
		if (queueSize == 0) {
			queueSize = 100 * threadCount;
		}
		
		BlockingQueue<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>(queueSize);
		ChunkedCountMinSketch sketch = new ChunkedCountMinSketch(options.getDepth(), options.getMaxMemory() / options.getDepth(), options.getChunks());
		ExecutorService executor = new ThreadPoolExecutor(threadCount, threadCount, 30,
			    TimeUnit.SECONDS, linkedBlockingDeque,
			    new ThreadPoolExecutor.CallerRunsPolicy());
		FileSampleReader reader;
		
		long totalTime = 0;
		double maxErrorRate = 0;

		for (File source : sources) {		
			FileOptions fileOptions = FileOptions.inferFromFilename(source);
			
			System.out.println("Generating from " + source.toPath());
			
			reader = new FileSampleReader(source, fileOptions, options.getBufferSize(), identifierRegex);
			
			Signature sig;
			String line;
			long start, j, diff;
			
			Sample sample = reader.readSample();
			while (sample != null) {
				System.out.println("Reading sample " + sample.getIdentifier());
				System.out.println();
				
				System.out.format(HEADER_FORMAT_STRING, "Unique", "Total", "Time");
				
				start = System.currentTimeMillis();
				j = 0;
				
				sketch.reset();
				
				sig = new Signature(ksize, options.getSignatureSize(), cutoff, sample.getIdentifier());
				
				line = sample.readSequence();
				
				while (line != null) {
					SimpleProcessor process = new SimpleProcessor(ksize, line, sketch, sig, cutoff);
					while (linkedBlockingDeque.size() >= queueSize) {
						Thread.yield();
					}
					executor.submit(process);
					if (j % 10000000 < line.length()) {
						diff = System.currentTimeMillis() - start;
						System.out.format(LINE_FORMAT_STRING, sketch.getOccupancy(), j, diff);
					}
					j += line.length();
					line = sample.readSequence();
				}
				
				while (linkedBlockingDeque.size() != 0) {
					Thread.sleep(100);
				}
				
				System.out.println();
				
				File sigFile = new File(dest.toPath().toString().replace("#id", sample.getIdentifier()));
				
				sig.writeToFile(sigFile);
				
				System.out.println("Wrote signature to " + sigFile.toPath());
				
				System.out.println("Occupancy: " + sketch.getOccupancy());
				
				double errorRate = sketch.getErrorRate();
				System.out.println("Error Rate: " + errorRate);
				if (errorRate > maxErrorRate) {
					maxErrorRate = errorRate;
				}
				
				diff = System.currentTimeMillis() - start;
				System.out.println("Took around " + TimeUnit.SECONDS.convert(diff, TimeUnit.MILLISECONDS) + "s");
				totalTime += diff;
				
				sample = reader.readSample();
			}
	
			reader.close();
		}
		
		executor.shutdown();
		while (!executor.isTerminated()) {
			Thread.sleep(100);
		}
		
		System.out.println("Max Error Rate: " + maxErrorRate);
		System.out.println("Took around " + TimeUnit.SECONDS.convert(totalTime, TimeUnit.MILLISECONDS) + "s");
		
		sketch = null;
		System.gc();
	}
		
}
