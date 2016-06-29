package com.scaleunlimited.tenaya.metadata;

import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.xml.sax.InputSource;

public class ExperimentMetadata {
	
	public static final String SRA_IDENTIFIER_REGEX = "([SE]R[RX][0-9]{6,})";
	
	public static final String ENTREZ_SEARCH_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi";
	public static final String ENTREZ_SUMMARY_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi";
	
	private String experiment;
	private String scientificName;
	private String title;
	private List<String> runAccessions;
	
	private ExperimentMetadata(Document doc) throws Exception {
		Element root = doc.getRootElement();

		runAccessions = new ArrayList<String>();
		List list = root.selectNodes("/eSummaryResult/DocSum/Item/Run");
		for (Object item : list) {
			Element runElement = (Element) item;
			runAccessions.add(runElement.attributeValue("acc"));
		}
		
		Element experimentEl = (Element) root.selectSingleNode("/eSummaryResult/DocSum/Item/Experiment");
		experiment = experimentEl.attributeValue("acc");
		
		Element titleEl = (Element) root.selectSingleNode("/eSummaryResult/DocSum/Item/Summary/Title");
		title = titleEl.getText();
		
		Element orgEl = (Element) root.selectSingleNode("/eSummaryResult/DocSum/Item/Organism");
		int taxid = Integer.parseInt(orgEl.attributeValue("taxid"));
		scientificName = Taxonomy.getScientificName(taxid);
	}
	
	public static ExperimentMetadata createFromAccession(String acc) throws Exception {
		Document document = XmlHttpReader.getDocumentFromUrl(ENTREZ_SEARCH_URL + "?db=sra&term=" + acc);
		Element root = document.getRootElement();
		
		document.write(new PrintWriter(System.out));
		
		Element idElement = (Element) root.selectSingleNode("/eSearchResult/IdList/Id");
		int id = Integer.parseInt(idElement.getText());
		
		return createFromId(id);
	}
	
	public static ExperimentMetadata createFromId(int id) throws Exception {
		return new ExperimentMetadata(XmlHttpReader.getDocumentFromUrl(ENTREZ_SUMMARY_URL + "?db=sra&id=" + id));
	}
	
	public List<String> getRunAccessions() {
		return runAccessions;
	}
	
	public String getScientificName() {
		return scientificName;
	}
	
	public String getExperiment() {
		return experiment;
	}
	
	public String getTitle() {
		return title;
	}

}
