package com.scaleunlimited.tenaya.sample;

public class StringSample implements Sample {
	
	private String sequence;
	private boolean done;
	
	public StringSample(String seq) {
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

	@Override
	public String getIdentifier() {
		return "String Sample [" + toString() + "]";
	}
	
	

}
