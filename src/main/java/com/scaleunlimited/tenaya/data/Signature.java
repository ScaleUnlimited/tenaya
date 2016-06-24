package com.scaleunlimited.tenaya.data;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class Signature {
	
	private int count;
	private int size;
	private long[] data;
	private int ksize;
	private int cutoff;
	
	private Signature(int ksize, int size,  int cutoff, long[] data) {
		this.cutoff = cutoff;
		this.size = size;
		this.count = 0;
		this.data = data;
		this.ksize = ksize;
	}
	
	public Signature(int ksize, int size, int cutoff) {
		this(ksize, size, cutoff, new long[size]);
	}
	
	public Signature(int size) {
		this(0, size, 0);
	}
	
	public synchronized void add(long hash) {
		if (count < size) {
			for (int i = 0; i < count; i++) {
				if (data[i] == hash) {
					return;
				}
			}
			data[count] = hash;
			if (count == (size - 1)) {
				Arrays.sort(data);
			}
		} else if (data[size-1] > hash) {
			int index = Arrays.binarySearch(data, hash);
			if (index < 0) {
				int insertion = -(index + 1);
				for (int i = (size - 1); i > insertion; i--) {
					data[i] = data[i - 1];
				}
				data[insertion] = hash;
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
		return ((double) matches) / ((double) size);
	}
	
	public void addAll(Signature other) {
		long[] otherData = other.get();
		for (int i = 0; i < other.getSize(); i++) {
			add(otherData[i]);
		}
	}
	
	public Signature combine(Signature other) {
		Signature combined = new Signature(ksize, size, cutoff, data);
		combined.addAll(other);
		return combined;
	}
	
	public void writeToFile(File file) throws IOException {
		Map<String, Object> contents = new HashMap<String, Object>();
		contents.put("version", "0.1");
		Map<String, Object> signature = new HashMap<String, Object>();
		signature.put("ksize", new Integer(ksize));
		signature.put("cutoff", cutoff);
		signature.put("num", new Integer(size));
		signature.put("hashes", data);
		contents.put("signature", signature);
		FileWriter infoWriter = new FileWriter(file);
		Yaml yaml = new Yaml();
		yaml.dump(contents, infoWriter);
	}
	
	@SuppressWarnings("unchecked")
	public static Signature createFromFile(File file) throws IOException {
		Yaml yaml = new Yaml();
		Map<String, Object> contents = (Map<String, Object>) yaml.load(new FileReader(file));
		Map<String, Object> signature = (Map<String, Object>) contents.get("signature");
		int ksize = ((Integer) signature.get("ksize")).intValue();
		int size = ((Integer) signature.get("num")).intValue();
		int cutoff = ((Integer) signature.get("cutoff")).intValue();
		String hashArray = signature.get("hashes").toString();
		String[] tokens = hashArray.substring(1, hashArray.length() - 1).split("\\s*,\\s*");
		long[] hashes = new long[size];
		for (int i = 0; i < size; i++) {
			hashes[i] = Long.parseLong(tokens[i]);
		}
		return new Signature(ksize, size, cutoff, hashes);
	}

}
