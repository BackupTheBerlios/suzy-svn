import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.Plugin;

/**
 * Test plugin which replies to test and admin text with a short message.
 *
 * <TABLE border="1">
 *   <CAPTION>Commands available</CAPTION>
 *   <TR><TH>test<TD><TD>sends some text
 *   <TR><TH>admintest<TD>*<TD>sends some text
 * </table>
 * * restricted
 *
 * @author honk
 */
public class TestPlugin implements Plugin {

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getCommands()
     */
    public String[] getCommands() {
        return new String[] {
                "test"
        };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getRestrictedCommands()
     */
    public String[] getRestrictedCommands() {
        return new String[] {
                "admintest"
        };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#handleEvent(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public void handleEvent(IrcCommandEvent ice) {
        if (ice.getCommand().equals("test")) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "moo!");
        } else if (ice.getCommand().equals("admintest")) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "fierce moo!");
        }
    }
}
