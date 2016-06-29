package com.scaleunlimited.tenaya;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.scaleunlimited.tenaya.data.FileSampleReader.FileFormat;
import com.scaleunlimited.tenaya.data.Sample;
import com.scaleunlimited.tenaya.data.Signature;
import com.scaleunlimited.tenaya.data.ChunkedCountMinSketch;
import com.scaleunlimited.tenaya.data.FileSampleReader;

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
			generateSignatures(options);
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
	
	private static FileFormat getFileFormat(File file) {
		return (file.toPath().toString().toLowerCase().indexOf(".fasta") != -1) ? FileFormat.FASTA : FileFormat.FASTQ;
	}
	
	public static void generateSignatures(SignatureGenerationToolOptions options) throws IOException, InterruptedException {
		File source = options.getInputFile();
		File dest = options.getOutputFile();
		int ksize = options.getKsize();
		int cutoff = options.getCutoff();
		
		int threadCount = options.getThreadCount();
		int queueSize = threadCount * 100;
		
		FileFormat format = getFileFormat(source);
		boolean gzip = options.getGzip();
		if (!gzip && source.toPath().toString().endsWith(".gz")) {
			gzip = true;
		}
		
		System.out.println("Generating from " + source.toPath());
		
		FileSampleReader reader = new FileSampleReader(source, format, options.getBufferSize(), options.getFilter().equals("sra") ? "([SE]RR[0-9]{6})" : "", gzip);
		BlockingQueue<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>(queueSize);
		ChunkedCountMinSketch sketch = new ChunkedCountMinSketch(options.getDepth(), options.getMaxMemory() / options.getDepth(), options.getChunks());
		ExecutorService executor = new ThreadPoolExecutor(threadCount, threadCount, 30,
			    TimeUnit.SECONDS, linkedBlockingDeque,
			    new ThreadPoolExecutor.CallerRunsPolicy());
		
		Signature sig;
		String line;
		long start, i, diff;
		long totalTime = 0;
		double maxErrorRate = 0;
		
		Sample sample = reader.readSample();
		while (sample != null) {
			System.out.println("Reading sample " + sample.getIdentifier());
			System.out.println();
			
			System.out.format(HEADER_FORMAT_STRING, "Unique", "Total", "Time");
			
			start = System.currentTimeMillis();
			i = 0;
			
			sketch.reset();
			
			sig = new Signature(ksize, options.getSignatureSize(), cutoff);
			
			line = sample.readSequence();
			
			while (line != null) {
				KmerProcessor process = new KmerProcessor(ksize, line, sketch, sig, cutoff);
				while (linkedBlockingDeque.size() == queueSize) {
					Thread.yield();
				}
				executor.submit(process);
				if (i % 10000000 < line.length()) {
					diff = System.currentTimeMillis() - start;
					System.out.format(LINE_FORMAT_STRING, sketch.getOccupancy(), i, diff);
				}
				i += line.length();
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
		
		executor.shutdown();
		while (!executor.isTerminated()) {
			Thread.sleep(100);
		}
		
		System.out.println("Max Error Rate: " + maxErrorRate);
		System.out.println("Took around " + TimeUnit.SECONDS.convert(totalTime, TimeUnit.MILLISECONDS) + "s");

		reader.close();
		
		sketch = null;
		System.gc();
	}
		
}
