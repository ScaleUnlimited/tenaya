package com.scaleunlimited.tenaya.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class ChunkedCountMinSketch implements KmerCounter {
	
	public static final long UNSIGNED_INT_MASK = 0x07fffffffL;
	
	private byte[][] data;
	private int chunks;
	private int chunkSize;
	private int rows, cols;
	private int occupants;
	
	public ChunkedCountMinSketch(int rows, int cols) {
		this(rows, cols, 1);
	}
	
	public ChunkedCountMinSketch(int rows, int cols, int chunks) {
		this.rows = rows;
		this.cols = cols;
		this.chunks = chunks;
		this.chunkSize = rows * cols / chunks;
		this.occupants = 0;
		this.data = new byte[chunks][chunkSize];
	}
	
	public int addKmer(String kmer, int ksize) {
		return addKmer(Kmer.encode(kmer, ksize), ksize);
	}
	
	public int addKmer(long kmer, int ksize) {
		return add(Kmer.hashMurmur(kmer, rows, ksize, null));
	}
	
	public int add(long[] hashes) {
		int count = Integer.MAX_VALUE;
		for (int i = 0; i < rows; i++) {
			int index = (int) (hashes[i] & UNSIGNED_INT_MASK) % cols;
			int calcIndex = i * cols + index;
			int chunk = calcIndex / chunkSize;
			int chunkIndex = calcIndex % chunkSize;
			byte currentCount;
			synchronized(data[chunk]) {
				currentCount = data[chunk][chunkIndex];
				if (currentCount != Byte.MAX_VALUE) {
					data[chunk][chunkIndex] = (byte) (currentCount + 1);
				}
			}
			if (currentCount < count) {
				count = currentCount;
			}
		}
		if (count == 0) {
			occupants++;
		}
		return count;
	}
	
	public int count(long[] hashes) {
		int count = Integer.MAX_VALUE;
		for (int i = 0; i < rows; i++) {
			int index = (int) (hashes[i] & UNSIGNED_INT_MASK) % cols;
			int calcIndex = i * cols + index;
			int chunk = calcIndex / chunkSize;
			int chunkIndex = calcIndex % chunkSize;
			byte currentCount = data[chunk][chunkIndex];
			if (currentCount < count) {
				count = currentCount;
			}
		}
		return count;
	}
	
	public int countKmer(long kmer, int ksize) {
		long[] hashes = Kmer.hashMurmur(kmer, rows, ksize, null);
		return count(hashes);
	}
	
	public int countKmer(String kmer, int ksize) {
		return countKmer(Kmer.encode(kmer, ksize), ksize);
	}
	
	public double getErrorRate() {
		return Math.pow(1.0 - Math.pow(1.0 - (1.0 / ((double) cols)), occupants), rows);
	}
	
	public int getOccupancy() {
		return occupants;
	}
	
	public void readFromFile(File file) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		for (int i = 0; i < chunks; i++) {
			inputStream.read(data[i]);
		}
		inputStream.close();
	}
	
	public void writeToFile(File file) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(file);
		for (int i = 0; i < chunks; i++) {
			outputStream.write(data[i]);
		}
		outputStream.close();
		
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("Rows", Integer.toString(rows));
		properties.put("Columns", Integer.toString(cols));
		properties.put("Occupancy", Integer.toString(occupants));
		properties.put("Error Rate", Double.toString(getErrorRate()));
		FileWriter infoWriter = new FileWriter(new File(file.toPath().toString() + ".info"));
		PrintWriter printWriter = new PrintWriter(infoWriter);
		for (String key : properties.keySet()) {
			String val = properties.get(key);
			printWriter.print(key + ":\t" + val + "\n");
		}
		printWriter.close();
	}

}
