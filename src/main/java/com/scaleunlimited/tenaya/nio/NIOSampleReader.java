package com.scaleunlimited.tenaya.nio;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.scaleunlimited.tenaya.metadata.ExperimentMetadata;

public class NIOSampleReader implements Closeable {

	private SeekableByteChannel channel;
	private ByteBuffer buffer;
	
	private NIOFastAParser parser;
	
	private String currentIdentifier;
	private String newIdentifier;
	private int bytesRead;
	private int readPointer;
	private boolean readEof;
	
	public NIOSampleReader(File file, int bufferSize, String regex) throws IOException {
		channel = Files.newByteChannel(file.toPath(), StandardOpenOption.READ);
		buffer = ByteBuffer.allocate(bufferSize);
		parser = new NIOFastAParser(this, regex);
		
		readEof = false;
		
		setNewIdentifier(parser.readIdentifier());
	}
	
	private void readChunk() {
		if (!readEof) {
			try {
				readPointer = 0;
				bytesRead = channel.read(buffer);
				if (bytesRead == -1) {
					readEof = true;
				}
				buffer.rewind();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public byte readByte() {
		if (readPointer == bytesRead) {
			readChunk();
		}
		if (!readEof) {
			return buffer.get(readPointer++);
		}
		return 0;
	}
	
	public String readLine() {
		String line = "";
		char lastChar;
		while ((lastChar = (char) readByte()) != 0) {
			if (lastChar == '\n') return line;
			line += lastChar;
		}
		return line;
	}
	
	public void skipLine() {
		char lastChar;
		while ((lastChar = (char) readByte()) != 0) {
			if (lastChar == '\n') return;
		}
	}
	
	public String getCurrentIdentifier() {
		return currentIdentifier;
	}
	
	public void setNewIdentifier(String newId) {
		newIdentifier = newId;
	}
	
	public String readIdentifier() {
		return parser.readIdentifier();
	}
	
	public NIOSample readSample() {
		if (readEof) return null;
		if (!newIdentifier.equals(currentIdentifier)) {
			currentIdentifier = newIdentifier;
			return new NIOSample(parser);
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		channel.close();		
	}
	
}
