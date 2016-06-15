package com.scaleunlimited.tenaya.data;

import java.util.Iterator;

public class KmerIterator implements Iterator<String> {
	
	private String sequence;
	private int ksize;
	private int currentIndex;
	private int len;
	
	public KmerIterator(int ksize, String seq) {
		sequence = seq;
		this.ksize = ksize;
		currentIndex = 0;
		len = seq.length();
	}

	@Override
	public boolean hasNext() {
		return (currentIndex + ksize - 1) < len;
	}

	@Override
	public String next() {
		return sequence.substring(currentIndex++, currentIndex + ksize - 1);
	}

}
