package com.scaleunlimited.tenaya.data;

public class EncodedKmerGenerator {

	private SampleReader reader;
	private boolean more;
	private String currentSequence;
	private int ksize;
	private int currentIndex;
	private int len;
	private long f, r;
	private long shiftMask;
	private boolean dna;
	
	public EncodedKmerGenerator(int ksize, SampleReader sampleReader) {
		this(ksize, sampleReader, false);
	}
	
	public EncodedKmerGenerator(int ksize, SampleReader sampleReader, boolean dna) {
		reader = sampleReader;
		this.ksize = ksize;
		this.dna = dna;

		shiftMask = ~(0x0ffffffffffffffffL << (ksize * 2));
		
		getNewSequence();
	}
	
	private void getNewSequence() {
		currentSequence = reader.readSequence();
		more = (currentSequence != null && currentSequence.length() != 0);
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
		return more;
	}
	
	public long next() {
		char currentChar = currentSequence.charAt(currentIndex++);
		shift();
		updateChar(currentChar);
		if (currentIndex == len) {
			getNewSequence();
		}
		if (dna) {
			return Kmer.unify(f, r);
		} else {
			return f;
		}
	}

}
