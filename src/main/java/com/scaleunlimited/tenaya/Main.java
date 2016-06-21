package com.scaleunlimited.tenaya;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.scaleunlimited.tenaya.data.FileSampleReader.FileFormat;
import com.scaleunlimited.tenaya.data.Signature;
import com.scaleunlimited.tenaya.data.ChunkedCountMinSketch;
import com.scaleunlimited.tenaya.data.CountMinSketch;
import com.scaleunlimited.tenaya.data.EncodedKmerGenerator;
import com.scaleunlimited.tenaya.data.FileSampleReader;
import com.scaleunlimited.tenaya.data.MurmurHash3;

public class Main {

	public static void main(String[] args) {
		try {
			loadIntoCountingThreaded(args[0], args[1]);
		} catch (Throwable t) {
			System.err.println("Tool failed: " + t.getMessage());
			t.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void loadIntoCounting(String sourceFile, String destFile) throws IOException {
		File file = new File(sourceFile);
		File dest = new File(destFile);
		int ksize = 20;
		FileSampleReader reader = new FileSampleReader(file, FileFormat.FASTQ);
		EncodedKmerGenerator generator = new EncodedKmerGenerator(ksize, reader);
		CountMinSketch sketch = new CountMinSketch(4, 400000000);
		Signature sig = new Signature(1000);
		long start = System.currentTimeMillis();
		long i = 0;
		while (generator.hasNext()) {
			if (i % 10000000 == 0) {
				System.out.println(sketch.getOccupancy() + "\t" + i + "\t" + (System.currentTimeMillis() - start) + "ms");
			}
			long encodedKmer = generator.next();
			sketch.addKmer(encodedKmer, ksize);
			if (sketch.countKmer(encodedKmer, ksize) == 1) {
				sig.add(MurmurHash3.fmix64(encodedKmer));
			}
			i++;
		}

		reader.close();
		sketch.writeToFile(dest);

		long diff = System.currentTimeMillis() - start;
		System.out.println("occupancy: " + sketch.getOccupancy());
		System.out.println("error rate: " + sketch.getErrorRate());
		System.out.println("took around " + TimeUnit.SECONDS.convert(diff, TimeUnit.MILLISECONDS) + "s");
		
		sig.writeToFile(new File(dest.toPath().toString() + ".sig"));
	}
	
	public static void loadIntoCountingThreaded(String sourceFile, String destFile) throws IOException, InterruptedException {
		File source = new File(sourceFile);
		File dest = new File(destFile);
		int ksize = 20;
		
		FileSampleReader reader = new FileSampleReader(source, FileFormat.FASTQ);
		Signature sig = new Signature(10000);
		BlockingQueue<Runnable> linkedBlockingDeque = new LinkedBlockingDeque<Runnable>(800);
		ExecutorService executor = new ThreadPoolExecutor(8, 8, 30,
		    TimeUnit.SECONDS, linkedBlockingDeque,
		    new ThreadPoolExecutor.CallerRunsPolicy());
		ChunkedCountMinSketch sketch = new ChunkedCountMinSketch(4, 400000000, 10000);
		//CountMinSketch sketch = new CountMinSketch(4, 400000000);
		
		long start = System.currentTimeMillis();
		long i = 0;
		
		String line;
		while (true) {
			line = reader.readSequence();
			if (line == null) {
				break;
			}
			KmerProcessor process = new KmerProcessor(ksize, line, sketch, sig);
			while (linkedBlockingDeque.size() == 800) {
				Thread.yield();
			}
			executor.submit(process);
			if (i % 10000000 == 0) {
				System.out.println(sketch.getOccupancy() + "\t" + i + "\t" + (System.currentTimeMillis() - start) + "ms");
			}
			i += line.length();
		}
		
		executor.shutdown();
		while (!executor.isTerminated()) {
			Thread.sleep(100);
		}
		
		sketch.writeToFile(dest);

		reader.close();
	
		System.out.println("occupancy: " + sketch.getOccupancy());
		System.out.println("error rate: " + sketch.getErrorRate());
		long diff = System.currentTimeMillis() - start;
		System.out.println("took around " + TimeUnit.SECONDS.convert(diff, TimeUnit.MILLISECONDS) + "s");
		
		sig.writeToFile(new File(dest.toPath().toString() + ".sig"));
	}
		
}
