package com.scaleunlimited.tenaya;

import org.kohsuke.args4j.Option;

public class DataSearchToolOptions {
	
	@Option(name="-o", usage="Organism name with spaces replaced with plus signs", required=true, aliases="--organism")
	private String term;
	
	@Option(name="-n", usage="Number of results to return", required=false, aliases="--count")
	private int count = 10;

	public String getTerm() {
		return term;
	}
	
	public int getCount() {
		return count;
	}

}
