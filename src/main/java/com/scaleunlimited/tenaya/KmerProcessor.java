package com.scaleunlimited.tenaya;

import com.scaleunlimited.tenaya.data.EncodedKmerGenerator;
import com.scaleunlimited.tenaya.data.Kmer;
import com.scaleunlimited.tenaya.data.KmerCounter;
import com.scaleunlimited.tenaya.data.MurmurHash3;
import com.scaleunlimited.tenaya.data.Signature;
import com.scaleunlimited.tenaya.data.StringSampleReader;

public class KmerProcessor implements Runnable {
	
	private EncodedKmerGenerator generator;
	private Signature sig;
	private KmerCounter counter;
	private int ksize;
	
	public KmerProcessor(int ksize, String sequence, KmerCounter output, Signature signature) {
		this.ksize = ksize;
		sig = signature;
		generator = new EncodedKmerGenerator(ksize, new StringSampleReader(sequence));
		counter = output;
	}

	@Override
	public void run() {
		while (generator.hasNext()) {
			long encodedKmer = generator.next();
			int count = counter.addKmer(encodedKmer, ksize);
			if (count == 1) {
				sig.add(MurmurHash3.hashLong(encodedKmer, 42));
			}
		}
	}

}
