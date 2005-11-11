package de.berlios.suzy.irc.plugin;


import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

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
 *   <TR><TH>alias<TD>*<TD>Creates an alias (as a command) for the factoid
 * </table>
 * * restricted
 *
 * @author Khalid
 */
public class FactoidPlugin implements Plugin {

	public static final String FACTOIDS_FILE = "factoids.xml";
	private Document doc;
    private DocumentBuilder db;
    private String [] initCommands;
    private String [] commands;
    private String [] restrictedCommands;
    private Map<String, String> aliasMap = new HashMap<String, String>();

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

		restrictedCommands = new String[] {
					                "add",
					                "delete",
					                "replace",
					                "alias"
					                };
		initCommands = new String[] {
		                "see",
		                "list",
		                "tell",
		                "details"
		        		};

		try {
			NodeList list = doc.getElementsByTagName("factoid");
			for (int i = 0; i < list.getLength(); i++) {
				Node factoid = list.item(i);
				Node aliasNode = factoid.getAttributes().getNamedItem("alias");
			 	if (aliasNode != null) {
			 		String alias = aliasNode.getTextContent();
					String name = factoid.getAttributes().getNamedItem("id").getTextContent();
					aliasMap.put(alias, name);
			 	}
			 }
		 } catch (Exception e) {
		 	throw new Exception("Error while trying to initialize aliases", e);
		 }

