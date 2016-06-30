package com.scaleunlimited.tenaya.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClusterGroupTest {

	@Test
	public void testCluster() {
		Signature sig1 = new Signature(2);
		sig1.add(1);
		sig1.add(2);
		Signature sig2 = new Signature(2);
		sig2.add(2);
		sig2.add(0);
		assertEquals("similarity between half-similar signatures should be 0.5", 0.5, sig1.jaccard(sig2), 0.001);
		ClusterGroup group = new ClusterGroup(0.5);
		group.addSignature(sig1);
		group.addSignature(sig2);
		assertEquals("both signatures should cluster into the same grouping", 1, group.cluster().size());
	}
	
}
