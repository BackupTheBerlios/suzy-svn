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
 *   <TR><TH>helpuser<TD>*<TD>sends a predefined help text to the specified user
 * </table>
 * * restricted
 *
 * @author honk
 */
public class HelpPlugin implements Plugin {
    private static final String[] HELP_TEXT = new String[] {
        "Important commands: $$prefix$$help, $$prefix$$api $$prefix$$class $$prefix$$method $$prefix$$commands (admin-only: $$prefix$$allcommands)",
        "Try \"$$prefix$$api String\" to browse search the api for string. Use \"$$prefix$$commands\" to show all commands available. \"$$prefix$$help help\" to get more help.",
        "Note: I will also answer you in a query, please use this to avoid spam in the channel."
    };


    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getCommands()
     */
    public String[] getCommands() {
        return new String[] {
                "help"
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
        } else if (ice.getCommand().equals("helpuser")) {
            helpuser(ice);
        }
    }


    private void helpuser(IrcCommandEvent ice) {
        for (String s: HELP_TEXT) {
            s = s.replaceAll("\\$\\$prefix\\$\\$", ice.getPrefix());
            ice.getSource().sendMessageTo(ice.getMessageContent(), MessageTypes.PRIVMSG, s);
        }
    }

    private void help(IrcCommandEvent ice) {
        String[] helpText = null;
        String message = ice.getMessageContent().toLowerCase().trim();

        if (message.length() == 0) {
            helpText = HELP_TEXT;
        } else {
            System.out.println(ice.getSource().getCompletePluginList().size());

            Plugin p = ice.getSource().getCompletePluginList().get(message);
            if (p != null) {
                helpText = p.getHelp(ice);
            } else {
                for (Plugin p2: ice.getSource().getCompletePluginList().values()) {
                    if (p2.getClass().getSimpleName().toLowerCase().equals(message)) {
                        helpText = p2.getHelp(ice);
                        break;
                    }
                }
            }
        }

        if (helpText == null) {
            ice.getSource().sendMessageTo(ice.getTarget().getUser(), MessageTypes.PRIVMSG, "No help available for "+message);
            return;
        }

        for (String s: helpText) {
            s = s.replaceAll("\\$\\$prefix\\$\\$", ice.getPrefix());
            ice.getSource().sendMessageTo(ice.getTarget().getUser(), MessageTypes.PRIVMSG, s);
        }
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getHelp(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public String[] getHelp(IrcCommandEvent ice) {
        String message = ice.getMessageContent();

        if (message.equals("helpplugin")) {
            return new String[] {
                    "Sends help to the user if requested.",
                    "See "+ice.getPrefix()+"help and "+ice.getPrefix()+"helpuser"
            };
        } else if (message.equals("help")){
            return new String[] {
                    "Search help for the specified command or plugin.",
                    "Example: "+ice.getPrefix()+"help api",
                    "If no text is given, general help will be sent"
            };
        } else if (message.equals("helpuser")){
            return new String[] {
                    "Search usage help to the specified user.",
                    "Example: "+ice.getPrefix()+"helpuser Bob",
            };
        }

        return null;
    }

}
