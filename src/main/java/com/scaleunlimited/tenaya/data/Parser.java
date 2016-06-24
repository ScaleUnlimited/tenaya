package com.scaleunlimited.tenaya.data;

public interface Parser {
	
	public Sample readSample();
	String readSequence(String identifier);

}
