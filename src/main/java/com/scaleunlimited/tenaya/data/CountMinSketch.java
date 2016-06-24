package com.scaleunlimited.tenaya.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class CountMinSketch implements KmerCounter {
	
	private byte[] data;
	private int rows, cols;
	private int occupants;
	
	public CountMinSketch(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.occupants = 0;
		this.data = new byte[rows * cols];
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
			int index = (int) (hashes[i] & Kmer.UNSIGNED_INT_MASK) % cols;
			int calcIndex = i * cols + index;
			byte currentCount = data[calcIndex];
			data[calcIndex] = (byte) (currentCount + 1);
			if (currentCount < count) {
				count = currentCount;
			}
			if (data[calcIndex] < 0) {
				data[calcIndex] = Byte.MAX_VALUE;
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
			int index = (int) (hashes[i] & Kmer.UNSIGNED_INT_MASK) % cols;
			byte currentCount = data[i * cols + index];
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
		inputStream.read(data);
		inputStream.close();
	}
	
	public void writeToFile(File file) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(file);
		outputStream.write(data);
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
