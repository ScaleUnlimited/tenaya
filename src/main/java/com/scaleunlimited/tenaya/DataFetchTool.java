package com.scaleunlimited.tenaya;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class DataFetchTool {
	
	public static class ItemXmlHandler extends DefaultHandler {
		
		private boolean inRunElement = false;
		private String runContent = "";
		private List<String> runIdentifiers;
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if (qName.equals("Item") && attributes.getValue("Name").equals("Runs")) {
				inRunElement = true;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equals("Item") && inRunElement) {
				inRunElement = false;
				Reader reader = new StringReader("<Wrapper>" + runContent + "</Wrapper>");
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser parser;
				try {
					parser = factory.newSAXParser();
					XMLReader xmlReader = parser.getXMLReader();
					RunXmlHandler runHandler = new RunXmlHandler();
					xmlReader.setContentHandler(runHandler);
					xmlReader.parse(new InputSource(reader));
					runIdentifiers = runHandler.getRuns();
				} catch (ParserConfigurationException | IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (inRunElement) {
				String raw = String.copyValueOf(ch, start, length);
				runContent += raw;
			}
		}
		
		public List<String> getRuns() {
			return runIdentifiers;
		}
		
	}
	
	public static class RunXmlHandler extends DefaultHandler {

		private List<String> runIdentifiers;
		
		public RunXmlHandler() {
			runIdentifiers = new ArrayList<String>();
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if (qName.equals("Run")) {
				runIdentifiers.add(attributes.getValue("acc"));
			}
		}
		
		public List<String> getRuns() {
			return runIdentifiers;
		}
		
	}
	
	public static class SearchXmlHandler extends DefaultHandler {

		private boolean inIdElement = false;
		private int id;
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equals("Id")) {
				inIdElement = false;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (inIdElement) {
				id = Integer.parseInt(String.copyValueOf(ch, start, length));
			}
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if (qName.equals("Id")) {
				inIdElement = true;
			}
		}
		
		public int getId() {
			return id;
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		for (String experiment : args) {
			URL searchUrl = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=sra&term=" + experiment);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			XMLReader xmlReader = parser.getXMLReader();
			SearchXmlHandler searchHandler = new SearchXmlHandler();
			xmlReader.setContentHandler(searchHandler);
			xmlReader.parse(new InputSource(searchUrl.openStream()));
			int id = searchHandler.getId();
			URL summaryUrl = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=sra&id=" + id);
			ItemXmlHandler summaryHandler = new ItemXmlHandler();
			xmlReader.setContentHandler(summaryHandler);
			xmlReader.parse(new InputSource(summaryUrl.openStream()));
			List<String> runs = summaryHandler.getRuns();
			for (String run : runs) {
				System.out.println(run);
			}
		}
	}

}
