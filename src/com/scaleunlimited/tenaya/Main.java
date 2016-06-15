package com.scaleunlimited.tenaya;

import java.io.File;
import java.util.concurrent.TimeUnit;

import com.scaleunlimited.tenaya.data.SampleReader.FileFormat;
import com.scaleunlimited.tenaya.data.CountMinSketch;
import com.scaleunlimited.tenaya.data.KmerGenerator;
import com.scaleunlimited.tenaya.data.SampleReader;

public class Main {

	public static void main(String[] args) {
		testSketch();
	}
	
	public static void testSketch() {
//		Path filePath = FileSystems.getDefault().getPath("fastq/sfg_samples/1_Cold_TESTFILE.fastq");
//		File file = filePath.toFile();
		File file = new File("C:/Users/Ryan/Desktop/Drop/abyssicola.fastq");
		File dump = new File("C:/Users/Ryan/Desktop/Drop/dump");
		SampleReader reader = new SampleReader(file, FileFormat.FASTQ);
		KmerGenerator generator = new KmerGenerator(20, reader);
		CountMinSketch sketch = new CountMinSketch(10, 200000000);
		long start = System.nanoTime();
		while (generator.hasNext()) {
			int occupancy = sketch.getOccupancy();
			if (occupancy % 1000000 == 0) {
				System.out.println(occupancy);
			}
			String kmer = generator.next();
			sketch.addKmer(kmer);
		}
		System.out.println("occupancy: " + sketch.getOccupancy());
		System.out.println("fp rate: " + sketch.falsePositiveRate());
		sketch.dump(dump);
		long diff = System.nanoTime() - start;
		System.out.println("took around " + TimeUnit.SECONDS.convert(diff, TimeUnit.NANOSECONDS) + "s");
		reader.close();
	}
	
}
