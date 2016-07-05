package com.scaleunlimited.tenaya.sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastAParser implements Parser {
	
	public enum ParserState {
		READ_LINE_WANT_SEQ,
		READ_LINE,
		READ_SEQ,
		PARSE_LINE
	}
	
	public enum LineType {
		COMMENT,
		IDENTIFIER,
		SEQUENCE
	}
	
	private Pattern pattern;
	private BufferedReader bufferedReader;
	private String identifier;
	
	private String lastLine;
	
	private ParserState state;
	
	public FastAParser(BufferedReader reader) {
		this(reader, ">.*");
	}

	public FastAParser(BufferedReader reader, String identifierRegex) {
		bufferedReader = reader;
		pattern = Pattern.compile(identifierRegex);
		state = ParserState.READ_LINE;
	}
	
	public LineType getLineType(String line) {
		if (line.startsWith(";")) {
			return LineType.COMMENT;
		} else if (line.startsWith(">")) {
			return LineType.IDENTIFIER;
		} else {
			return LineType.SEQUENCE;
		}
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
			String line;
			while ((line = readLine()) != null) {
				if (line.startsWith(">")) {
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
		String seq = "";
		while (true) {
			switch (state) {
			case READ_LINE:
				lastLine = readLine();
				if (lastLine == null) {
					return null;
				}
				state = ParserState.PARSE_LINE;
				break;
			case READ_LINE_WANT_SEQ:
				lastLine = readLine();
				if (lastLine == null) {
					return null;
				}
				state = ParserState.READ_SEQ;
				break;
			case PARSE_LINE:
				LineType type = getLineType(lastLine);
				switch (type) {
				case IDENTIFIER:
					this.identifier = parseIdentifierLine(lastLine);
					if (!this.identifier.equals(identifier)) {
						return null;
					}
				case COMMENT:
					state = ParserState.READ_LINE;
					break;
				case SEQUENCE:
					state = ParserState.READ_SEQ;
					break;
				}
				break;
			case READ_SEQ:
				if (getLineType(lastLine) != LineType.SEQUENCE) {
					state = ParserState.PARSE_LINE;
					return seq;
				} else {
					state = ParserState.READ_LINE_WANT_SEQ;
					seq += lastLine;
				}
			}
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
