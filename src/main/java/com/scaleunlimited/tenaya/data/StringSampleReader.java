package com.scaleunlimited.tenaya.data;

public class StringSampleReader implements SampleReader {
	
	private String sequence;
	private boolean done;
	
	public StringSampleReader(String seq) {
		sequence = seq;
		done = false;
	}

	@Override
	public String readSequence() {
		if (done) {
			return null;
		} else {
			done = true;
			return sequence;
		}
	}
	
	

}
