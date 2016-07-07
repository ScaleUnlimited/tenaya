package com.scaleunlimited.tenaya;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jsat.classifiers.DataPoint;
import jsat.clustering.FLAME;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.scaleunlimited.tenaya.cluster.Cluster;
import com.scaleunlimited.tenaya.cluster.SimpleCluster;
import com.scaleunlimited.tenaya.cluster.SimpleClusterer;
import com.scaleunlimited.tenaya.cluster.SignatureDataSet;
import com.scaleunlimited.tenaya.cluster.SignatureDistance;
import com.scaleunlimited.tenaya.cluster.SignatureVec;
import com.scaleunlimited.tenaya.data.Signature;
import com.scaleunlimited.tenaya.metadata.ExperimentMetadata;

public class SignatureClusterTool {
	
	public static void main(String[] args) {
		SignatureClusterToolOptions options = new SignatureClusterToolOptions();
		CmdLineParser cmdParser = new CmdLineParser(options);
		
		try {
			cmdParser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			printUsageAndExit(cmdParser);
		}
		try {
			clusterSignatures(options);
		} catch (Throwable t) {
			System.err.println("Tool failed: " + t.getMessage());
			t.printStackTrace();
			System.exit(-1);
		}
	}
	
	private static void printUsageAndExit(CmdLineParser parser) {
		parser.printUsage(System.err);
		System.exit(-1);
	}
	
	public static Signature[] loadSignatures(String[] sigs) throws IOException {
		int n = sigs.length;
		Signature[] signatures = new Signature[n];
		for (int i = 0; i < n; i++) {
			System.out.println("Loading signature " + sigs[i]);
			signatures[i] = Signature.createFromFile(new File(sigs[i]));
		}
		return signatures;
	}
	
	public static void clusterSignatures(SignatureClusterToolOptions options) throws Exception {
		String method = options.getMethod().toLowerCase();
		List<Cluster> clusters = null;
		if (method.equals("flame")) {
			clusters = clusterSignaturesFLAME(options);
		} else if (method.equals("simple")) {
			clusters = clusterSignaturesSimple(options);
		} else {
			throw new IllegalArgumentException("Unknown method type: " + method);
		}
		String format = options.getFormat();
		boolean lookup = (format.indexOf("#name") != -1) || (format.indexOf("#title") != -1);
		System.out.println("Found " + clusters.size() + " clusters");
		for (Cluster cluster : clusters) {
			System.out.println("====Cluster====");
			List<Signature> signatures = cluster.getSignatures();
			for (Signature signature : signatures) {
				String id = signature.getIdentifier();
				String line = format.replaceAll("#id", id);
				if (lookup) {
					ExperimentMetadata metadata = ExperimentMetadata.createFromAccession(id);
					String title = metadata.getTitle();
					String name = metadata.getScientificName();
					line = line.replaceAll("#title", title).replaceAll("#name", name);
				}
				System.out.println(line);
			}
		}
	}

	public static List<Cluster> clusterSignaturesSimple(SignatureClusterToolOptions options) throws IOException {
		Signature[] signatures = loadSignatures(options.getInputs());
		SimpleClusterer group = new SimpleClusterer(options.getThreshold());
		return group.cluster(Arrays.asList(signatures));
	}
	
	public static List<Cluster> clusterSignaturesFLAME(SignatureClusterToolOptions options) throws Exception {
		Signature[] signatures = loadSignatures(options.getInputs());
		FLAME flame = new FLAME(new SignatureDistance(), options.getK(), 100);
		List<List<DataPoint>> clusters = flame.cluster(new SignatureDataSet(signatures));
		List<Cluster> genericClusters = new ArrayList<Cluster>();
		for (List<DataPoint> points : clusters) {
			Cluster current = new SimpleCluster();
			for (DataPoint point : points) {
				Signature signature = ((SignatureVec) point.getNumericalValues()).asSignature();
				current.add(signature);
			}
			genericClusters.add(current);
		}
		return genericClusters;
	}

}
