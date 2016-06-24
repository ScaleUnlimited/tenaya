package com.scaleunlimited.tenaya;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import com.scaleunlimited.tenaya.data.Signature;

public class SignatureComparisonTool {
	
	public static void main(String[] args) {
		try {
			compareSignatures(args);
		} catch (Throwable t) {
			System.err.println("Tool failed: " + t.getMessage());
			t.printStackTrace();
			System.exit(-1);
		}
	}

	public static void compareSignatures(String[] sigs) throws IOException {
		int n = sigs.length;
		Signature[] signatures = new Signature[n];
		for (int i = 0; i < n; i++) {
			System.out.println("Loading signature " + sigs[i]);
			signatures[i] = Signature.createFromFile(new File(sigs[i]));
		}
		double[][] similarity = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j <= i; j++) {
				double sim = signatures[i].jaccard(signatures[j]);
				similarity[i][j] = sim;
				if (i != j) {
					similarity[j][i] = sim;
				}
			}
		}
		DecimalFormat format = new DecimalFormat("0.000");
		System.out.println("Similarity Matrix:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print(format.format(similarity[i][j]) + "\t");
			}
			System.out.println();
		}
	}

}
