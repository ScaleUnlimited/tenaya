package com.scaleunlimited.tenaya.data;

import java.util.Arrays;
import java.util.Random;

import com.scaleunlimited.tenaya.data.MurmurHash3.LongPair;

public class CountMinSketchTest {

	public static void main(String[] args) {
		int rows = 4;
		int width = 1000000;
		int n = 1000000;
		int range = n / 10;
		long[] values = new long[n];
		int[] seeds = new int[rows];
		Random rng = new Random();
		CountMinSketch sketch = new CountMinSketch(rows, width);
		for (int i = 0; i < rows; i++) {
			seeds[i] = rng.nextInt();
		}
		for (int i = 0; i < n; i++) {
			values[i] = (((long) rng.nextInt()) & 0x00000000ffffffffL) % range;
			System.out.println(values[i]);
			sketch.add(hash(values[i], seeds));
		}
		Arrays.sort(values);
		int fp = 0, index = 0;
		for (int i = 0; i < range; i++) {
			System.out.println(i);
			System.out.println(values[index]);
			int count = 0;
			int sketchCount = sketch.count(hash(i, seeds));
			while (values.length > index && i == values[index]) {
				index++;
				count++;
			}
			if (count != sketchCount) {
				System.out.println("count: got: " + sketchCount + ", expected: " + count);
				fp++;
			}
		}
		double fpRate = ((double) fp) / ((double) n);
		System.out.println("estimated: " + sketch.falsePositiveRate());
		System.out.println("actual: " + fpRate);
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
