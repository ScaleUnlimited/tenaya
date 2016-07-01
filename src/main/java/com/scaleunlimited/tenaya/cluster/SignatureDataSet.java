package com.scaleunlimited.tenaya.cluster;

import java.util.List;

import com.scaleunlimited.tenaya.data.Signature;

import jsat.DataSet;
import jsat.classifiers.DataPoint;

public class SignatureDataSet extends DataSet {
	
	private Signature[] signatures;
	private int size;
	
	public SignatureDataSet(Signature[] sigs) {
		signatures = sigs;
		size = signatures.length;
	}

	@Override
	public DataPoint getDataPoint(int i) {
		return new DataPoint(SignatureVec.createFromSignature(signatures[i]));
	}

	@Override
	public void setDataPoint(int i, DataPoint dp) {
		throw new UnsupportedOperationException("SignatureDataSet does not support setDataPoint()");
	}

	@Override
	public int getSampleSize() {
		return size;
	}

	@Override
	protected DataSet getSubset(List indicies) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataSet shallowClone() {
		throw new UnsupportedOperationException("SignatureDataSet does not support shallowClone()");
	}

}
