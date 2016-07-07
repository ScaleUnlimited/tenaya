package com.scaleunlimited.tenaya.cluster;

import java.util.List;
import java.util.concurrent.ExecutorService;

import jsat.linear.Vec;
import jsat.linear.distancemetrics.DistanceMetric;

@SuppressWarnings("serial")
public class SignatureDistance implements DistanceMetric {

	public SignatureDistance() {
		
	}
	
	private long[] decodeHashes(Vec a) {
		int len = a.length();
		long[] hashes = new long[len];
		for (int i = 0; i < len; i++) {
			hashes[i] = Double.doubleToLongBits(a.get(i));
		}
		return hashes;
	}

	@Override
	public double dist(Vec a, Vec b) {
		long[] aHashes = decodeHashes(a);
		long[] bHashes = decodeHashes(b);
		int i = 0, j = 0, matches = 0;
		while (i < aHashes.length && j < bHashes.length) {
			if (aHashes[i] < bHashes[j]) {
				i++;
			} else if (aHashes[i] > bHashes[j]) {
				j++;
			} else {
				i++;
				j++;
				matches++;
			}
		}
		double dist = 1.0 - ((double) matches) / ((double) aHashes.length);
		if (dist < 0.15) {
			return 0;
		} else {
			return dist;
		}
	}

	@Override
	public boolean isSymmetric() {
		return true;
	}

	@Override
	public boolean isSubadditive() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isIndiscemible() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double metricBound() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean supportsAcceleration() {
		return false;
	}

	@Override
	public List<Double> getAccelerationCache(List<? extends Vec> vecs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Double> getAccelerationCache(List<? extends Vec> vecs, ExecutorService threadpool) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double dist(int a, int b, List<? extends Vec> vecs, List<Double> cache) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double dist(int a, Vec b, List<? extends Vec> vecs, List<Double> cache) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Double> getQueryInfo(Vec q) {
		return null;
	}

	@Override
	public double dist(int a, Vec b, List<Double> qi, List<? extends Vec> vecs, List<Double> cache) {
		return dist(vecs.get(a), b);
	}

	@Override
	public DistanceMetric clone() {
		throw new UnsupportedOperationException();
	}

}
