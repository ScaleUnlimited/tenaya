package com.scaleunlimited.tenaya;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.scaleunlimited.tenaya.metadata.*;

public class DataFetchTool {
	
	public static void main(String[] args) throws Exception {
		Logger.getLogger("org.apache.http").setLevel(Level.OFF);
		for (String experiment : args) {
			List<String> runAccessions = ExperimentMetadata.createFromAccession(experiment).getRunAccessions();
			for (String acc : runAccessions) {
				System.out.println(acc);
			}
		}
	}

}
