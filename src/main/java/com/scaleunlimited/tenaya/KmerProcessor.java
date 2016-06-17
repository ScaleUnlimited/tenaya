package com.scaleunlimited.tenaya;

import com.scaleunlimited.tenaya.data.CountMinSketch;
import com.scaleunlimited.tenaya.data.EncodedKmerGenerator;
import com.scaleunlimited.tenaya.data.StringSampleReader;

public class KmerProcessor implements Runnable {
	
	private EncodedKmerGenerator generator;
	private CountMinSketch sketch;
	private int ksize;
	
	public KmerProcessor(int ksize, String sequence, CountMinSketch output) {
		this.ksize = ksize;
		generator = new EncodedKmerGenerator(ksize, new StringSampleReader(sequence));
		sketch = output;
	}

	@Override
	public void run() {
		while (generator.hasNext()) {
			long encodedKmer = generator.next();
			sketch.addKmer(encodedKmer, ksize);
		}
	}

}
