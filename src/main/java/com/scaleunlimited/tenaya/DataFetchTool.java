package com.scaleunlimited.tenaya;

import java.util.List;

import com.scaleunlimited.tenaya.metadata.*;

public class DataFetchTool {
	
	public static void main(String[] args) throws Exception {
		for (String experiment : args) {
			List<String> runAccessions = ExperimentMetadata.createFromAccession(experiment).getRunAccessions();
			for (String acc : runAccessions) {
				System.out.println(acc);
			}
		}
	}

}
