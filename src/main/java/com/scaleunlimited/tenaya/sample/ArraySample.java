package com.scaleunlimited.tenaya.sample;

public class ArraySample implements Sample {
	
	private String[] sequences;
	private int index;
	
	public ArraySample(String[] seqs) {
		sequences = seqs;
		index = 0;
	}

	@Override
	public String readSequence() {
		if (index < sequences.length) {
			return sequences[index++];
		} else {
			return null;
		}
	}

	@Override
	public String getIdentifier() {
		return "Array Sample [" + toString() + "]";
	}

}
