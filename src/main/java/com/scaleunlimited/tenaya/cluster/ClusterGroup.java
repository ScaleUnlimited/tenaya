package com.scaleunlimited.tenaya.cluster;

import java.util.ArrayList;
import java.util.List;

import com.scaleunlimited.tenaya.data.Signature;

public class ClusterGroup {
	
	private List<Signature> signatures;
	private List<Cluster> clusters;
	private double threshold;
	
	public ClusterGroup(double threshold) {
		signatures = new ArrayList<Signature>();
		clusters = new ArrayList<Cluster>();
		this.threshold = threshold;
	}
	
	public void addSignature(Signature sig) {
		signatures.add(sig);
	}
	
	public List<Cluster> cluster() {
		for (Signature sig : signatures) {
			System.out.println("Processing " + sig.getIdentifier());
			if (clusters.size() == 0) {
				Cluster first = new OptimisticCluster();
				first.add(sig);
				clusters.add(first);
				continue;
			}
			List<Cluster> close = new ArrayList<Cluster>();
			Cluster closest = null;
			double closestSim = 0.0;
			for (Cluster cluster : clusters) {
				double sim = cluster.similarity(sig);
				if (sim >= threshold) {
					close.add(cluster);
				}
				if (sim > closestSim) {
					closestSim = sim;
					closest = cluster;
				}
			}
			System.out.println("Best similarity: " + closestSim);
			int numClusters = close.size();
			if (numClusters == 0) {
				System.out.println("Too different");
				Cluster another = new OptimisticCluster();
				another.add(sig);
				clusters.add(another);
			} else {
				if (numClusters > 1) {
					System.out.println("Ignoring the " + numClusters + " clusters within the threshold");
					System.out.println("Too similar");
				} else {
					System.out.println("Just right");
				}
				closest.add(sig);
			}
		}
		for (Cluster c : clusters) {
			System.out.println("Cluster " + c.toString());
			for (Signature s : c.getSignatures()) {
				System.out.println(s.getIdentifier());
			}
		}
		return clusters;
	}

}
