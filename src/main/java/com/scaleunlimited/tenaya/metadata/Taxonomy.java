package com.scaleunlimited.tenaya.metadata;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

public class Taxonomy {
	
	private static Map<Integer, String> scientificNames;
	
	public static String getScientificName(int taxid) throws Exception {
		if (scientificNames == null) {
			scientificNames = new HashMap<Integer, String>();
		}
		if (scientificNames.containsKey(taxid)) {
			return scientificNames.get(taxid);
		}
		Document doc = XmlHttpReader.getDocumentFromUrl(ExperimentMetadata.ENTREZ_SUMMARY_URL + "?db=taxonomy&id=" + taxid);
		Element nameElement = (Element) doc.getRootElement().selectSingleNode("/eSummaryResult/DocSum/Item[@Name='ScientificName']");
		String scientificName = nameElement.getText();
		scientificNames.put(taxid, scientificName);
		return scientificName;
	}

}
