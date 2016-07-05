package com.scaleunlimited.tenaya.sample;

import java.io.File;

import com.scaleunlimited.tenaya.sample.FileOptions;

public class FileOptions {
	
	public final FileFormat format;
	public final boolean compressed;
	
	public FileOptions(FileFormat format) {
		this(format, false);
	}
	
	public FileOptions(FileFormat format, boolean compressed) {
		this.format = format;
		this.compressed = compressed;
	}
	
	public static FileOptions inferFromFilename(File file) {
		FileFormat format;
		boolean compressed;
		String filename = file.getName().toLowerCase();
		if (filename.indexOf(".fasta") != -1 || filename.indexOf(".fa") != -1) {
			format = FileFormat.FASTA;
		} else if (filename.indexOf(".fastq") != -1 || filename.indexOf(".fq") != -1) {
			format = FileFormat.FASTQ;
		} else {
			format = FileFormat.UNKNOWN;
		}
		compressed = (filename.indexOf(".gz") != -1);
		return new FileOptions(format, compressed);
	}
	
}
