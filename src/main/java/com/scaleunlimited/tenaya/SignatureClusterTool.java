package com.scaleunlimited.tenaya;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jsat.classifiers.DataPoint;
import jsat.clustering.FLAME;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
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
		} else if (method.equals("complex")) {
			clusters = clusterSignaturesComplex(options);
		} else if (method.equals("hierarchical")) {
			clusters = clusterSignaturesHierarchical(options);
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
	
	public static List<Cluster> clusterSignaturesComplex(SignatureClusterToolOptions options) throws IOException {
		Signature[] signatures = loadSignatures(options.getInputs());
		SimpleClusterer clusterer = new SimpleClusterer(options.getThreshold());
		int n = signatures.length;
		int[][] affinityMatrix = new int[n][n];
		Map<String, Integer> pairings = new HashMap<String, Integer>();
		for (int i = 0; i < n; i++) {
			pairings.put(signatures[i].getIdentifier(), i);
		}
		List<Cluster> clusters;
		for (int i = 0; i < options.getN(); i++) {
			shuffle(signatures);
			clusters = clusterer.cluster(Arrays.asList(signatures));
			for (Cluster cluster : clusters) {
				List<Signature> sigs = cluster.getSignatures();
				for (int k = 0; k < sigs.size(); k++) {
					for (int j = 0; j < sigs.size(); j++) {
						int kIndex = pairings.get(sigs.get(k).getIdentifier());
						int jIndex = pairings.get(sigs.get(j).getIdentifier());
						//if (kIndex == jIndex) continue;
						affinityMatrix[kIndex][jIndex] += 1;
					}
				}
			}
		}
		// columns: index, affinity, cluster index
		int[][] maxAffinity = new int[n][3];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int affinity = affinityMatrix[i][j];
				if (affinity > maxAffinity[i][1]) {
					maxAffinity[i][1] = affinity;
					maxAffinity[i][0] = j;
				}
			}
		}
		clusters = new ArrayList<Cluster>();
		for (int i = 0; i < n; i++) {
			maxAffinity[i][2] = -1;
		}
		int currentCluster = -1;
		for (int i = 0; i < n; i++) {
			if (maxAffinity[i][2] == -1) {
				int bestSigIndex = maxAffinity[i][0];
				if (maxAffinity[bestSigIndex][2] != -1) {
					clusters.get(maxAffinity[bestSigIndex][2]).add(signatures[i]);
					maxAffinity[i][2] = maxAffinity[bestSigIndex][2];
				} else {
					Cluster newCluster = new SimpleCluster();
					currentCluster += 1;
					newCluster.add(signatures[i]);
					maxAffinity[i][2] = currentCluster;
					if (maxAffinity[bestSigIndex][0] == i) {
						newCluster.add(signatures[bestSigIndex]);
						maxAffinity[bestSigIndex][2] = currentCluster;
					}
					clusters.add(newCluster);
				}
			}
		}
		return clusters;
	}
	
	private static void shuffle(Object[] a) {
		Random random = new Random();
		Object temp;
		int n = a.length;
		for (int i = 0; i < (n - 1); i++) {
			int j = random.nextInt(i + 1);
			temp = a[i];
			a[i] = a[j];
			a[j] = temp;
		}
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
