package com.scaleunlimited.tenaya.sample;

public interface Parser {
	
	public Sample readSample();
	String readSequence(String identifier);
	public String readIdentifier();

}
