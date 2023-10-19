package edu.cornell.cs.sam.utils;

import java.util.Collection;

import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class contains several abstract methods
 * for writing XML files.
 */
public class XMLUtils {
	/**
	 * Writes the XML for a node to the output stream
	 */
	public static void writeXML(Node n, PrintWriter out) {
		switch (n.getNodeType()) {
			case Node.DOCUMENT_NODE:
				Document d = (Document) n;
				out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				writeXML(d.getDocumentElement(), out);
				break;
			case Node.ELEMENT_NODE:
				Element e = (Element) n;
				out.print("<" + e.getNodeName());
				NamedNodeMap attrs = e.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++) {
					out.print(" " + attrs.item(i).getNodeName() + "=\"");
					printXML(attrs.item(i).getNodeValue(), out, true);
					out.print("\"");
				}
				out.print(">");

				for (Node child = e.getFirstChild(); child != null; child = child.getNextSibling()) {
					writeXML(child, out);
				}

				out.println("</" + e.getNodeName() + ">");
				break;
			case Node.TEXT_NODE:
				printXML(n.getNodeValue(), out, false);
				break;
		}

		out.flush();
	}

	public static void printXML(String s, PrintWriter out, boolean isAttr) {
		if (s == null) return;
		for (int i = 0; i < s.length(); i++)
			printXML(s.charAt(i), out, isAttr);
	}

	public static void printXML(char c, PrintWriter out, boolean isAttr) {
		switch (c) {
			case '<':
				out.print("&lt;");
				break;
			case '>':
				out.print("&gt;");
				break;
			case '&':
				out.print("&amp;");
				break;
			case '"':
				out.print(isAttr ? "&lt;" : "\"");
				break;
			case '\r':
				out.print("&#xD;");
				break;
			case '\n':
				out.print("&#xA;");
				break;
			case '\t':
				out.print("&#x9;");
				break;
			default:
				out.print(c);
				break;
		}
	}

}
