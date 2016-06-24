package com.scaleunlimited.tenaya.data;

import java.util.Iterator;

public class KmerGenerator implements Iterator<String> {
	
	private Sample sample;
	private boolean more;
	private String currentSequence;
	private int ksize;
	private int currentIndex;
	private int len;
	
	public KmerGenerator(int ksize, Sample sample) {
		this.ksize = ksize;
		
		setSample(sample);		
	}
	
	public void setSample(Sample sample) {
		this.sample = sample;
		more = true;
		currentSequence = sample.readSequence();
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
			currentSequence = sample.readSequence();
			if (currentSequence == null) {
				more = false;
			} else {
				currentIndex = 0;
				len = currentSequence.length();
			}
		}
		return kmer;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("KmerGenerator doesn't support remove");
	}
	
}
