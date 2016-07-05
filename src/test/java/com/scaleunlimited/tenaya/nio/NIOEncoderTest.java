package com.scaleunlimited.tenaya.nio;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.scaleunlimited.tenaya.data.EncodedKmerGenerator;
import com.scaleunlimited.tenaya.sample.FileOptions;
import com.scaleunlimited.tenaya.sample.FileSampleReader;
import com.scaleunlimited.tenaya.sample.Sample;

public class NIOEncoderTest {
	
	@SuppressWarnings("resource")
	@Test
	public void testEncoder() throws Exception {
		int ksize = 20;
		File file = new File("test/sample.fasta");
		String regex = "(.*)";
		
		FileSampleReader fileSampleReader = new FileSampleReader(file, FileOptions.inferFromFilename(file), 10*1024*1024, regex);
		Sample sample = fileSampleReader.readSample();
		EncodedKmerGenerator fileEncodedGenerator = new EncodedKmerGenerator(ksize, sample);
		
		NIOSampleReader nioSampleReader = new NIOSampleReader(file, 1000, regex);
		NIOSample nioSample = nioSampleReader.readSample();
		NIOEncoder nioEncoder = new NIOEncoder(ksize, nioSample);
		
		while (nioEncoder.hasNext() && fileEncodedGenerator.hasNext()) {
			long fileHash = fileEncodedGenerator.next();
			long nioHash = nioEncoder.next();
			assertEquals("Hashes from the file sample reader should equal the NIO-generated hash", fileHash, nioHash);
		}
	}

}
