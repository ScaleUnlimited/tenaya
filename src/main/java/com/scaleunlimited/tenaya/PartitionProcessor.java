package com.scaleunlimited.tenaya;

import com.scaleunlimited.tenaya.data.KmerCounter;
import com.scaleunlimited.tenaya.data.LongQueue;
import com.scaleunlimited.tenaya.data.MurmurHash3;
import com.scaleunlimited.tenaya.data.Signature;

public class PartitionProcessor extends Thread {
	
	private LongQueue hashQueue;
	private int ksize;
	private Signature signature;
	private KmerCounter kmerCounter;
	private int cutoff;
	
	public PartitionProcessor(LongQueue hashQueue, Signature sig, KmerCounter kCounter) {
		this.hashQueue = hashQueue;
		this.ksize = sig.getKsize();
		signature = sig;
		kmerCounter = kCounter;
		this.cutoff = sig.getCutoff();
	}
	
	@Override
	public void run() {
		while (true) {
			while (hashQueue.isEmpty()) {
				Thread.yield();
			}
			long nextHash = hashQueue.get();
			kmerCounter.addKmer(nextHash, ksize);
			if (kmerCounter.countKmer(nextHash, ksize) >= cutoff) {
				signature.add(MurmurHash3.hashLong(nextHash, 42));
			}
		}
	}
	
	public LongQueue getQueue() {
		return hashQueue;
	}
	
	public Signature getSignature() {
		return signature;
	}
	
	public KmerCounter getKmerCounter() {
		return kmerCounter;
	}
	
	public void reset() {
		kmerCounter.reset();
		signature.clear();
	}

}
