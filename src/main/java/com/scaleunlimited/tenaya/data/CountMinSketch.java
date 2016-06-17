package com.scaleunlimited.tenaya.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class CountMinSketch {
	
	private byte[][] data;
	private int rows, cols;
	private int occupants;
	private long[] lastHashes;
	
	public CountMinSketch(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		this.occupants = 0;
		this.data = new byte[rows][cols];
	}
	
	private int getIndex(long hash) {
		return (int) (hash & 0x07fffffffL) % cols;
	}
	
	public void addKmer(String kmer, int ksize) {
		lastHashes = Kmer.hashMurmur(kmer, rows, ksize, lastHashes);
		add(lastHashes);
	}
	
	public void add(long[] hashes) {
		boolean counted = false;
		for (int i = 0; i < rows; i++) {
			int index = getIndex(hashes[i]);
			byte currentCount = data[i][index];
			if (currentCount == 0 && !counted) {
				counted = true;
				occupants++;
			}
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
	
	public int countKmer(String kmer, int ksize) {
		long[] hashes = Kmer.hashMurmur(kmer, rows, ksize, null);
		return count(hashes);
	}
	
	public double falsePositiveRate() {
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
		
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("Rows", Integer.toString(rows));
		properties.put("Columns", Integer.toString(cols));
		properties.put("Occupancy", Integer.toString(occupants));
		properties.put("FP Rate", Double.toString(falsePositiveRate()));
		FileWriter infoWriter = new FileWriter(new File(file.toPath().toString() + ".info"));
		PrintWriter printWriter = new PrintWriter(infoWriter);
		for (String key : properties.keySet()) {
			String val = properties.get(key);
			printWriter.print(key + ":\t" + val);
		}
		printWriter.close();
	}

}
