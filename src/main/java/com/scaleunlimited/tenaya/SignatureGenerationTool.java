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
		
		FileSampleReader reader = new FileSampleReader(source, getFileFormat(source), options.getBufferSize());
		Signature sig = new Signature(ksize, options.getSignatureSize());
		BlockingQueue<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>(400);
		ExecutorService executor = new ThreadPoolExecutor(8, 8, 30,
		    TimeUnit.SECONDS, linkedBlockingDeque,
		    new ThreadPoolExecutor.CallerRunsPolicy());
		ChunkedCountMinSketch sketch = new ChunkedCountMinSketch(options.getDepth(), options.getMaxMemory() / options.getDepth(), options.getChunks(), options.getDna());
		
		int cutoff = options.getCutoff();
		long start = System.currentTimeMillis();
		long i = 0;
		
		String line;
		while (true) {
			line = reader.readSequence();
			if (line == null) {
				break;
			}
			KmerProcessor process = new KmerProcessor(ksize, line, sketch, sig, cutoff);
			while (linkedBlockingDeque.size() == 800) {
				Thread.yield();
			}
			executor.submit(process);
			if (i % 10000000 < line.length()) {
				System.out.println(sketch.getOccupancy() + "\t" + i + "\t" + (System.currentTimeMillis() - start) + "ms");
			}
			i += line.length();
		}
		
		executor.shutdown();
		while (!executor.isTerminated()) {
			Thread.sleep(100);
		}
		
		//sketch.writeToFile(dest);

		reader.close();
	
		System.out.println("occupancy: " + sketch.getOccupancy());
		System.out.println("error rate: " + sketch.getErrorRate());
		long diff = System.currentTimeMillis() - start;
		System.out.println("took around " + TimeUnit.SECONDS.convert(diff, TimeUnit.MILLISECONDS) + "s");
		
		sig.writeToFile(dest);
		
		sketch = null;
		System.gc();
	}
		
}
