package com.scaleunlimited.tenaya.data;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

public class SimilaritySimulation {
	
	@Test
	public void simulateSequenceSampling() {
		int sequenceSize = 20000;
		int sampleSize = sequenceSize / 10;
		int numSamples = 100000;
		int[] indexes = new int[sequenceSize];
		for (int i = 0; i < sequenceSize; i++) {
			indexes[i] = i;
		}
		int[] histogram = new int[101];
		for (int i = 0; i < numSamples; i++) {
			shuffleArray(indexes);
			int[] a = Arrays.copyOfRange(indexes, 0, sampleSize);
			shuffleArray(indexes);
			int[] b = Arrays.copyOfRange(indexes, 0, sampleSize);
			double percentSim = overlap(a, b);
			int roundedSim = (int) Math.round(100.0 * percentSim);
			histogram[roundedSim] += 1;
		}
		for (int i = 0; i < 101; i++) {
			System.out.println(i + "%\t" + histogram[i]);
		}
	}
	
	public double overlap(int[] a, int[] b) {
		Arrays.sort(a);
		Arrays.sort(b);
		int overlaps = 0;
		int i = 0, j = 0;
		while (i != a.length && j != b.length) {
			if (a[i] < b[j]) {
				i++;
			} else if (a[i] > b[j]) {
				j++;
			} else {
				i++;
				j++;
				overlaps++;
			}
		}
		return ((double) overlaps) / ((double) a.length);
	}
	
	// Implementing Fisher-Yates shuffle
	public static void shuffleArray(int[] ar)
	  {
	    // If running on Java 6 or older, use `new Random()` on RHS here
	    Random rnd = ThreadLocalRandom.current();
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      int a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	  }

}
