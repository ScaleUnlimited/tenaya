package com.scaleunlimited.tenaya.data;

import java.util.Iterator;

public class KmerGenerator implements Iterator<String> {
	
	private SampleReader reader;
	private boolean more;
	private String currentSequence;
	private int ksize;
	private int currentIndex;
	private int len;
	
	public KmerGenerator(int ksize, SampleReader sampleReader) {
		reader = sampleReader;
		this.ksize = ksize;
		
		more = true;
		currentSequence = sampleReader.readSequence();
		currentIndex = 0;
		len = currentSequence.length();
	}

	@Override
	public boolean hasNext() {
		return more;
	}

	@Override
	public String next() {
		String kmer = currentSequence.substring(currentIndex++, currentIndex + ksize - 1);
		if ((currentIndex + ksize) > len) {
			currentSequence = reader.readSequence();
			if (currentSequence == null) {
				more = false;
			} else {
				currentIndex = 0;
				len = currentSequence.length();
			}
		}
		return kmer;
	}
	
	public void reset() {
		reader.reset();
		more = true;
		currentSequence = reader.readSequence();
		currentIndex = 0;
		len = currentSequence.length();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("KmerGenerator doesn't support remove");
		
	}
	
}
