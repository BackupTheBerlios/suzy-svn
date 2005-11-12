package de.berlios.suzy.irc.plugin;


import java.text.DecimalFormat;
import java.util.Set;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.Plugin;
import de.berlios.suzy.parser.RequestHandler;


/**
 * Supplies commands to access the functionality implemented by {@link RequestHandler}.
 * This will allow users to access the api search functions.
 * <br>
 * <br>
 * <TABLE border="1">
 *   <CAPTION><b>Commands available</b></CAPTION>
 *   <TR><TH>api<TD><TD>search all information available (methods and classes) for the given term
 *   <TR><TH>class<TD><TD>search classes only for the given term
 *   <TR><TH>method<TD><TD>search methods only for the given term
 *   <TR><TH>apistat<TD><TD>return short information about the last search (execution time)
 *   <TR><TH>apireload<TD>*<TD>causes the api to be reload
 * </table>
 * * restricted
 *
 * @author honk
 */
public class ApiPlugin implements Plugin {
    private long start;
    private long stop;
    private enum LastRequest {
        ALL, CLASSES, METHODS, NONE
    };
    private LastRequest lastRequest = LastRequest.NONE;


    /**
     * Causes the underlying RequestHandler to load an instance of the api data.
     */
    public ApiPlugin() {
        RequestHandler.getInstance();   //Initialise
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getCommands()
     */
    public String[] getCommands() {
        return new String[] {
                "api",
                "class",
                "method",
                "apistat"
        };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getRestrictedCommands()
     */
    public String[] getRestrictedCommands() {
        return new String[] {
                "apireload"
        };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#handleEvent(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public void handleEvent(IrcCommandEvent ice) {
        if (ice.getCommand().equals("apistat")) {
            stats(ice);
            return;
        } else if (ice.getCommand().equals("apireload")) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "Reloading api.");
            RequestHandler.getInstance().reload();
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "The api was reloaded.");
            return;
        }


        if (ice.getMessageContent().length() == 0) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "http://java.sun.com/j2se/1.5.0/docs/api/");
            return;
        }

        start = System.nanoTime();
        if (ice.getCommand().equals("api")) {
            api(ice);
        } else if (ice.getCommand().equals("class")) {
            classes(ice);
        } else if (ice.getCommand().equals("method")) {
            methods(ice);
        }
        stop = System.nanoTime();

    }

    private void stats(IrcCommandEvent ice) {
        if (lastRequest == LastRequest.NONE) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "No previous query.");
            return;
        }

        int methods = RequestHandler.getInstance().methodCount();
        int classes = RequestHandler.getInstance().classCount();

        long diff = stop - start;
        String time = (diff/1000)+" µs";

        String answer = "";
        int total = 0;
        switch (lastRequest) {
            case ALL: answer = "Searched "+classes+" classes and "+methods+" methods in "+time; total = classes+methods; break;
            case CLASSES: answer = "Searched "+classes+" classes in "+time; total = classes; break;
            case METHODS: answer = "Searched "+methods+" methods in "+time; total = methods; break;
        }

        double perItem = diff/(1000.*total);
        String perItemString = new DecimalFormat("0.00 µs").format(perItem);

        answer += " ("+perItemString+" / item).";


        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, answer);
    }

    private void api(IrcCommandEvent ice) {
        Set<String> matches = RequestHandler.getInstance().parseAll(ice.getMessageContent());
        lastRequest = LastRequest.ALL;
        reply(ice, matches);
    }

    private void classes(IrcCommandEvent ice) {
        Set<String> matches = RequestHandler.getInstance().parseClasses(ice.getMessageContent());
        lastRequest = LastRequest.CLASSES;
        reply(ice, matches);
    }

    private void methods(IrcCommandEvent ice) {
        Set<String> matches = RequestHandler.getInstance().parseMethods(ice.getMessageContent());
        lastRequest = LastRequest.METHODS;
        reply(ice, matches);
    }


    private void reply(IrcCommandEvent ice, Set<String> matches) {
        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, format(matches));
    }

    private String format(Set<String> matches) {
        if (matches.size() == 0) {
            return "No matches found.";
        }

        StringBuilder sb = new StringBuilder();
        for (String match: matches) {
            if (match.length() + sb.length() > 300) {
                sb.append(" (total: ");
                sb.append(matches.size());
                sb.append(")");
                break;
            }
            if (sb.length() != 0) {
                sb.append(" | ");
            }
            sb.append(match);
        }

        return sb.toString();
    }


    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getHelp(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public String[] getHelp(IrcCommandEvent ice) {
        String message = ice.getMessageContent();

        if (message.equals("apiplugin")) {
            return new String[] {
                    "Capable of parsing the java api. See "+ice.getPrefix()+"help api."
            };
        } else if (message.equals("api")){
            return new String[] {
                    "Search all information within the api for the given term.",
                    "Example: "+ice.getPrefix()+"api String.toS*"
            };
        } else if (message.equals("class")){
            return new String[] {
                    "Search all classes within the api for the given term.",
                    "Example: "+ice.getPrefix()+"api String*"
            };
        } else if (message.equals("method")){
            return new String[] {
                    "Search all methods within the api for the given term.",
                    "Example: "+ice.getPrefix()+"api String.toS*"
            };
        } else if (message.equals("apistat")){
            return new String[] {
                    "Give some statistics for the api plugin."
            };
        } else if (message.equals("apireload")){
            return new String[] {
                    "Causes the api.dat file to be reloaded. If no such file exists, it will be rebuilt as specified in the config."
            };
        }


        return null;
    }


}
