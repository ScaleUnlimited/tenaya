package com.scaleunlimited.tenaya.cluster;

import com.scaleunlimited.tenaya.data.Signature;

import jsat.linear.Vec;

@SuppressWarnings("serial")
public class SignatureVec extends Vec {
	
	private int size;
	private long[] hashes;
	private String identifier;
	
	private SignatureVec(String name, int size, long[] hashes) {
		this.size = size;
		this.hashes = hashes;
		this.identifier = name;
	}
	
	public static SignatureVec createFromSignature(Signature signature) {
		return new SignatureVec(signature.getIdentifier(), signature.getSize(), signature.get());
	}
	
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public int length() {
		return size;
	}

	@Override
	public double get(int index) {
		return Double.longBitsToDouble(hashes[index]);
	}

	@Override
	public void set(int index, double val) {
		hashes[index] = Double.doubleToLongBits(val);
	}

	@Override
	public boolean isSparse() {
		return false;
	}

	@Override
	public Vec clone() {
		return new SignatureVec(identifier, size, hashes);
	}

}
