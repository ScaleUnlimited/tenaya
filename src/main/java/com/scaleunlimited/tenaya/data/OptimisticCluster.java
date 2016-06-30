package com.scaleunlimited.tenaya.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OptimisticCluster extends Cluster {

	private Set<Long> hashes;
	private List<Signature> signatures;
	
	public OptimisticCluster() {
		signatures = new ArrayList<Signature>();
		hashes = new HashSet<Long>();
	}
	
	public void add(Signature sig) {
		signatures.add(sig);
		for (Long hash : sig.get()) {
			hashes.add(hash);
		}
	}
	
	public List<Signature> getSignatures() {
		return signatures;
	}
	
	public double similarity(Signature sig) {
		Signature cluster = new Signature(hashes.size());
		for (Long hash : hashes) {
			cluster.add(hash);
		}
		return sig.jaccard(cluster);
	}

}
