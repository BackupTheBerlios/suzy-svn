package de.berlios.suzy.irc.plugin;


import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.Plugin;

/**
 * Allows the bot to respond with factoids.
 *
 * * <TABLE border="1">
 * <CAPTION>Commands available</CAPTION>
 *   <TR><TH>see<TD><TD>View a factoid
 *	 <TR><TH>list<TD><TD>List factoids
 *   <TR><TH>tell<TD><TD>Tell a specific user about a factoid
 *   <TR><TH>details<TD><TD>Display factoid details
 *   <TR><TH>add<TD>*<TD>Add a factoid
 *   <TR><TH>delete<TD>*<TD>Delete a factoid
 *   <TR><TH>replace<TD>*<TD>Replace a factoid
 * </table>
 * * restricted
 *
 * @author Khalid
 */
public class FactoidPlugin implements Plugin {

	public static final String FACTOIDS_FILE = "factoids.xml";
	private Document doc;
    private DocumentBuilder db;

	public FactoidPlugin () throws Exception {
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");

		DocumentBuilderFactory dbf= DocumentBuilderFactory.newInstance();
		try {
			db = dbf.newDocumentBuilder();
			doc = db.parse(FACTOIDS_FILE);
		} catch (Exception e) {
			throw new Exception("Error while attempting to parse XML.");
		}
		if (doc == null) {
			throw new Exception("Error while attempting to parse XML.");
		}
	}

    public String[] getCommands() {
        return new String[] {
                "see",
                "list",
                "tell",
                "details",
                "factoid-help"
        };
    }

    public String[] getRestrictedCommands() {
        return new String[] {
                "add",
                "delete",
                "replace"
        };
    }

    public void handleEvent(IrcCommandEvent ice) {
    	String command = ice.getCommand().intern();
    	if (command == "see") {
    		executeSee(ice);
    	} else if (command == "tell") {
    		executeTell(ice);
    	} else if (command == "details") {
    		executeDetails(ice);
    	} else if (command == "list") {
    		executeList(ice);
    	} else if (command == "add") {
    		executeAdd(ice);
    	} else if (command == "delete") {
    		executeDelete(ice);
    	} else if (command == "replace") {
    		executeReplace(ice);
    	} else if (command == "factoid-help") {
    		executeHelp(ice);
    	}
    }

    public void sendMessage(IrcCommandEvent ice, String message) {
    	ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(),
    								MessageTypes.PRIVMSG, message);
    }

    public void sendNotice(IrcCommandEvent ice, String message) {
    	ice.getSource().sendMessageTo(ice.getTarget().getUser(),
    								MessageTypes.NOTICE, message);
    }

    public void executeSee(IrcCommandEvent ice) {
     	String id = ice.getMessageContent().toLowerCase();;
 		Element e = doc.getElementById(id);
 		if (e != null) {
 			sendMessage(ice, id+" is: "+e.getFirstChild().getNodeValue());
 		} else {
 			sendMessage(ice, "Factoid "+id+" not found.");
 		}
    }

    public void executeTell(IrcCommandEvent ice) {
     	int index = ice.getMessageContent().indexOf("about");
     	String nickname = ice.getMessageContent().substring(0, index - 1);
     	String id = ice.getMessageContent().substring(index + 6).toLowerCase();
 		Element e = doc.getElementById(id);
 		if (e != null) {
 			sendMessage(ice, nickname+", "+id+" is: "+e.getFirstChild().getNodeValue());
 		} else {
 			sendMessage(ice, "Factoid "+id+" not found.");
 		}
    }

    public void executeDetails(IrcCommandEvent ice) {
     	String id = ice.getMessageContent().toLowerCase();;
 		Element e = doc.getElementById(id);
 		if (e != null) {
 			sendNotice(ice, "Factoid details ("+id+"):");
 			sendNotice(ice, "- Contents: " +e.getFirstChild().getNodeValue());
 			sendNotice(ice, "- Added By: " +e.getAttribute("user"));
 			sendNotice(ice, "- Time Added: " +e.getAttribute("time"));
 		} else {
 			sendNotice(ice, "Factoid "+id+" not found.");
 		}
    }

    public void executeList(IrcCommandEvent ice) {
		 NodeList list = doc.getElementsByTagName("factoid");
		 StringBuffer buffer = new StringBuffer();
		 sendNotice(ice, "Available Factoids:");
		 for (int i = 0; i < list.getLength(); i++) {
		 	Node factoid = list.item(i);
		 	buffer.append(factoid.getAttributes().getNamedItem("id").getTextContent());
		 	if (buffer.length() > 100) {
		 		sendNotice(ice, new String(buffer));
		 		buffer.delete(0, buffer.length());
		 	} else if (i != list.getLength() - 1) {
		 		buffer.append(" - ");
		 	}
		 }
		 if (buffer.length() > 0) {
		 	sendNotice(ice, new String(buffer));
		 }
    }

    public void executeAdd(IrcCommandEvent ice) {
    	int index = ice.getMessageContent().indexOf(" is ");
    	String id = ice.getMessageContent().substring(0, index).toLowerCase();
    	Element e = doc.getElementById(id);
    	if (e != null && e.getFirstChild() != null) {
    		sendMessage(ice, "Factoid "+id+" already exists.");
    		return;
    	}
    	String value = ice.getMessageContent().substring(index + 2);
		Node parent = doc.getElementsByTagName("factoids").item(0);
		Element child = doc.createElement("factoid");
		child.setAttribute("id", id);
		child.setAttribute("time", Calendar.getInstance().getTime().toString());
		child.setAttribute("user", ice.getTarget().getUser());
		child.setTextContent(value);
		parent.appendChild(child);
		updateXml(ice);
		sendMessage(ice, "Factoid "+id+" sucessfully added.");
    }

    public void executeDelete(IrcCommandEvent ice)  {
     	String id = ice.getMessageContent().toLowerCase();
		Element e = doc.getElementById(id);
		if (e != null) {
			e.getParentNode().removeChild(e);
			updateXml(ice);
			sendMessage(ice, "Factoid "+id+" sucessfully deleted.");
		} else {
			sendMessage(ice, "Factoid "+id+" does not exist.");
		}
    }

    public void executeReplace(IrcCommandEvent ice)  {
    	int index = ice.getMessageContent().indexOf("with");
    	String id = ice.getMessageContent().substring(0, index - 1).toLowerCase();
		Element e = doc.getElementById(id);
		if (e != null) {
			String value = ice.getMessageContent().substring(index + 5);
			e.setTextContent(value);
			updateXml(ice);
			sendMessage(ice, "Factoid "+id+" sucessfully replaced.");
		} else {
			sendMessage(ice, "Factoid "+id+" does not exist.");
		}
    }

    public void executeHelp(IrcCommandEvent ice)  {
		sendNotice(ice, "Available commands: (Will add later!)");
    }

    public void updateXml(IrcCommandEvent ice) {
	   try
	   {
	     Transformer transformer = TransformerFactory.newInstance().newTransformer();
	     transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"factoids.dtd");;
	   	 transformer.transform(new DOMSource(doc), new StreamResult(FACTOIDS_FILE));
	     doc = db.parse(FACTOIDS_FILE);
	   } catch(Exception e) {
	   		sendMessage(ice, "Failed to update the XML document.");
	   }
	}
}