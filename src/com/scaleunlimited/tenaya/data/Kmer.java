package com.scaleunlimited.tenaya.data;

public class Kmer {

	public static long encode(String sequence) {
		long f = 0, r = 0;
		int len = sequence.length();
		for (int i = 0, j = (len - 1); i < len; i++, j--) {
			f |= repr(sequence.charAt(i));
			r |= comp(sequence.charAt(j));
			
			f <<= 2;
			r <<= 2;
		}
		
		return unify(f, r);
	}
	
	public static long unify(long f, long r) {
		return (f < r) ? f : r;
	}
	
	public static int repr(char c) {
		if (c == 'A') {
			return 0;
		} else if (c == 'T') {
			return 1;
		} else if (c == 'C') {
			return 2;
		} else {
			return 3;
		}
	}
	
	public static int comp(char c) {
		if (c == 'A') {
			return 1;
		} else if (c == 'T') {
			return 0;
		} else if (c == 'C') {
			return 3;
		} else {
			return 2;
		}
	}
	
}
