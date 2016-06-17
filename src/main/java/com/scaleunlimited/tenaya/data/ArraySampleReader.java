package com.scaleunlimited.tenaya.data;

public class ArraySampleReader implements SampleReader {
	
	private String[] sequences;
	private int index;
	
	public ArraySampleReader(String[] seqs) {
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

}
