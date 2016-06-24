package com.scaleunlimited.tenaya;

import java.io.File;
import org.kohsuke.args4j.Option;

public class SignatureGenerationToolOptions {
	
	@Option(name="-i", usage="Input FASTA or FASTQ file", required=true, aliases="--input")
	private File input;
	
	@Option(name="-o", usage="Output signature file", required=true, aliases="--output")
	private File output;
	
	@Option(name="-k", usage="Length of generated k-mers", required=false, aliases="--ksize")
	private int ksize = 20;
	
	@Option(name="-M", usage="Maximum memory in bytes for the Count-Min Sketch", required=false, aliases="--max-memory")
	private int maxMemory = 1000000000;
	
	@Option(name="-c", usage="Cutoff count for filtering k-mers", required=false, aliases="--cutoff")
	private int cutoff = 1;
	
	@Option(name="-s", usage="Size of the generate signature", required=false, aliases="--signature-size")
	private int signatureSize = 1000;
	
	@Option(name="-b", usage="Buffer size for the input file (in bytes)", required=false, aliases="--buffer-size")
	private int bufferSize = 10 * 1024 * 1024;
	
	@Option(name="-C", usage="Number of chunks for the Count-Min Sketch", required=false, aliases="--chunks")
	private int chunks = 1000;
	
	@Option(name="-d", usage="Depth (i.e. rows or hashes) of the Count-Min Sketch", required=false, aliases="--depth")
	private int depth = 5;
	
	public File getInputFile() {
		return input;
	}
	
	public File getOutputFile() {
		return output;
	}
	
	public int getKsize() {
		return ksize;
	}
	
	public int getMaxMemory() {
		return maxMemory;
	}
	
	public int getCutoff() {
		return cutoff;
	}
	
	public int getSignatureSize() {
		return signatureSize;
	}
	
	public int getBufferSize() {
		return bufferSize;
	}
	
	public int getChunks() {
		return chunks;
	}
	
	public int getDepth() {
		return depth;
	}

}
