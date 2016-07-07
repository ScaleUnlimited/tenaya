package com.scaleunlimited.tenaya.cluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.scaleunlimited.tenaya.data.Signature;

public class OptimisticCluster implements Cluster {

	private Set<Long> hashes;
	private List<Signature> signatures;
	
	public OptimisticCluster() {
		signatures = new ArrayList<Signature>();
		hashes = new HashSet<Long>();
	}
	
	@Override
	public void add(Signature sig) {
		signatures.add(sig);
		for (Long hash : sig.get()) {
			hashes.add(hash);
		}
	}
	
	@Override
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
