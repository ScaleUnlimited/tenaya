package com.scaleunlimited.tenaya.nio;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NIOFastAParser {

	// newline and carriage return
	public static final byte[] IGNORE_CHARS = {10, 13}; 
	
	public enum ParserState {
		READ_CHAR,
		PARSE_CHAR,
		READ_SEQ_SKIP,
		READ_SEQ_CHAR
	}
	
	public enum LineType {
		COMMENT,
		IDENTIFIER,
		SEQUENCE
	}
	
	private NIOSampleReader sampleReader;
	private ParserState state;
	
	private byte lastChar;
	
	private Pattern pattern;
	
	public NIOFastAParser(NIOSampleReader reader, String regex) {
		state = ParserState.READ_CHAR;
		pattern = Pattern.compile(regex);
		sampleReader = reader;
	}
	
	public LineType getLineType(byte c) {
		if (c == (byte) ';') {
			return LineType.COMMENT;
		} else if (c == (byte) '>') {
			return LineType.IDENTIFIER;
		} else {
			return LineType.SEQUENCE;
		}
	}
	
	public String matchIdentifier(String line) {
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "";
	}
	
	public String readIdentifier() {
		char lastChar;
		while ((lastChar = (char) sampleReader.readByte()) != 0) {
			if (lastChar == '>') break;
		}
		return matchIdentifier(sampleReader.readLine());
	}
	
	public boolean shouldIgnore(byte b) {
		for (int i = 0; i < IGNORE_CHARS.length; i++) {
			if (IGNORE_CHARS[i] == b) {
				return true;
			}
		}
		return false;
	}
	
	public byte readByte() {
		while (true) {
			switch (state) {
			case READ_CHAR:
				lastChar = sampleReader.readByte();
				state = ParserState.PARSE_CHAR;
				break;
			case PARSE_CHAR:
				LineType type = getLineType(lastChar);
				switch (type) {
				case IDENTIFIER:
					String identifier = readIdentifier();
					if (!sampleReader.getCurrentIdentifier().equals(identifier)) {
						sampleReader.setNewIdentifier(identifier);
						return 0;
					}
					state = ParserState.READ_CHAR;
					break;
				case COMMENT:
					sampleReader.skipLine();
					state = ParserState.READ_CHAR;
					break;
				case SEQUENCE:
					state = ParserState.READ_SEQ_CHAR;
					return lastChar;
				}
				break;
			case READ_SEQ_SKIP:
				lastChar = sampleReader.readByte();
				if (!shouldIgnore(lastChar)) {
					if (getLineType(lastChar) == LineType.SEQUENCE) {
						state = ParserState.READ_CHAR;
						return lastChar;
					} else {
						state = ParserState.PARSE_CHAR;
						return (byte) '\n';
					}
				}
				break;
			case READ_SEQ_CHAR:
				lastChar = sampleReader.readByte();
				if (shouldIgnore(lastChar)) {
					state = ParserState.READ_SEQ_SKIP;
				} else {
					return lastChar;
				}
				break;
			}
		}
	}
	
	public NIOSampleReader getReader() {
		return sampleReader;
	}

}