		 Set<String> aliasSet = aliasMap.keySet();
		 commands = new String[initCommands.length + aliasSet.size()];
		 int i = 0;
		 for (String command : initCommands) {
		 	commands[i++] = command;
		 }
		 for (String command : aliasSet) {
		 	commands[i++] = command;
		 }
	}

    public String[] getCommands() {
		return commands;
    }

    public String[] getRestrictedCommands() {
		return restrictedCommands;
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
    	} else if (command == "alias") {
    		executeAlias(ice);
    	} else {
			findAlias(ice, command);
    	}
    }

    private void sendMessage(IrcCommandEvent ice, String message) {
    	ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(),
    								MessageTypes.PRIVMSG, message);
    }


    /* +see factoid */
    private void executeSee(IrcCommandEvent ice) {
		String name = getFirst(ice);
		executeSee(ice, name);
    }
	private void executeSee(IrcCommandEvent ice, String name) {
		if (name != null) {
			name = name.toLowerCase();
	 		String factoid = getFactoid(name);
	 		if (factoid != null) {
	 			sendMessage(ice, name+" is: "+factoid);
	 		} else {
	 			sendMessage(ice, "Factoid '"+name+"' not found.");
	 		}
	 	} else {
	 		sendMessage(ice, "Please specify a factoid.");
	 	}
	}

	/* +tell nickname: factoid */
    private void executeTell(IrcCommandEvent ice) {
     	String nickname = getFirst(ice);
     	String name = getSecond(ice);
     	executeTell(ice, name, nickname);
	}
	private void executeTell(IrcCommandEvent ice, String name, String nickname) {
		if (name != null) {
			name = name.toLowerCase();
			String factoid = getFactoid(name);
	 		if (factoid != null) {
	 			if (nickname == null) {
	 				sendMessage(ice, "Please specify a nickname or use the see command.");
	 			} else {
		 			sendMessage(ice, nickname+", "+name+" is: "+factoid);
	 			}
	 		} else {
	 			sendMessage(ice, "Factoid '"+name+"' not found.");
	 		}
	 	} else {
	 		sendMessage(ice, "Please specify a factoid.");
	 	}
    }

	/* +details factoid */
    private void executeDetails(IrcCommandEvent ice) {
     	String name = getFirst(ice);
     	if (name != null) {
     	 	Element e = doc.getElementById(name);
	 		if (e != null) {
	 			StringBuilder message = new StringBuilder();
	 			message.append("Factoid details (");
	 			message.append(name);
	 			message.append("): ");
				message.append("Contents: ");
				message.append(e.getFirstChild().getNodeValue());
	 			message.append(" | Added By: ");
	 			message.append(e.getAttribute("user"));
	 			message.append(" | Time Added: ");
	 			message.append(e.getAttribute("time"));
	 			if (e.getAttribute("alias") != null) {
	 				message.append(" | Alias: ");
	 				message.append(e.getAttribute("alias"));
	 			}
	 			sendMessage(ice, message.toString());
	 		} else {
	 			sendMessage(ice, "Factoid '"+name+"' not found.");
	 		}
     	} else {
	 		sendMessage(ice, "Please specify a factoid.");
     	}

    }

    private void executeList(IrcCommandEvent ice) {
		 NodeList list = doc.getElementsByTagName("factoid");
		 StringBuilder buffer = new StringBuilder("Available factoids: ");
		 for (int i = 0; i < list.getLength(); i++) {
		 	Node factoid = list.item(i);
		 	buffer.append(factoid.getAttributes().getNamedItem("id").getTextContent());
		 	if (i != list.getLength() - 1) {
		 		buffer.append(", ");
		 	}
		 }
		 sendMessage(ice, buffer.toString());
    }

	/* +add factoid: contents */
    public void executeAdd(IrcCommandEvent ice) {
		String name = getFirst(ice);
		if (name == null) {
    		sendMessage(ice, "Please specify a factoid name.");
    		return;
		}
    	name = name.toLowerCase();
    	Element e = doc.getElementById(name);
    	if (e != null && e.getFirstChild() != null) {
    		sendMessage(ice, "Factoid '"+name+"' already exists.");
    		return;
    	}
    	String value = getSecond(ice);
		if (value == null) {
    		sendMessage(ice, "Please specify factoid contents.");
    		return;
		}
		Node parent = doc.getElementsByTagName("factoids").item(0);
		Element child = doc.createElement("factoid");
		child.setAttribute("id", name);
		child.setAttribute("time", Calendar.getInstance().getTime().toString());
		child.setAttribute("user", ice.getTarget().getUser());
		child.setTextContent(value);
		parent.appendChild(child);
		updateXml(ice);
		sendMessage(ice, "Factoid '"+name+"' successfully added.");
    }

	/* +delete factoid */
    public void executeDelete(IrcCommandEvent ice)  {
     	String name = getFirst(ice);
     	if (name != null) {
     		name = name.toLowerCase();
			Element e = doc.getElementById(name);
			if (e != null) {
				e.getParentNode().removeChild(e);
				updateXml(ice);
				sendMessage(ice, "Factoid '"+name+"' successfully deleted.");
			} else {
				sendMessage(ice, "Factoid '"+name+"' does not exist.");
			}
		} else {
			sendMessage(ice, "Please specify a factoid name.");
		}
    }

	/* +replace factoid: contents */
    public void executeReplace(IrcCommandEvent ice)  {
		String name = getFirst(ice);
		if (name == null) {
			sendMessage(ice, "Please specify a factoid name.");
			return;
		}
		name = name.toLowerCase();
		Element e = doc.getElementById(name);
		if (e == null) {
			sendMessage(ice, "Factoid '"+name+"' does not exist.");
		}
		String value = getSecond(ice);
		if (value == null) {
    		sendMessage(ice, "Please specify the new factoid contents.");
    		return;
		}
		e.setTextContent(value);
		e.setAttribute("time", Calendar.getInstance().getTime().toString());
		e.setAttribute("user", ice.getTarget().getUser());
		updateXml(ice);
		sendMessage(ice, "Factoid '"+name+"' successfully replaced.");
    }

    /* +alias factoid: alias */
    public void executeAlias(IrcCommandEvent ice)  {
		String name = getFirst(ice);
		if (name == null) {
			sendMessage(ice, "Please specify a factoid name.");
			return;
		}
		name = name.toLowerCase();
		Element e = doc.getElementById(name);
		if (e == null) {
			sendMessage(ice, "Factoid '"+name+"' does not exist.");
			return;
		}
		String alias = getSecond(ice);
		if (alias == null) {
    		sendMessage(ice, "Please specify the factoid alias.");
    		return;
		}
		if (alias.indexOf(' ') != -1) {
    		sendMessage(ice, "An alias can consist of one word only.");
    		return;
		}
		for (String command : commands) {
			if (command.equals(alias)) {
				sendMessage(ice, "A command or alias with this name already exists, please try another name.");
				return;
			}
		}
		e.setAttribute("alias", alias);
		updateXml(ice);
		sendMessage(ice, "Factoid '"+name+"' can now be accessed using the alias '"+alias+"' after reloading the plugin. ");
    }

	public void findAlias(IrcCommandEvent ice, String command) {
		    String commandForAlias = aliasMap.get(command);
    		if (commandForAlias != null) {
    			String nickname = getFirst(ice);
    			if (nickname == null) {
    				executeSee(ice, commandForAlias);
    			} else {
    				executeTell(ice, commandForAlias, nickname);
    			}
    		}
    }

	private String getFactoid(String name) {
		try {
			Element e = doc.getElementById(name);
			System.out.println(e);
	 		if (e != null) {
	 			return e.getFirstChild().getNodeValue();
	 		} else {
	 			return null;
	 		}
	 	} catch (Exception e) {
	 		return null;
	 	}
	}
    private void updateXml(IrcCommandEvent ice) {
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

	private String getFirst(IrcCommandEvent ice) {
		if (ice.getMessageContent().length() == 0) {
			return null;
		}
		int index = ice.getMessageContent().indexOf(":");
		if (index == -1) {
			return ice.getMessageContent().trim();
		} else {
			String first = ice.getMessageContent().substring(0, index).trim();
			if (first.equals("")) {
				return null;
			}
			return first;
		}
	}
	private String getSecond(IrcCommandEvent ice) {
		int index = ice.getMessageContent().indexOf(":");
		if (index == -1) {
			return null;
		} else {
			return ice.getMessageContent().substring(index + 1).trim();
		}
	}

    public String[] getHelp(IrcCommandEvent ice) {
        // TODO Auto-generated method stub
        return null;
    }
}