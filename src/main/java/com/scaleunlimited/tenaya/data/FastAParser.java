package com.scaleunlimited.tenaya.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastAParser implements Parser {
	
	private String lastLine;
	private Pattern pattern;
	private BufferedReader bufferedReader;
	
	public FastAParser(BufferedReader reader) {
		this(reader, ">.*");
	}

	public FastAParser(BufferedReader reader, String identifierRegex) {
		bufferedReader = reader;
		pattern = Pattern.compile(identifierRegex);
		
		incrLine();
	}
	
	public void incrLine() {
		try {
			lastLine = bufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String readLine() {
		return lastLine;
	}
	
	public String readIdentifier() {
		String line = "";
		while (true) {
			line = readLine();
			if (line == null) {
				return null;
			}
			if (line.startsWith(">")) {
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
			if (line == null) {
				return null;
			}
			if (line.startsWith(";")) {
				// pass; we don't care about comments
			} else if (line.startsWith(">")) {
				String token = parseIdentifierLine(line);
				if (!token.equals(identifier)) {
					return null;
				}
			} else {
				incrLine();
				return line;
			}
			incrLine();
		}
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
