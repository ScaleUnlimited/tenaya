package com.scaleunlimited.tenaya.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class CountMinSketch implements KmerCounter {
	
	private byte[][] data;
	private int rows;
	private long occupants;
	private int size;
	private int[] rowSizes;
	private int[] indices;
	
	public CountMinSketch(int rows, int cols) {
		this.rows = rows;
		this.occupants = 0;
		this.rowSizes = CountMinSketch.generatePrimesAround(cols, rows);
		this.size = 0;
		this.data = new byte[rows][];
		for (int i = 0; i < rows; i++) {
			size += rowSizes[i];
			data[i] = new byte[rowSizes[i]];
		}
		this.indices = new int[rows];
	}
	
	public int[] calculateIndices(long hash) {
		int prevSum = 0;
		for (int i = 0; i < rows; i++) {
			indices[i] = (int) (hash & Kmer.UNSIGNED_INT_MASK) % rowSizes[i]; 
			prevSum += rowSizes[i];
		}
		return indices;
	}
	
	public int addKmer(String kmer, int ksize) {
		return addKmer(Kmer.encode(kmer, ksize), ksize);
	}
	
	public int addKmer(long kmer, int ksize) {
		return add(Kmer.hashMurmur(kmer, ksize));
	}
	
	public int add(long hash) {
		int count = Integer.MAX_VALUE;
		int[] indices = calculateIndices(hash);
		for (int i = 0; i < rows; i++) {
			int index = indices[i];
			byte currentCount = data[i][index];
			if (currentCount != Byte.MAX_VALUE) {
				data[i][index] = (byte) (currentCount + 1);
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
	
	public int count(long hash) {
		int count = Integer.MAX_VALUE;
		int[] indices = calculateIndices(hash);
		for (int i = 0; i < rows; i++) {
			int index = indices[i];
			byte currentCount = data[i][index];
			if (currentCount < count) {
				count = currentCount;
			}
		}
		return count;
	}
	
	public int countKmer(long kmer, int ksize) {
		return count(Kmer.hashMurmur(kmer, ksize));
	}
	
	public int countKmer(String kmer, int ksize) {
		return countKmer(Kmer.encode(kmer, ksize), ksize);
	}
	
	public double getErrorRate() {
		double product = 1.0;
		for (int i = 0; i < rows; i++) {
			product *= (1.0 - Math.pow(1.0 - 1.0 / rowSizes[i], occupants));
		}
		return product;
	}
	
	public long getOccupancy() {
		return occupants;
	}
	
	public void readFromFile(File file) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		for (int i = 0; i < rows; i++) {
			inputStream.read(data[i]);
		}
		inputStream.close();
	}
	
	public void writeToFile(File file) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(file);
		for (int i = 0; i < rows; i++) {
			outputStream.write(data[i]);
		}
		outputStream.close();
		
		HashMap<String, String> properties = new HashMap<String, String>();
		properties.put("Rows", Integer.toString(rows));
		properties.put("Size", Long.toString(size));
		properties.put("Occupancy", Long.toString(occupants));
		properties.put("Error Rate", Double.toString(getErrorRate()));
		FileWriter infoWriter = new FileWriter(new File(file.toPath().toString() + ".info"));
		PrintWriter printWriter = new PrintWriter(infoWriter);
		for (String key : properties.keySet()) {
			String val = properties.get(key);
			printWriter.print(key + ":\t" + val + "\n");
		}
		printWriter.close();
	}

	@Override
	public void reset() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rowSizes[i]; j++) {
				data[i][j] = 0;
			}
		}
		occupants = 0;
	}

	public static int[] generatePrimesAround(int cols, int multiplicity) {
		if (cols % 2 == 0) {
			cols--;
		}
		int[] primes = new int[multiplicity];
		int count = 0;
		while (count < multiplicity) {
			int upperBound = (int) Math.floor(Math.sqrt(cols));
			for (int i = 3; i <= upperBound; i++) {
				if (cols % i == 0) {
					break;
				} else if (i == upperBound) {
					primes[count++] = cols;
				}
			}
			cols -= 2;
		}
		return primes;
	}

}
