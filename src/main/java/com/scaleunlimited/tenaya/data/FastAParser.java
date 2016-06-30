package com.scaleunlimited.tenaya.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastAParser implements Parser {
	
	private Pattern pattern;
	private BufferedReader bufferedReader;
	private String identifier;
	
	public FastAParser(BufferedReader reader) {
		this(reader, ">.*");
	}

	public FastAParser(BufferedReader reader, String identifierRegex) {
		bufferedReader = reader;
		pattern = Pattern.compile(identifierRegex);
	}
	
	public String readLine() {
		try {
			return bufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String readIdentifier() {
		if (identifier == null) {
			String line = readLine();
			if (line.startsWith(">")) {
				identifier = parseIdentifierLine(line);
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
		String seq = "";
		boolean sawSeq = false;
		while ((line = readLine()) != null) {
			if (line.startsWith(";")) {
				if (sawSeq) return seq;
				// pass; we don't care about comments
			} else if (line.startsWith(">")) {
				this.identifier = parseIdentifierLine(line);
				if (!this.identifier.equals(identifier)) {
					break;
				}
				if (sawSeq) return seq;
			} else {
				seq += line;
				sawSeq = true;
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
