package com.scaleunlimited.tenaya.data;

import static org.junit.Assert.*;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.scaleunlimited.tenaya.data.MurmurHash3.LongPair;

public class CountMinSketchTest {

	@Test
	public void testSketch() {
		double acceptableErrorDiff = 0.3;
		int rows = 6;
		int width = 400000;
		int unique = 300000;
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
				errors++;
			}
		}
		double actualErrorRate = ((double) errors) / ((double) unique);
		double estimatedErrorRate = sketch.getErrorRate();
		double errorDiff = (estimatedErrorRate - actualErrorRate) / actualErrorRate;
		assertTrue("Actual and estimated error rates must differ by less than " + acceptableErrorDiff + "%", Math.abs(errorDiff) <= acceptableErrorDiff);
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
	
	@Test
	public void testSketchThreaded() throws InterruptedException {
		final CountMinSketch sketch = new CountMinSketch(4, 1000000);
		final AtomicBoolean ready = new AtomicBoolean(false);
		final AtomicInteger counter = new AtomicInteger();
		Random rng = new Random();
		final long kmer = rng.nextLong();
		for (int i = 0; i < 100; i++) {
			Thread t = new Thread() {
				@Override
				public void run() {
					while (!ready.get()) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					for (int i = 0; i < 1000; i++) {
						sketch.addKmer(kmer, 32);
					}
					counter.incrementAndGet();
				}
			};
			t.start();
		}
		
		ready.set(true);
		
		while (counter.get() < 100) {
			Thread.sleep(1);
		}
		
		assertEquals("Count-Min Sketch counter should reset to max value after overflowing", Byte.MAX_VALUE, sketch.countKmer(kmer, 32));
	}
	
	@Test
	public void testRandomThreaded() throws InterruptedException {
		final int NUM_THREADS = 8;
		final int NUM_KMERS = 100000;
		final int MAX_COUNT = 5;
		final CountMinSketch sketch = new CountMinSketch(4, 1000000);
		final AtomicBoolean ready = new AtomicBoolean(false);
		final AtomicInteger threadCounter = new AtomicInteger();
		long seed = System.nanoTime();
		System.out.println("seed: " + seed);
		Random rng = new Random(seed);
		final long[][] counts = new long[NUM_KMERS][2];
		for (int i = 0; i < NUM_KMERS; i++) {
			counts[i][0] = rng.nextLong();
			counts[i][1] = (rng.nextLong() & 0x07fffffff) % MAX_COUNT;
		}
		
		for (int i = 0; i < NUM_THREADS; i++) {
			Thread t = new Thread() {
				@Override
				public void run() {
					while (!ready.get()) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					for (int i = 0; i < MAX_COUNT; i++) {
						for (int j = 0; j < NUM_KMERS; j++) {
							if (counts[j][1] > i) {
								sketch.addKmer(counts[j][0], 32);
							}
						}
					}
					threadCounter.incrementAndGet();
				}
			};
			t.start();
		}
		
		ready.set(true);
		
		while (threadCounter.get() != NUM_THREADS) {
			Thread.sleep(1);
		}
		
		int wrong = 0;
		double sumDiff = 0.0;
		for (int i = 0; i < NUM_KMERS; i++) {
			long kmer = counts[i][0];
			long expectedCount = counts[i][1] * NUM_THREADS;
			long actualCount = sketch.countKmer(kmer, 32);
			if (actualCount < expectedCount && actualCount != Byte.MAX_VALUE) {
				sumDiff += (expectedCount - actualCount);
				wrong++;
			}
			//assertTrue("Actual count must be greater than or equal to the expected count", actualCount >= expectedCount | actualCount == Byte.MAX_VALUE);
		}
		System.out.println(wrong);
		System.out.println(sumDiff / wrong);
	}
	
	@Test
	public void testByteIncrement() {
		assertEquals("Byte rolls over to -1 after hitting max", (byte) -128, (byte) (Byte.MAX_VALUE + 1));
	}
	
}
