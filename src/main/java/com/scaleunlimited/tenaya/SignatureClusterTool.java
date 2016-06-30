package com.scaleunlimited.tenaya;

import java.io.File;
import java.io.IOException;

import com.scaleunlimited.tenaya.data.ClusterGroup;
import com.scaleunlimited.tenaya.data.Signature;

public class SignatureClusterTool {
	
	public static final double CLUSTER_THRESHOLD = 0.358;
	
	public static void main(String[] args) {
		try {
			clusterSignatures(args);
		} catch (Throwable t) {
			System.err.println("Tool failed: " + t.getMessage());
			t.printStackTrace();
			System.exit(-1);
		}
	}

	public static void clusterSignatures(String[] sigs) throws IOException {
		int n = sigs.length;
		ClusterGroup group = new ClusterGroup(CLUSTER_THRESHOLD);
		for (int i = 0; i < n; i++) {
			System.out.println("Loading signature " + sigs[i]);
			group.addSignature(Signature.createFromFile(new File(sigs[i])));
		}
		group.cluster();
	}

}
