package com.scaleunlimited.tenaya.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Cluster {
	
	private Set<Long> hashes;
	private List<Signature> signatures;
	
	public Cluster() {
		hashes = new HashSet<Long>();
		signatures = new ArrayList<Signature>();
	}
	
	public void add(Signature sig) {
		signatures.add(sig);
		for (long hash : sig.get()) {
			hashes.add(hash);
		}
	}
	
	public List<Signature> getSignatures() {
		return signatures;
	}
	
	public double distance(Signature sig) {
		Signature cluster = new Signature(hashes.size());
		for (Long hash : hashes) {
			cluster.add(hash);
		}
		return 1.0 - sig.jaccard(cluster);
	}

}
