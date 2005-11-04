
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.IrcSender;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.PerformOnConnectPlugin;

/**
 * Auths the irc client on quakenet (perform on connect + command).
 * <br>
 * <br>
 * <TABLE border="1">
 *   <CAPTION><b>Commands available</b></CAPTION>
 *   <TR><TH>auth<TD>*<TD>auths the bot
 * </table>
 * * restricted
 *
 * @author honk
 */
public class QuakenetAuthPlugin implements PerformOnConnectPlugin {
    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getCommands()
     */
    public String[] getCommands() {
        return new String[0];
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getRestrictedCommands()
     */
    public String[] getRestrictedCommands() {
        return new String[] { "auth" };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#handleEvent(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public void handleEvent(IrcCommandEvent ice) {
        auth(ice.getSource());
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.PerformOnConnectPlugin#perform(de.berlios.suzy.irc.IrcSender)
     */
    public void perform(IrcSender ircSender) {
        auth(ircSender);
    }

    private void auth(IrcSender ircSender) {
        Scanner sc;
        try {
            sc = new Scanner(new File("qauth.conf"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        while (sc.hasNext()) {
            String line = sc.nextLine().trim();

            if (!line.startsWith("//") && line.length()!=0) {
                ircSender.sendMessageTo(
                        "Q@CServe.quakenet.org",
                        MessageTypes.PRIVMSG,
                        line
                );
                return;
            }
        }


    }


}
