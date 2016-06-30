package com.scaleunlimited.tenaya.sample;

public class ParserSample implements Sample {

	private Parser parser;
	private String identifier;
	
	public ParserSample(String identifier, Parser parser) {
		this.identifier = identifier;
		this.parser = parser;
	}
	
	@Override
	public String readSequence() {
		return parser.readSequence(identifier);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

}
