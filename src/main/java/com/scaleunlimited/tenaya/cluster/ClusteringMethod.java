package com.scaleunlimited.tenaya.cluster;

import java.util.List;

import com.scaleunlimited.tenaya.data.Signature;

public interface ClusteringMethod {
	
	public List<Cluster> cluster(List<Signature> signatures);

}
