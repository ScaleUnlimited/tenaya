package com.scaleunlimited.tenaya.metadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.crimson.jaxp.SAXParserFactoryImpl;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XmlHttpReader {
	
	private static CloseableHttpClient httpClient;
	
	public static Document getDocumentFromUrl(String url) throws Exception {
		if (httpClient == null) {
			Logger.getLogger("org.apache.http").setLevel(Level.OFF);
			httpClient = HttpClients.createDefault();
		}
		HttpGet searchUrl = new HttpGet(url);
		String contents = EntityUtils.toString(httpClient.execute(searchUrl).getEntity()).replaceAll("&gt;", ">").replaceAll("&lt;", "<");
		InputStream inputStream = new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
		
		SAXParserFactory factory = SAXParserFactoryImpl.newInstance();
		SAXParser parser = factory.newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		
		SAXReader reader = new SAXReader(xmlReader, false);
		Document document = reader.read(inputStream);
		return document;
	}

}
