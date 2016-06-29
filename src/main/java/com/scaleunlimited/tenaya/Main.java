package com.scaleunlimited.tenaya;

import java.util.Arrays;

public class Main {
	
	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			String action = args[0].toLowerCase();
			String[] modifiedArgs = Arrays.copyOfRange(args, 1, args.length);
			if (action.equals("compare")) {
				SignatureComparisonTool.main(modifiedArgs);
			} else if (action.equals("generate")) {
				SignatureGenerationTool.main(modifiedArgs);
			} else if (action.equals("fetch")) {
				DataFetchTool.main(modifiedArgs);
			} else if (action.equals("search")) {
				DataSearchTool.main(modifiedArgs);
			} else if (action.equals("cluster")) {
				SignatureClusterTool.main(modifiedArgs);
			} else {
				System.err.println("Tool failed: unknown action '" + action + "'");
			}
		}
	}

}
