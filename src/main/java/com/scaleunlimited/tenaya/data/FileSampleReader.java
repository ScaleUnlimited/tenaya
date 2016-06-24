package com.scaleunlimited.tenaya.data;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

public class FileSampleReader implements SampleReader, Closeable {

	public enum FileFormat {
		FASTA,
		FASTQ
	}
	
	private String identifier;
	
	private FileInputStream fileInputStream;
	private Reader decoder;
	private BufferedReader bufferedReader;
	private Parser parser;
	
	public FileSampleReader(File file, FileFormat format) {
		this(file, format, false);
	}
	
	public FileSampleReader(File file, FileFormat format, boolean gzip) {
		this(file, format, 10 * 1024 * 1024, "", gzip);
	}
	
	public FileSampleReader(File file, FileFormat format, int bufferSize, String regex, boolean gzip) {
		try {
			fileInputStream = new FileInputStream(file);
			if (gzip) {
				GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
				decoder = new InputStreamReader(gzipInputStream);
			} else {
				decoder = new InputStreamReader(fileInputStream);
			}
			bufferedReader = new BufferedReader(decoder, bufferSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		switch (format) {
		case FASTA:
			parser = new FastAParser(bufferedReader, regex);
			break;
		case FASTQ:
			parser = new FastQParser(bufferedReader, regex);
			break;
		}	
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	@Override
	public void close() throws IOException {
		bufferedReader.close();
	}

	@Override
	public Sample readSample() {
		return parser.readSample();
	}
}
