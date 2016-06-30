package com.scaleunlimited.tenaya.sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastQParser implements Parser {
	
	public static final int IDENTIFIER_LINE = 0;
	public static final int SEQ_LINE = 1;
	
	private Pattern pattern;
	private BufferedReader bufferedReader;
	private String identifier;
	private int line;
	
	public FastQParser(BufferedReader reader) {
		this(reader, "@.*");
	}
	
	public FastQParser(BufferedReader reader, String identifierRegex) {
		bufferedReader = reader;
		pattern = Pattern.compile(identifierRegex);
		line = -1;
	}
	
	public String readLine() {
		try {
			line++;
			return bufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public int getLineType() {
		return line % 4;
	}
	
	public String readIdentifier() {
		if (identifier == null) {
			String line;
			while ((line = readLine()) != null) {
				if (getLineType() == IDENTIFIER_LINE) {
					identifier = parseIdentifierLine(line);
					break;
				}
			}
		}
		return identifier;
	}
	
	public String parseIdentifierLine(String line) {
		Matcher matcher = pattern.matcher(line);
		matcher.find();
		if (matcher.groupCount() > 1) {
			return matcher.group(1);
		} else {
			return matcher.group(0);
		}
	}

	@Override
	public String readSequence(String identifier) {
		String line;
		while ((line = readLine()) != null) {
			int lineType = getLineType();
			switch (lineType) {
			case IDENTIFIER_LINE:
				this.identifier = parseIdentifierLine(line);
				if (!this.identifier.equals(identifier)) {
					return null;
				}
				break;
			case SEQ_LINE:
				return line;
			}
		}
		return null;
	}

	@Override
	public Sample readSample() {
		String identifier = readIdentifier();
		if (identifier == null) {
			return null;
		}
		return new ParserSample(identifier, this);
	}

}
