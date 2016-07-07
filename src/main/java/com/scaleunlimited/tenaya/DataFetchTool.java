package com.scaleunlimited.tenaya;

import java.util.List;

import com.scaleunlimited.tenaya.metadata.*;

public class DataFetchTool {
	
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			printUsageAndExit();
		}
		for (String experiment : args) {
			List<String> runAccessions = ExperimentMetadata.createFromAccession(experiment).getRunAccessions();
			for (String acc : runAccessions) {
				System.out.println(acc);
			}
		}
	}
	
	public static void printUsageAndExit() {
		System.err.println("Usage: fetch FILE1 FILE2 ...");
		System.exit(-1);
	}

}
