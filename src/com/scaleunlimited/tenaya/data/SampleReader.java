package com.scaleunlimited.tenaya.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SampleReader {

	public enum FileFormat {
		FASTA,
		FASTQ
	}
	
	private String identifier;
	
	private FileReader fileReader;
	private BufferedReader bufferedReader;
	private Parser parser;
	
	public SampleReader(File file, FileFormat format) {
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
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
		
		readIdentifier();
	}
	
	public void readIdentifier() {
		identifier = parser.readIdentifier();
	}
	
	public String readSequence() {
		return parser.readSequence();
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public void close() {
		try {
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void reset() {
		//close();
		bufferedReader = new BufferedReader(fileReader);
	}
}
