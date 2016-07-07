package com.scaleunlimited.tenaya;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

public class SignatureClusterToolOptions {

	@Option(name="-m", usage="Clustering method to use", required=false, aliases="--method")
	private String method = "simple";
	
	@Option(name="-i", usage="Input files", required=true, aliases="--input", handler=StringArrayOptionHandler.class)
	private String[] inputs;
	
	@Option(name="-t", usage="Clustering similarity threshold (simple only)", required=false, aliases="--threshold")
	private double threshold = 0.15;
	
	@Option(name="-k", usage="Set the k for clustering (FLAME only)", required=false)
	private int k = 5;
	
	@Option(name="-f", usage="Set output format string where #id is replaced with the id and similarly with #name for scientific name and #title for the title (#name and #title only for SRA reads)", required=false, aliases="--format")
	private String format = "#id";
	
	public String getMethod() {
		return method;
	}
	
	public String[] getInputs() {
		return inputs;
	}
	
	public double getThreshold() {
		return threshold;
	}
	
	public int getK() {
		return k;
	}
	
	public String getFormat() {
		return format;
	}
	
}
