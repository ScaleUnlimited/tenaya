package com.scaleunlimited.tenaya.data;

import java.util.ArrayList;
import java.util.List;

public class ClusterGroup {
	
	public static final double CLUSTER_THRESHOLD = 0.8;
	
	private List<Signature> signatures;
	private List<Cluster> clusters;
	
	public ClusterGroup() {
		signatures = new ArrayList<Signature>();
		clusters = new ArrayList<Cluster>();
	}
	
	public void addSignature(Signature sig) {
		signatures.add(sig);
	}
	
	public void cluster() {
		for (Signature sig : signatures) {
			System.out.println("Processing " + sig.getIdentifier());
			if (clusters.size() == 0) {
				Cluster first = new Cluster();
				first.add(sig);
				clusters.add(first);
				continue;
			}
			List<Cluster> close = new ArrayList<Cluster>();
			Cluster closest = null;
			double closestDist = 1.0;
			for (Cluster cluster : clusters) {
				double dist = cluster.distance(sig);
				if (dist < CLUSTER_THRESHOLD) {
					close.add(cluster);
					if (dist < closestDist) {
						closestDist = dist;
						closest = cluster;
					}
				}
			}
			int numClusters = close.size();
			if (numClusters == 0) {
				//System.out.println("Too different");
				Cluster another = new Cluster();
				another.add(sig);
				clusters.add(another);
			} else {
				if (numClusters > 1) {
					System.out.println("Ignoring the " + numClusters + " clusters within the threshold");
					//System.out.println("Too similar");
				} else {
					//System.out.println("Just right");
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
	}

}
