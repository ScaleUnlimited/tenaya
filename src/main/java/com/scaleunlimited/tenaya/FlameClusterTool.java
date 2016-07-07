package com.scaleunlimited.tenaya;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.scaleunlimited.tenaya.cluster.SignatureDataSet;
import com.scaleunlimited.tenaya.cluster.SignatureDistance;
import com.scaleunlimited.tenaya.cluster.SignatureVec;
import com.scaleunlimited.tenaya.data.Signature;
import com.scaleunlimited.tenaya.metadata.ExperimentMetadata;

import jsat.classifiers.DataPoint;
import jsat.clustering.FLAME;

public class FlameClusterTool {
	
	public static void main(String[] args) {
		try {
			clusterSignatures(args);
		} catch (Throwable t) {
			System.err.println("Tool failed: " + t.getMessage());
			t.printStackTrace();
			System.exit(-1);
		}
	}

	public static void clusterSignatures(String[] sigs) throws Exception {
		int n = sigs.length;
		Signature[] signatures = new Signature[n];
		for (int i = 0; i < n; i++) {
			System.out.println("Loading signature " + sigs[i]);
			signatures[i] = Signature.createFromFile(new File(sigs[i]));
		}
		FLAME flame = new FLAME(new SignatureDistance(), 3, 100);
		List<List<DataPoint>> clusters = flame.cluster(new SignatureDataSet(signatures));
		System.out.println("Found " + clusters.size() + " clusters");
		for (List<DataPoint> cluster : clusters) {
			System.out.println("====Cluster====");
			for (DataPoint point : cluster) {
				String identifier = ((SignatureVec) point.getNumericalValues()).getIdentifier();
				String scientificName = ExperimentMetadata.createFromAccession(identifier).getScientificName();
				System.out.println(scientificName);
			}
		}
	}

}
