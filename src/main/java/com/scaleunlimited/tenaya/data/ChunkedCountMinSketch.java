package com.scaleunlimited.tenaya.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class ChunkedCountMinSketch implements KmerCounter {
	
	private byte[][] data;
	private int chunks;
	private int chunkSize;
	private int rows;
	private int[] rowSizes;
	private int occupants;
	private int size;
	
	private final ThreadLocal<int[][]> indices = new ThreadLocal<int[][]>() {

		@Override
		protected int[][] initialValue() {
			return new int[rows][2];
		}
		
	};
	
	public ChunkedCountMinSketch(int rows, int cols) {
		this(rows, cols, 1);
	}
	
	public ChunkedCountMinSketch(int rows, int cols, int chunks) {
		this.rows = rows;
		this.rowSizes = generatePrimesAround(cols, rows);
		this.size = 0;
		for (int i = 0; i < rows; i++) {
			size += rowSizes[i];
		}
		this.chunks = chunks;
		this.chunkSize = (size / chunks) + 1;
		this.occupants = 0;
		this.data = new byte[chunks][chunkSize];
	}
	
	public static int[] generatePrimesAround(int target, int multiplicity) {
		if (target % 2 == 0) {
			target--;
		}
		int[] primes = new int[multiplicity];
		int count = 0;
		while (count < multiplicity) {
			int upperBound = (int) Math.floor(Math.sqrt(target));
			for (int i = 3; i <= upperBound; i++) {
				if (target % i == 0) {
					break;
				} else if (i == upperBound) {
					primes[count++] = target;
				}
			}
			target -= 2;
		}
		return primes;
	}
	
	public int[][] calculateIndices(long hash) {
		int[][] indices = this.indices.get();
		int prevSum = 0;
		for (int i = 0; i < rows; i++) {
			int bound = (int) (hash & Kmer.UNSIGNED_INT_MASK) % rowSizes[i];
			int rawIndex = bound + prevSum;
			int chunk = rawIndex / chunkSize;
			int chunkIndex = rawIndex % chunkSize;
			indices[i][0] = chunk;
			indices[i][1] = chunkIndex;
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
		int[][] indices = calculateIndices(hash);
		for (int i = 0; i < rows; i++) {
			int chunk = indices[i][0];
			int chunkIndex = indices[i][1];
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
	
	public synchronized int count(long hash) {
		int count = Integer.MAX_VALUE;
		int[][] indices = calculateIndices(hash);
		for (int i = 0; i < rows; i++) {
			int chunk = indices[i][0];
			int chunkIndex = indices[i][1];
			byte currentCount = data[chunk][chunkIndex];
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
		properties.put("Size", Integer.toString(size));
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

	@Override
	public synchronized void reset() {
		for (int i = 0; i < chunks; i++) {
			for (int j = 0; j < chunkSize; j++) {
				data[i][j] = 0;
			
			}
		}
		occupants = 0;
	}

}
