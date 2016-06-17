package com.scaleunlimited.tenaya.data;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.scaleunlimited.tenaya.data.MurmurHash3.LongPair;

public class MurmurHash3Test {

	@Test
	public void testLongHash() {
		Random rng = new Random();
		int n = 10000;
		int seed = 42;
		for (int i = 0; i < n; i++) {
			long val = rng.nextLong();
			long newHash = MurmurHash3.hashLong(val, seed);
			byte[] bytes = new byte[8];
			for (int j = 0; j < 8; j++) {
				bytes[j] = (byte) (val & 0x0ffL);
				val >>= 8;
			}
			LongPair pair = new LongPair();
			MurmurHash3.murmurhash3_x64_128(bytes, 0, 8, seed, pair);
			long originalHash = pair.val1;
			assertEquals("The two hashes are supposed to be equivalent", newHash, originalHash);
		}
		
	}
	
}
