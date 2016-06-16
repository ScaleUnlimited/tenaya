package com.scaleunlimited.tenaya.data;

import java.io.BufferedReader;
import java.io.IOException;

public class FastQParser implements Parser {
	
	private BufferedReader bufferedReader;
	private int count = -1;
	
	public FastQParser(BufferedReader reader) {
		bufferedReader = reader;
	}
	
	public String readLineAndIncr() {
		count++;
		try {
			return bufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String readIdentifier() {
		String line = "";
		while (true) {
			line = readLineAndIncr();
			if (line == null) break;
			if (line.startsWith("@")) {
				return line.substring(1);
			}
		}
		return null;
	}

	@Override
	public String readSequence() {
		String line = "";
		// increment until the second line is read
		while (true) {
			line = readLineAndIncr();
			if (line == null) break;
			if ((count % 4) == 1) {
				return line;
			}
		}
		return null;
	}

}
