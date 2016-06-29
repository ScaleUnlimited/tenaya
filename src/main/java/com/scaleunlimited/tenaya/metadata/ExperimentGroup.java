package com.scaleunlimited.tenaya.metadata;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

public class ExperimentGroup {
	
	private int retStart;
	private int count;
	private int retMax;
	private int retIndex;
	
	private int[] ids;
	private String baseUrl;
	
	private ExperimentGroup(String baseUrl) throws Exception {
		this.baseUrl = baseUrl;
		
		retStart = 0;
		readNextSet();
	}
	
	private void readNextSet() throws Exception {
		Document doc = XmlHttpReader.getDocumentFromUrl(baseUrl + "&retstart=" + retStart);
		retStart = getIntField(doc, "/eSearchResult/RetStart");
		count = getIntField(doc, "/eSearchResult/Count");
		retMax = getIntField(doc, "/eSearchResult/RetMax");
		retIndex = 0;
		
		int i = 0;
		ids = new int[retMax];
		List list = doc.getRootElement().selectNodes("/eSearchResult/IdList/Id");
		for (Object o : list) {
			Element el = (Element) o;
			ids[i++] = Integer.parseInt(el.getText());
		}
	}
	
	public boolean hasNext() {
		return (retStart + retIndex) < count;
	}
	
	public ExperimentMetadata next() throws Exception {
		if (!hasNext()) return null;
		ExperimentMetadata ret = ExperimentMetadata.createFromId(ids[retIndex++]);
		if (hasNext() && retIndex == retMax) {
			retStart += retMax;
			readNextSet();
		}
		return ret;
	}
	
	private static int getIntField(Document doc, String xpath) {
		Element root = doc.getRootElement();
		Element target = (Element) root.selectSingleNode(xpath);
		return Integer.parseInt(target.getText());
	}
	
	public static ExperimentGroup getExperimentsByOrganism(String organism) throws Exception {
		return new ExperimentGroup(ExperimentMetadata.ENTREZ_SEARCH_URL + "?db=sra&term=" + organism + "[Organism]");
	}

}
