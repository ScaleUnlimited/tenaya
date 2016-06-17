package com.scaleunlimited.tenaya.data;

public class Kmer {

	public static long encode(String sequence, int ksize) {
		long f = 0, r = 0;
		for (int i = 0, j = (ksize - 1); i < ksize; i++, j--) {
			f <<= 2;
			r <<= 2;
			
			f |= repr(sequence.charAt(i));
			r |= comp(sequence.charAt(j));
		}
		
		return unify(f, r);
	}
	
	public static String decodeForward(long encoded, int ksize) {
		String sequence = "";
		for (int i = 0; i < ksize; i++) {
			sequence = rrepr(encoded & 0b011L) + sequence;
			encoded >>= 2;
		}
		return sequence;
	}
	
	public static String decodeReverse(long encoded, int ksize) {
		String sequence = "";
		for (int i = 0; i < ksize; i++) {
			sequence += rcomp(encoded & 0b011L);
			encoded >>= 2;
		}
		return sequence;
	}
	
	public static long unify(long f, long r) {
		return (f < r) ? f : r;
	}
	
	public static char rrepr(long l) {
		if (l == 0L) {
			return 'A';
		} else if (l == 1L) {
			return 'T';
		} else if (l == 2L) {
			return 'C';
		} else {
			return 'G';
		}
	}
	
	public static char rcomp(long l) {
		if (l == 0L) {
			return 'T';
		} else if (l == 1L) {
			return 'A';
		} else if (l == 2L) {
			return 'G';
		} else {
			return 'C';
		}
	}
	
	public static long repr(char c) {
		if (c == 'A') {
			return 0L;
		} else if (c == 'T') {
			return 1L;
		} else if (c == 'C') {
			return 2L;
		} else {
			return 3L;
		}
	}
	
	public static long comp(char c) {
		if (c == 'A') {
			return 1L;
		} else if (c == 'T') {
			return 0L;
		} else if (c == 'C') {
			return 3L;
		} else {
			return 2L;
		}
	}

	public static long[] hashMurmur(long kmer, int n, int ksize, long[] hashes) {
		if (hashes == null) {
			hashes = new long[n];
		}
		int seed = 42;
		for (int i = 0; i < n; i++) {
			hashes[i] = MurmurHash3.hashLong(kmer, seed);
			seed = (int) hashes[i];
		}
		return hashes;
	}
	
}
