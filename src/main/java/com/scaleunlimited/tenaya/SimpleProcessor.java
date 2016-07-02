package com.scaleunlimited.tenaya;

import com.scaleunlimited.tenaya.data.EncodedKmerGenerator;
import com.scaleunlimited.tenaya.data.KmerCounter;
import com.scaleunlimited.tenaya.data.MurmurHash3;
import com.scaleunlimited.tenaya.data.Signature;
import com.scaleunlimited.tenaya.sample.StringSample;

public class SimpleProcessor implements Runnable {
	
	private EncodedKmerGenerator generator;
	private Signature sig;
	private KmerCounter counter;
	private int ksize;
	private int cutoff;
	
	public SimpleProcessor(int ksize, String sequence, KmerCounter output, Signature signature, int cutoff) {
		this.ksize = ksize;
		sig = signature;
		generator = new EncodedKmerGenerator(ksize, new StringSample(sequence));
		counter = output;
		this.cutoff = cutoff;
	}

	@Override
	public void run() {
		while (generator.hasNext()) {
			long encodedKmer = generator.next();
			int count = counter.addKmer(encodedKmer, ksize);
			if (count >= cutoff) {
				sig.add(MurmurHash3.hashLong(encodedKmer, 42));
			}
		}
	}

}
