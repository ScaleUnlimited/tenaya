package com.scaleunlimited.tenaya.data;

import java.util.Iterator;

public class EncodedKmerGenerator implements Iterator<Long> {

	private SampleReader reader;
	private boolean more;
	private String currentSequence;
	private int ksize;
	private int currentIndex;
	private int len;
	private long f, r;
	
	public EncodedKmerGenerator(int ksize, SampleReader sampleReader) {
		reader = sampleReader;
		this.ksize = ksize;
		
		getNewSequence();
	}
	
	private void getNewSequence() {
		currentSequence = reader.readSequence();
		more = (currentSequence != null);
		if (more) {
			len = currentSequence.length();
			prepareNewSequence();
		}
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
	
	// TODO add a variable length mask corresponding to the ksize
	private void updateChar(char currentChar) {
		f |= Kmer.repr(currentChar);
		r |= (Kmer.comp(currentChar) << (ksize * 2 - 2));
	}
	
	private void shift() {
		f <<= 2;
		f &= 0x0ffffffffffL;
		r >>>= 2;
	}

	@Override
	public boolean hasNext() {
		return more;
	}

	@Override
	public Long next() {
		char currentChar = currentSequence.charAt(currentIndex++);
		shift();
		updateChar(currentChar);
		if (currentIndex == len) {
			getNewSequence();
		}
		return Kmer.unify(f, r);
	}

}
