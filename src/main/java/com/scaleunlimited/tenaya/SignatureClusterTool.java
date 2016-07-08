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

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.scaleunlimited.tenaya.SignatureClusterToolOptions.ClusterMethod;
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
		ClusterMethod method = options.getMethod();
		List<Cluster> clusters = null;
		switch (method) {
		case SIMPLE:
			clusters = clusterSignaturesSimple(options);
			break;
		case FLAME:
			clusters = clusterSignaturesFLAME(options);
			break;
		case HIERARCHICAL:
			clusters = clusterSignaturesHierarchical(options);
			break;
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
	
	public static List<Cluster> clusterSignaturesHierarchical(SignatureClusterToolOptions options) throws IOException {
		Signature[] signatures = loadSignatures(options.getInputs());
		int n = signatures.length;
		double[][] distance = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j <= i; j++) {
				if (i == j) {
					distance[i][j] = 1.0;
					continue;
				}
				double dist = 1.0 - signatures[i].jaccard(signatures[j]);
				distance[i][j] = dist;
				distance[j][i] = dist;
			}
		}
		String[] names = new String[n];
		for (int i = 0; i < n; i++) {
			names[i] = Integer.toString(i);
		}
		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		List<List<Integer>> clusters = collectBottom(alg.performClustering(distance, names, new AverageLinkageStrategy()), 1.0 - options.getThreshold());
		List<Cluster> genericClusters = new ArrayList<Cluster>();
		for (List<Integer> indices : clusters) {
			Cluster newCluster = new SimpleCluster();
			for (Integer index : indices) {
				newCluster.add(signatures[index]);
			}
			genericClusters.add(newCluster);
		}
		return genericClusters;
	}
	
	private static List<List<Integer>> collectBottom(com.apporiented.algorithm.clustering.Cluster cluster, double cutoff) {
		double distValue = cluster.getDistanceValue();
		List<List<Integer>> list = new ArrayList<>();
		if (distValue < cutoff) {
			list.add(collect(cluster));
		} else {
			for (com.apporiented.algorithm.clustering.Cluster child : cluster.getChildren()) {
				list.addAll(collectBottom(child, cutoff));
			}
		}
		return list;
	}
	
	private static List<Integer> collect(com.apporiented.algorithm.clustering.Cluster cluster) {
		List<Integer> list = new ArrayList<Integer>();
		if (cluster.getChildren().size() == 0) {
			list.add(Integer.parseInt(cluster.getName()));
		} else {
			for (com.apporiented.algorithm.clustering.Cluster child : cluster.getChildren()) {
				list.addAll(collect(child));
			}
		}
		return list;
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
