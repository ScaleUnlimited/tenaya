package com.scaleunlimited.tenaya.data;

public interface KmerCounter {

	public int addKmer(String kmer, int ksize);
	public int addKmer(long encoded, int ksize);
	public int countKmer(String kmer, int ksize);
	public int countKmer(long encoded, int ksize);
	public void reset();
	
}
