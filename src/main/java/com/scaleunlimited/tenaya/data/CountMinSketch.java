package com.scaleunlimited.tenaya.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class CountMinSketch {
	
	private byte[] data;
	private int rows, cols;
	private int occupants;
	private long[] lastHashes;
	
	public CountMinSketch(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.occupants = 0;
		this.data = new byte[rows * cols];
	}
	
	private int getIndex(long hash) {
		return (int) (hash & 0x07fffffffL) % cols;
	}
	
	public void addKmer(String kmer, int ksize) {
		addKmer(Kmer.encode(kmer, ksize), ksize);
	}
	
	public void addKmer(long kmer, int ksize) {
		lastHashes = Kmer.hashMurmur(kmer, rows, ksize, lastHashes);
		add(lastHashes);
	}
	
	public void add(long[] hashes) {
		boolean counted = false;
		for (int i = 0; i < rows; i++) {
			int index = getIndex(hashes[i]);
			byte currentCount = data[i * cols + index];
			if (currentCount == 0 && !counted) {
				counted = true;
				occupants++;
			}
			if (currentCount != Byte.MAX_VALUE) {
				data[i * cols + index] = (byte) (currentCount + 1);
			}
		}
	}
	
	public int count(long[] hashes) {
		int count = Integer.MAX_VALUE;
		for (int i = 0; i < rows; i++) {
			int index = getIndex(hashes[i]);
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
