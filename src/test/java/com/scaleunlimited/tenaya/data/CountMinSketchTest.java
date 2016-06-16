package com.scaleunlimited.tenaya.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.scaleunlimited.tenaya.data.MurmurHash3.LongPair;

public class CountMinSketchTest {

	public static void main(String[] args) {
		double acceptableErrorDiff = 0.2;
		int rows = 4;
		int width = 10000000;
		int unique = 30000000;
		int range = 100;
		int[][] counts = new int[unique][2];
		int[] seeds = new int[rows];
		Random rng = new Random();
		CountMinSketch sketch = new CountMinSketch(rows, width);
		for (int i = 0; i < rows; i++) {
			seeds[i] = rng.nextInt();
		}
		for (int i = 0; i < unique; i++) {
			int value = rng.nextInt();
			int count = Math.abs(rng.nextInt()) % range;
			counts[i][0] = value;
			counts[i][1] = count;
			long[] hashes = hash(value, seeds);
			for (int j = 0; j < count; j++) {
				sketch.add(hashes);
			}
		}
		int errors = 0;
		for (int i = 0; i < unique; i++) {
			int value = counts[i][0];
			long[] hashes = hash(value, seeds);
			int expectedCount = counts[i][1];
			int sketchCount = sketch.count(hashes);
			if (expectedCount != sketchCount) {
				//System.out.println("expected: " + expectedCount + " but got: " + sketchCount);
				errors++;
			}
		}
		double actualErrorRate = ((double) errors) / ((double) unique);
		double estimatedErrorRate = sketch.falsePositiveRate();
		double errorDiff = (estimatedErrorRate - actualErrorRate) / actualErrorRate;
		System.out.println("estimated: " + estimatedErrorRate);
		System.out.println("actual: " + actualErrorRate);
		System.out.println("percent diff: " + errorDiff);
		System.out.println(Math.abs(errorDiff) <= acceptableErrorDiff ? "SUCCESS" : "FAILURE");
	}
	
	private static long[] hash(long value, int[] seeds) {
		byte[] bytes = new byte[8];
		for (int i = 0; i < 8; i++) {
			bytes[i] = (byte) (value & 0x0ffL);
			value >>= 8;
		}
		LongPair pair = new LongPair();
		long[] hashes = new long[seeds.length];
		for (int i = 0; i < seeds.length; i++) {
			MurmurHash3.murmurhash3_x64_128(bytes, 0, bytes.length, seeds[i], pair);
			hashes[i] = pair.val1;
		}
		return hashes;
	}
	
}
