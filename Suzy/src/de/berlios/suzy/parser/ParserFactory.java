package de.berlios.suzy.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Creates ApiParsers according to config file <b>parsers.xml</b>
 * 
 * @author Antubis
 * 
 * @see ApiParser
 */
public class ParserFactory {

	private static final String DEFAULT_CONFIG = "parsers.xml";

	private static final String PARSER_ELEMENT = "parser";

	private static ParserFactory instance;

	private static HashMap<String, ApiParser> parserMap;

	protected ParserFactory() {
		loadConfig();
	}

	private void loadConfig() {
		try {
			// no idea why needed but w/o on test it failed
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
            "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document doc = builder.parse(new File(DEFAULT_CONFIG));
			NodeList parserElements = doc.getElementsByTagName(PARSER_ELEMENT);
			createParsers(parserElements);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void createParsers(NodeList parserElements) {
		parserMap = new HashMap<String, ApiParser>(parserElements.getLength(),
				1);
		for (int i = 0; i < parserElements.getLength(); i++) {
			// <Parser name="NAME" fileName="FILENAME">
			Element element = (Element) parserElements.item(i);
			String name = element.getAttribute("name");
			if (name == null)
				name = "default";
			String fileName = element.getAttribute("fileName");
			if (fileName == null)
				fileName = name + ".dat";
			NodeList apis = element.getElementsByTagName("api");
			List<ApiParser.ParseEntry> config = new ArrayList<ApiParser.ParseEntry>();
			for (int j = 0; j < apis.getLength(); j++) {
				// <Api>
				element = (Element) apis.item(j);
				// dirty but ok
				String src = element.getElementsByTagName("src").item(0)
						.getTextContent();
				String packages = element.getElementsByTagName("packages")
						.item(0).getTextContent();
				String baseUrl = element.getElementsByTagName("base").item(0)
						.getTextContent();
				if (!baseUrl.endsWith("/")) {
					baseUrl = baseUrl + "/";
				}
				config.add(new ApiParser.ParseEntry(src, packages, baseUrl));
			}
			parserMap.put(name.toLowerCase(), new ApiParser(fileName, config));
		}
	}

	public static synchronized ParserFactory getInstance() {
		if (instance == null) {
			instance = new ParserFactory();
		}
		return instance;
	}

	public void reload() {
		parserMap.clear();
		loadConfig();
	}

	public ApiParser getParser(String name) {
		if (name == null)
			name = "default";
		return parserMap.get(name.toLowerCase());
	}

	public Set<String> getParserNames() {
		return Collections.unmodifiableSet(parserMap.keySet());
	}

	public boolean supportsParserName(String name) {
		if (name == null)
			name = "default";
		return parserMap.containsKey(name.toLowerCase());
	}
}
