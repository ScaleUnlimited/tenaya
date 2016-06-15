package com.scaleunlimited.tenaya.data;

import java.io.BufferedReader;
import java.io.IOException;

public class FastAParser implements Parser {
	
	private BufferedReader bufferedReader;

	public FastAParser(BufferedReader reader) {
		bufferedReader = reader;
	}
	
	@Override
	public String readIdentifier() {
		String line = "";
		while (true) {
			try {
				line = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (line == null) break;
			if (line.startsWith(">")) {
				return line.substring(1);
			}
		}
		return null;
	}

	@Override
	public String readSequence() {
		String line = "";
		while (true) {
			try {
				line = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (line == null) break;
			if (!(line.startsWith(";") || line.startsWith(">"))) {
				return line;
			}
		}
		return null;
	}

}
