package de.berlios.suzy.irc.plugin;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.soap.search.GoogleSearch;
import com.google.soap.search.GoogleSearchFault;
import com.google.soap.search.GoogleSearchResult;
import com.google.soap.search.GoogleSearchResultElement;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.Plugin;

/**
 * This plugin allows users to search for expressions on google.
 *
 * <TABLE border="1">
 *   <CAPTION>Commands available</CAPTION>
 *   <TR><TH>google<TD><TD>google for the specified search string
 * </table>
 * * restricted
 *
 * @author honk
 */
public class GooglePlugin implements Plugin {
    private final static String CLIENT_KEY = "foo";

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getCommands()
     */
    public String[] getCommands() {
        return new String[] {
                "google"
        };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getRestrictedCommands()
     */
    public String[] getRestrictedCommands() {
        return new String[] {
        };
    }


    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#handleEvent(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public void handleEvent(IrcCommandEvent ice) {
        if (ice.getMessageContent().length() == 0) {
            ice.getSource().sendMessageTo(ice.getTarget().getUser(), MessageTypes.PRIVMSG, "please specify a search string");
            return;
        }

        String url = "";
        try {
            url = " | All results: http://www.google.com/search?q=" + URLEncoder.encode(ice.getMessageContent(), "ISO-8859-15");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        GoogleSearch s = new GoogleSearch();
        s.setKey(CLIENT_KEY);

        s.setMaxResults(1);
        s.setQueryString(ice.getMessageContent());
        try {
            GoogleSearchResult result = s.doSearch();
            GoogleSearchResultElement[] results = result.getResultElements();
            if (results.length == 0) {
                ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "No results found for "+ice.getMessageContent()+".");
            } else {
                String resultString = results[0].getTitle().replaceAll("<[^>]+>", "") +  " - " + results[0].getURL();
                resultString += url;
                ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, resultString);
            }
        } catch (GoogleSearchFault e) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "There was a problem searching google."+url );
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getHelp(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public String[] getHelp(IrcCommandEvent ice) {
        String message = ice.getMessageContent();

        if (message.equals("googleplugin")) {
            return new String[] {
                    "Sends the given text to google and displays the results.",
            };
        } else if (message.equals("google")){
            return new String[] {
                    "Searches google for the given text.",
                    "Example: "+ice.getPrefix()+"google foobar",
            };
        }

        return null;
    }
}
