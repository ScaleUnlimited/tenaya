package com.scaleunlimited.tenaya.cluster;

import java.util.ArrayList;
import java.util.List;

import com.scaleunlimited.tenaya.data.Signature;

public class SimpleClusterer implements ClusteringMethod {
	
	private double threshold;
	
	public SimpleClusterer(double threshold) {
		this.threshold = threshold;
	}
	
	@Override
	public List<Cluster> cluster(List<Signature> signatures) {
		List<Cluster> clusters = new ArrayList<Cluster>();
		for (Signature sig : signatures) {
			if (clusters.size() == 0) {
				OptimisticCluster first = new OptimisticCluster();
				first.add(sig);
				clusters.add(first);
				continue;
			}
			List<OptimisticCluster> close = new ArrayList<OptimisticCluster>();
			OptimisticCluster closest = null;
			double closestSim = 0.0;
			for (int i = 0; i < clusters.size(); i++) {
				OptimisticCluster cluster = (OptimisticCluster) clusters.get(i);
				double sim = cluster.similarity(sig);
				if (sim >= threshold) {
					close.add(cluster);
				}
				if (sim > closestSim) {
					closestSim = sim;
					closest = cluster;
				}
			}
			int numClusters = close.size();
			if (numClusters == 0) {
				OptimisticCluster another = new OptimisticCluster();
				another.add(sig);
				clusters.add(another);
			} else {
				if (numClusters > 1) {
					System.out.println("Ignoring the " + numClusters + " clusters within the threshold");
				}
				closest.add(sig);
			}
		}
		return clusters;
	}

}
