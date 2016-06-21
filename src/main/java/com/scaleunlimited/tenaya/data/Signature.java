package com.scaleunlimited.tenaya.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class Signature {
	
	private int count;
	private int size;
	private long[] data;
	
	private Signature(int size, long[] data) {
		this.size = size;
		this.count = 0;
		this.data = data;
	}
	
	public Signature(int size) {
		this(size, new long[size]);
	}
	
	public void add(long hash) {
		if (count < size) {
			synchronized(this) {
				for (int i = 0; i < count; i++) {
					if (data[i] == hash) {
						return;
					}
				}
				data[count] = hash;
				if (count == (size - 1)) {
					Arrays.sort(data);
				}
			}
		} else if (data[size-1] > hash) {
			int index = Arrays.binarySearch(data, hash);
			if (index < 0) {
				synchronized(this) {
					int insertion = -(index + 1);
					for (int i = (size - 1); i > insertion; i--) {
						data[i] = data[i - 1];
					}
					data[insertion] = hash;
				}
			}
		}
		count++;
	}
	
	public int getSize() {
		return size;
	}
	
	public long[] get() {
		return data;
	}
	
	public double jaccard(Signature other) {
		long[] otherData = other.get();
		int i = 0, j = 0, matches = 0;
		while (i < size && j < size) {
			if (data[i] < otherData[j]) {
				i++;
			} else if (data[i] > otherData[j]) {
				j++;
			} else {
				i++;
				j++;
				matches++;
			}
		}
		return ((float) matches) / ((float) size);
	}
	
	public void addAll(Signature other) {
		long[] otherData = other.get();
		for (int i = 0; i < other.getSize(); i++) {
			add(otherData[i]);
		}
	}
	
	public Signature combine(Signature other) {
		Signature combined = new Signature(size, data);
		combined.addAll(other);
		return combined;
	}
	
	public void writeToFile(File file) throws IOException {
		FileWriter infoWriter = new FileWriter(file);
		PrintWriter printWriter = new PrintWriter(infoWriter);
		for (int i = 0; i < size; i++) {
			printWriter.format("%08X\n", data[i]);
		}
		printWriter.close();
	}

}
