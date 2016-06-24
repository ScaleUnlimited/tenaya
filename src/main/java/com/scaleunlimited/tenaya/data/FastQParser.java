package com.scaleunlimited.tenaya.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastQParser implements Parser {
	
	public static final int IDENTIFIER_LINE = 0;
	public static final int SEQ_LINE = 1;
	
	private String lastLine;
	private Pattern pattern;
	private BufferedReader bufferedReader;
	private int count = -1;
	
	public FastQParser(BufferedReader reader) {
		this(reader, "@.*");
	}
	
	public FastQParser(BufferedReader reader, String identifierRegex) {
		bufferedReader = reader;
		pattern = Pattern.compile(identifierRegex);
		
		incrLine();
	}
	
	public String readLine() {
		return lastLine;
	}
	
	public void incrLine() {
		try {
			lastLine = bufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		count++;
	}
	
	public String readLineAndIncr() {
		String line = lastLine;
		incrLine();
		return line;
	}
	
	public int getLineType() {
		return count % 4;
	}
	
	public String readIdentifier() {
		String line = "";
		while (true) {
			line = readLineAndIncr();
			if (line == null) {
				return null;
			}
			if (line.startsWith("@")) {
				return parseIdentifierLine(line);
			}
		}
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
		String line = "";
		while (true) {
			line = readLine();
			if (line == null) break;
			int lineType = getLineType();
			switch (lineType) {
			case IDENTIFIER_LINE:
				String identifierToken = parseIdentifierLine(line);
				if (!identifierToken.equals(identifier)) {
					return null;
				}
				incrLine();
				break;
			case SEQ_LINE:
				incrLine();
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
