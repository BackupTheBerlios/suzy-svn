
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
    private final static String CLIENT_KEY = "key";

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
                try {
                    resultString += " | All results: http://www.google.com/search?q=" + URLEncoder.encode(ice.getMessageContent(), "ISO-8859-15");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, resultString);
            }
        } catch (GoogleSearchFault e) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, e.getMessage());
            e.printStackTrace();
        }
    }
}
