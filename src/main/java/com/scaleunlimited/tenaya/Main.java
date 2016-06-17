package com.scaleunlimited.tenaya;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.scaleunlimited.tenaya.data.FileSampleReader.FileFormat;
import com.scaleunlimited.tenaya.data.CountMinSketch;
import com.scaleunlimited.tenaya.data.EncodedKmerGenerator;
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
		EncodedKmerGenerator generator = new EncodedKmerGenerator(ksize, reader);
		CountMinSketch sketch = new CountMinSketch(4, 400000000);
		long start = System.nanoTime();
		long i = 0;
		while (generator.hasNext()) {
			if (i % 10000000 == 0) {
				System.out.println(sketch.getOccupancy() + "\t" + i);
			}
			long encodedKmer = generator.next();
			sketch.addKmer(encodedKmer, ksize);
			i++;
		}
		
		System.out.println("occupancy: " + sketch.getOccupancy());
		System.out.println("fp rate: " + sketch.getErrorRate());
		sketch.writeToFile(dump);
		long diff = System.nanoTime() - start;
		System.out.println("took around " + TimeUnit.SECONDS.convert(diff, TimeUnit.NANOSECONDS) + "s");
		reader.close();
	}
	
}
