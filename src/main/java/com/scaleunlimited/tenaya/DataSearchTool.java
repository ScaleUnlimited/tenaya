package com.scaleunlimited.tenaya;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.scaleunlimited.tenaya.metadata.ExperimentGroup;
import com.scaleunlimited.tenaya.metadata.ExperimentMetadata;

public class DataSearchTool {
	
	public static void main(String[] args) throws Exception {
		DataSearchToolOptions options = new DataSearchToolOptions();
		CmdLineParser cmdParser = new CmdLineParser(options);
		
		try {
			cmdParser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			printUsageAndExit(cmdParser);
		}
		
		int retCount = options.getCount();
		String query = options.getTerm();
		ExperimentGroup group = ExperimentGroup.getExperimentsByOrganism(query);
		for (int i = 0; i < retCount; i++) {
			ExperimentMetadata meta = group.next();
			for (String run : meta.getRunAccessions()) {
				System.out.println(run);
			}
		}
	}
	
	private static void printUsageAndExit(CmdLineParser parser) {
		parser.printUsage(System.err);
		System.exit(-1);
	}

}
