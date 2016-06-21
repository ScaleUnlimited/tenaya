package com.scaleunlimited.tenaya.data;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileSampleReader implements SampleReader, Closeable {

	public enum FileFormat {
		FASTA,
		FASTQ
	}
	
	private String identifier;
	
	private FileReader fileReader;
	private BufferedReader bufferedReader;
	private Parser parser;
	
	public FileSampleReader(File file, FileFormat format) {
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader, 100 * 1024 * 1024);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		switch (format) {
		case FASTA:
			parser = new FastAParser(bufferedReader);
			break;
		case FASTQ:
			parser = new FastQParser(bufferedReader);
			break;
		}
		
		parser.readIdentifier();
	}
	
	public String readSequence() {
		return parser.readSequence();
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	@Override
	public void close() throws IOException {
		bufferedReader.close();
	}
}
