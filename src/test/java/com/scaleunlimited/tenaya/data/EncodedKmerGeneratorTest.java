package com.scaleunlimited.tenaya.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class EncodedKmerGeneratorTest {
	
	@Test
	public void testGenerator() {
		final String testSequence = "ATTACATAACCCAATGATACCCTAGAATGAAGCTTGCCTGCACAGACACAAAACTTTTCCTACCATGAAGCTCTTATGCACATTCTAATTCTACTATTCA";
		System.out.println(testSequence.length());
		int ksize = 20;
		
		KmerGenerator regularGenerator = new KmerGenerator(ksize, new SimpleSampleReader(new String[]{testSequence}));
	
		EncodedKmerGenerator encodedGenerator = new EncodedKmerGenerator(ksize, new SimpleSampleReader(new String[]{testSequence}));
		
		while (regularGenerator.hasNext()) {
			String kmer = regularGenerator.next();
			long regularEncoding = Kmer.encode(kmer, ksize);
			long generatedEncoding = encodedGenerator.next();
			assertEquals("Encodings must be equal", regularEncoding, generatedEncoding);
		}
	}

}
