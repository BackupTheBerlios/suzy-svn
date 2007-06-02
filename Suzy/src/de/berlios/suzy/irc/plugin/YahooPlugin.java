package de.berlios.suzy.irc.plugin;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;

import com.yahoo.search.SearchClient;
import com.yahoo.search.WebSearchRequest;
import com.yahoo.search.WebSearchResult;
import com.yahoo.search.WebSearchResults;

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
public class YahooPlugin implements Plugin {
    private final static String CLIENT_KEY = "ulAw0C3V34GGWUhsIu.OdNSdGMdK5FS3GBF4uRIh0hYXbTUV7awDGdqTCkJo";

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getCommands()
     */
    public String[] getCommands() {
        return new String[] {
                "yahoo", 
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
            url = " | All results: http://search.yahoo.com/search?p=" + URLEncoder.encode(ice.getMessageContent(), "ISO-8859-15");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // Create the search client. Pass it your application ID.
        SearchClient client = new SearchClient(CLIENT_KEY);

        // Create the web search request.
        WebSearchRequest request = new WebSearchRequest(ice.getMessageContent());
        request.setResults(1);

        try {
            // Execute the search.
            WebSearchResults results = client.webSearch(request);

            if (results.getTotalResultsReturned().compareTo(BigInteger.ZERO) <= 0) {
                ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "No results found for "+ice.getMessageContent()+".");
            } else {
                WebSearchResult result = results.listResults()[0];
                
                String resultString = result.getTitle().replaceAll("<[^>]+>", "") +  " - " + result.getUrl();
                resultString += url;
                
                ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, resultString);
            }
        }
        catch (Exception e) {
            // An issue with the XML or with the service.
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "There was a problem searching yahoo: "+e.toString()+"."+url );
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
