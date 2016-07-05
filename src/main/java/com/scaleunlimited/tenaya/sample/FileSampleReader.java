package com.scaleunlimited.tenaya.sample;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

public class FileSampleReader implements SampleReader, Closeable {
	
	private String identifier;
	private FileInputStream fileInputStream;
	private Reader decoder;
	private BufferedReader bufferedReader;
	private Parser parser;
	
	public FileSampleReader(File file, FileOptions options) {
		this(file, options, 10 * 1024 * 1024, "Default identifier");
	}
	
	public FileSampleReader(File file, FileOptions options, int bufferSize, String regex) {
		try {
			fileInputStream = new FileInputStream(file);
			if (options.compressed) {
				GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
				decoder = new InputStreamReader(gzipInputStream);
			} else {
				decoder = new InputStreamReader(fileInputStream);
			}
			bufferedReader = new BufferedReader(decoder, bufferSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		switch (options.format) {
		case FASTA:
			parser = new FastAParser(bufferedReader, regex);
			break;
		case FASTQ:
			parser = new FastQParser(bufferedReader, regex);
			break;
		case UNKNOWN:
			throw new IllegalArgumentException("File format must be of known parsing type");
		}	
		
		identifier = parser.readIdentifier();
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
