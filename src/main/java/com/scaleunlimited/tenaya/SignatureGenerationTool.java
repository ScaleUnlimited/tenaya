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
		return file.toPath().toString().toLowerCase().endsWith(".fasta") ? FileFormat.FASTA : FileFormat.FASTQ;
	}
	
	public static void generateSignatures(SignatureGenerationToolOptions options) throws IOException, InterruptedException {
		File source = options.getInputFile();
		File dest = options.getOutputFile();
		int ksize = options.getKsize();
		int cutoff = options.getCutoff();
		int queueSize = 800;
		
		System.out.println("Generating from " + source.toPath());
		
		FileSampleReader reader = new FileSampleReader(source, getFileFormat(source), options.getBufferSize(), options.getFilter().equals("sra") ? "(SRR[0-9]{6})" : "");
		BlockingQueue<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>(queueSize);
		ChunkedCountMinSketch sketch = new ChunkedCountMinSketch(options.getDepth(), options.getMaxMemory() / options.getDepth(), options.getChunks());
		
		Signature sig;
		String line;
		ExecutorService executor;
		
		Sample sample = reader.readSample();
		while (sample != null) {
			System.out.println("Reading sample " + sample.getIdentifier());
			
			long start = System.currentTimeMillis();
			long i = 0;
			
			sketch.reset();
			
			executor = new ThreadPoolExecutor(8, 8, 30,
			    TimeUnit.SECONDS, linkedBlockingDeque,
			    new ThreadPoolExecutor.CallerRunsPolicy());
			sig = new Signature(ksize, options.getSignatureSize(), cutoff);
			
			line = sample.readSequence();
			
			while (line != null) {
				KmerProcessor process = new KmerProcessor(ksize, line, sketch, sig, cutoff);
				while (linkedBlockingDeque.size() == queueSize) {
					Thread.yield();
				}
				executor.submit(process);
				if (i % 10000000 < line.length()) {
					System.out.println(sketch.getOccupancy() + "\t" + i + "\t" + (System.currentTimeMillis() - start) + "ms");
				}
				i += line.length();
				line = sample.readSequence();
			}
			
			executor.shutdown();
			while (!executor.isTerminated()) {
				Thread.sleep(100);
			}
			
			File sigFile = new File(dest.toPath().toString().replace(".sig", "." + sample.getIdentifier() + ".sig"));
			
			sig.writeToFile(sigFile);
			
			System.out.println("Wrote signature to " + sigFile.toPath());
			
			System.out.println("Occupancy: " + sketch.getOccupancy());
			System.out.println("Error Rate: " + sketch.getErrorRate());
			long diff = System.currentTimeMillis() - start;
			System.out.println("Took around " + TimeUnit.SECONDS.convert(diff, TimeUnit.MILLISECONDS) + "s");
			
			sample = reader.readSample();
		}

		reader.close();
		
		sketch = null;
		System.gc();
	}
		
}
