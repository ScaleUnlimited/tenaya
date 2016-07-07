package com.scaleunlimited.tenaya.cluster;

import java.util.ArrayList;
import java.util.List;

import com.scaleunlimited.tenaya.data.Signature;

public class SimpleCluster implements Cluster {
	
	private List<Signature> signatures;
	
	public SimpleCluster() {
		signatures = new ArrayList<Signature>();
	}

	@Override
	public void add(Signature sig) {
		signatures.add(sig);
	}

	@Override
	public List<Signature> getSignatures() {
		return signatures;
	}

}
