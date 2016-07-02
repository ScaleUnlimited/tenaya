package com.scaleunlimited.tenaya.data;

import com.scaleunlimited.tenaya.sample.Sample;

public class EncodedKmerGenerator {

	private Sample sample;
	private boolean hasMoreKmers;
	private String currentSequence;
	private int ksize;
	private int currentIndex;
	private long f, r;
	private long shiftMask;
	
	public EncodedKmerGenerator(int ksize) {
		this(ksize, null);
	}
	
	public EncodedKmerGenerator(int ksize, Sample sample) {
		this.ksize = ksize;

		shiftMask = ~(0x0ffffffffffffffffL << (ksize * 2));
		
		setSample(sample);
	}
	
	public void setSample(Sample sample) {
		this.sample = sample;
		readNewSequence();
	}
	
	private void readNewSequence() {
		hasMoreKmers = false;
		if (sample == null) {
			return;
		}
		currentSequence = sample.readSequence();
		if (currentSequence == null || currentSequence.length() == 0) {
			return;
		}
		hasMoreKmers = true;
		prepareNewSequence();
	}
	
	private void prepareNewSequence() {
		f = 0;
		r = 0;
		for (currentIndex = 0; currentIndex < (ksize - 1); currentIndex++) {
			char currentChar = currentSequence.charAt(currentIndex);
			shift();
			updateChar(currentChar);
		}
	}
	
	private void updateChar(char currentChar) {
		f |= Kmer.repr(currentChar);
		r |= (Kmer.comp(currentChar) << (ksize * 2 - 2));
	}
	
	private void shift() {
		f <<= 2;
		f &= shiftMask;
		r >>>= 2;
	}

	public boolean hasNext() {
		return hasMoreKmers;
	}
	
	public long next() {
		char currentChar = currentSequence.charAt(currentIndex++);
		shift();
		updateChar(currentChar);
		int len = currentSequence.length();
		if (currentIndex >= len) {
			readNewSequence();
		}
		return Kmer.unify(f, r);
	}

}
