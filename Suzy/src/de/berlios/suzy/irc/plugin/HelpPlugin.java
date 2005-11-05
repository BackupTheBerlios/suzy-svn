package de.berlios.suzy.irc.plugin;



import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.Plugin;


/**
 * This plugin will send some basic help whenever the help command is triggered.
 * <br>
 * <br>
 * <TABLE border="1">
 *   <CAPTION><b>Commands available</b></CAPTION>
 *   <TR><TH>help<TD><TD>sends a predefined help text
 *   <TR><TH>tutorial<TD><TD>prints out the tutorial-url
 *   <TR><TH>ask<TD><TD>gives a message saying that one should not ask to ask
 *   <TR><TH>download<TD><TD>prints out the url to the java download
 *   <TR><TH>helpuser<TD>*<TD>sends a predefined help text to the specified user
 * </table>
 * * restricted
 *
 * @author honk
 */
public class HelpPlugin implements Plugin {
    private static final String[] HELP_TEXT = new String[] {
        "Important commands: $$prefix$$api $$prefix$$class $$prefix$$method $$prefix$$commands (admin-only: $$prefix$$allcommands)",
        "Try \"$$prefix$$api String\" to browse search the api for string. Use \"$$prefix$$commands\" to show all commands available.",
        "Note: I will also answer you in a query, please use this to avoid spam in the channel."
    };


    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getCommands()
     */
    public String[] getCommands() {
        return new String[] {
                "help",
                "ask",
                "tutorial",
                "download"
        };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getRestrictedCommands()
     */
    public String[] getRestrictedCommands() {
        return new String[] {
                "helpuser"
        };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#handleEvent(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public void handleEvent(IrcCommandEvent ice) {
        if (ice.getCommand().equals("help")) {
            help(ice);
        } else if (ice.getCommand().equals("tutorial")) {
            tutorial(ice);
        } else if (ice.getCommand().equals("helpuser")) {
            helpuser(ice);
        } else if (ice.getCommand().equals("download")) {
            download(ice);
        } else if (ice.getCommand().equals("ask")) {
            ask(ice);
        }
    }


    private void helpuser(IrcCommandEvent ice) {
        for (String s: HELP_TEXT) {
            s = s.replaceAll("\\$\\$prefix\\$\\$", ice.getPrefix());
            ice.getSource().sendMessageTo(ice.getMessageContent(), MessageTypes.PRIVMSG, s);
        }
    }

    private void ask(IrcCommandEvent ice) {
        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "Don't ask to ask, just ask.");
    }

    private void tutorial(IrcCommandEvent ice) {
        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "http://java.sun.com/docs/books/tutorial/");
    }

    private void download(IrcCommandEvent ice) {
        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "http://java.sun.com/j2se/1.5.0/download.jsp");
    }

    private void help(IrcCommandEvent ice) {
        for (String s: HELP_TEXT) {
            s = s.replaceAll("\\$\\$prefix\\$\\$", ice.getPrefix());
            ice.getSource().sendMessageTo(ice.getTarget().getUser(), MessageTypes.PRIVMSG, s);
        }
    }
}
