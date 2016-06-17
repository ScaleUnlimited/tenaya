package com.scaleunlimited.tenaya;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.scaleunlimited.tenaya.data.FileSampleReader.FileFormat;
import com.scaleunlimited.tenaya.data.CountMinSketch;
import com.scaleunlimited.tenaya.data.KmerGenerator;
import com.scaleunlimited.tenaya.data.FileSampleReader;

public class Main {

	public static void main(String[] args) {
		try {
			testSketch(args[0], args[1]);
		} catch (Throwable t) {
			System.err.println("Tool failed: " + t.getMessage());
			t.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void testSketch(String sourceFile, String destFile) throws IOException {
		File file = new File(sourceFile);
		File dump = new File(destFile);
		int ksize = 20;
		FileSampleReader reader = new FileSampleReader(file, FileFormat.FASTQ);
		KmerGenerator generator = new KmerGenerator(ksize, reader);
		CountMinSketch sketch = new CountMinSketch(4, 400000000);
		long start = System.nanoTime();
		long i = 0;
		while (generator.hasNext()) {
			int occupancy = sketch.getOccupancy();
			if (occupancy % 1000000 == 0) {
				System.out.println(occupancy + "\t" + i);
			}
			String kmer = generator.next();
			sketch.addKmer(kmer, ksize);
			i++;
		}
		
		System.out.println("occupancy: " + sketch.getOccupancy());
		System.out.println("fp rate: " + sketch.falsePositiveRate());
		sketch.dump(dump);
		long diff = System.nanoTime() - start;
		System.out.println("took around " + TimeUnit.SECONDS.convert(diff, TimeUnit.NANOSECONDS) + "s");
		reader.close();
	}
	
}
