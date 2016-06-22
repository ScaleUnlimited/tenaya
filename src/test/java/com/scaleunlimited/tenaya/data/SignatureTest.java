package com.scaleunlimited.tenaya.data;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class SignatureTest {
	
	@Test
	public void testJaccardSelf() {
		Signature one = new Signature(2);
		one.add(2);
		one.add(1);
		assertEquals("signature should have a Jaccard index of 1.0 when compared to itself", 1.0, one.jaccard(one), 0.01);
	}
	
	@Test
	public void testSignatureZeros() {
		Signature one = new Signature(2);
		one.add(0);
		one.add(0);
		one.add(1);
		assertTrue("signature should properly handle hashes of zero", Arrays.equals(one.get(), new long[]{0L, 1L}));
	}
	
	@Test
	public void testSignatureSort() {
		Signature one = new Signature(2);
		one.add(2);
		one.add(1);
		assertTrue("signature should sort the incoming hashes when the size is surpassed", Arrays.equals(one.get(), new long[]{1L, 2L}));
	}
	
	@Test
	public void testSignatureDupBefore() {
		Signature one = new Signature(2);
		one.add(2);
		one.add(2);
		one.add(3);
		assertTrue("signature should ignore duplicates before sorting", Arrays.equals(one.get(), new long[]{2L, 3L}));
	}
	
	@Test
	public void testSignatureAddAll() {
		Signature one = new Signature(2);
		Signature two = new Signature(2);
		one.add(1);
		one.add(2);
		two.add(0);
		two.add(3);
		one.addAll(two);
		assertTrue("signature should properly add all items from another signature", Arrays.equals(one.get(), new long[]{0L, 1L}));
	}

}
