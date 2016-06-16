package com.scaleunlimited.tenaya.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.scaleunlimited.tenaya.data.MurmurHash3.LongPair;

public class CountMinSketch {
	
	private byte[][] data;
	private int rows, cols;
	private int occupants;
	
	public CountMinSketch(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.occupants = 0;
		this.data = new byte[rows][cols];
	}
	
	public static long[] hashKmer(String kmer, int n) {
		long rep = Kmer.encode(kmer);
		byte[] bytes = new byte[8];
		for (int i = 0; i < 8; i++) {
			bytes[i] = (byte) (rep & 0x0ffL);
			rep >>= 8;
		}
		int seed = 42;
		long[] hashes = new long[n];
		LongPair pair = new LongPair();
		for (int i = 0; i < n; i++) {
			MurmurHash3.murmurhash3_x64_128(bytes, 0, bytes.length, seed, pair);
			hashes[i] = pair.val1;
			seed = (int) hashes[i];
		}
		return hashes;
	}
	
	private int getIndex(long hash) {
		return (int) (hash & 0x07fffffffL) % cols;
	}
	
	public void addKmer(String kmer) {
		long[] hashes = hashKmer(kmer, rows);
		add(hashes);
	}
	
	public void add(long[] hashes) {
		if (count(hashes) == 0) occupants++;
		for (int i = 0; i < rows; i++) {
			int index = getIndex(hashes[i]);
			byte currentCount = data[i][index];
			if (currentCount != Byte.MAX_VALUE) {
				data[i][index] = (byte) (currentCount + 1);
			}
		}
	}
	
	public int count(long[] hashes) {
		int count = Integer.MAX_VALUE;
		for (int i = 0; i < rows; i++) {
			int index = getIndex(hashes[i]);
			byte currentCount = data[i][index];
			if (currentCount < count) {
				count = currentCount;
			}
		}
		return count;
	}
	
	public int countKmer(String kmer) {
		long[] hashes = hashKmer(kmer, rows);
		return count(hashes);
	}
	
	public double falsePositiveRate() {
		System.out.println(cols);
		System.out.println(occupants);
		System.out.println(rows);
		return Math.pow(1.0 - Math.pow(1.0 - (1.0 / ((double) cols)), occupants), rows);
	}
	
	public int getOccupancy() {
		return occupants;
	}
	
	public void dump(File file) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(file);
		for (int i = 0; i < rows; i++) {
			outputStream.write(data[i]);
		}
		outputStream.close();
	}

}
