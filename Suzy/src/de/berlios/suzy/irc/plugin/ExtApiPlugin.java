package de.berlios.suzy.irc.plugin;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.Plugin;
import de.berlios.suzy.parser.ApiSearchUtil;
import de.berlios.suzy.parser.ParserFactory;

/**
 * Simple refactoring of {@link ApiPlugin} to support multiple apis.
 * 
 * @author Antubis
 * 
 */
public class ExtApiPlugin implements Plugin {

    private enum LastRequest {
        ALL, CLASSES, METHODS, FIELDS, NONE
    };

    private HashMap<String, StatEntry> requestMap;

    /**
     * Causes the underlying RequestHandler to load an instance of the api data.
     */
    public ExtApiPlugin() {
        requestMap = new HashMap<String, StatEntry>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.berlios.suzy.irc.Plugin#getCommands()
     */
    public String[] getCommands() {
        String[] defaultCmds = new String[] { "api", "class", "method", "field",
                 "apistat" };
        // for old usage
        List<String> cmds = new ArrayList<String>(Arrays.asList(defaultCmds));
        for (String parserName : ParserFactory.getInstance().getParserNames()) {
            for (String cmd : defaultCmds) {
                cmds.add(cmd + "." + parserName);
            }
        }
        return cmds.toArray(new String[cmds.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.berlios.suzy.irc.Plugin#getRestrictedCommands()
     */
    public String[] getRestrictedCommands() {
        String[] defaultCmds = new String[] { "apireload" };
        List<String> cmds = new ArrayList<String>(Arrays.asList(defaultCmds));
        for (String parserName : ParserFactory.getInstance().getParserNames()) {
            for (String cmd : defaultCmds) {
                cmds.add(cmd + "." + parserName);
            }
        }
        return cmds.toArray(new String[cmds.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.berlios.suzy.irc.Plugin#handleEvent(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public void handleEvent(IrcCommandEvent ice) {
        if (!ParserFactory.getInstance().supportsParserName(
                extractParserName(ice.getCommand()))) {
            return;
        }
        if (ice.getCommand().startsWith("apistat")) {
            stats(ice);
            return;
        } else if (ice.getCommand().startsWith("apireload")) {
            reload(ice);
            return;
        }

        if (ice.getMessageContent().length() == 0) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(),
                    MessageTypes.PRIVMSG,
                    "Missing argument: see "+ice.getPrefix()+"help");
            return;
        }

        if (ice.getCommand().startsWith("api")) {
            api(ice);
        } else if (ice.getCommand().startsWith("class")) {
            classes(ice);
        } else if (ice.getCommand().startsWith("method")) {
            methods(ice);
        } else if (ice.getCommand().startsWith("field")) {
            fields(ice);
        } 
    }

    private void reload(IrcCommandEvent ice) {
        String name = extractParserName(ice.getCommand());
        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(),
                MessageTypes.PRIVMSG, "Reloading api " + name + ".");
        ParserFactory.getInstance().getParser(name).reload();
        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(),
                MessageTypes.PRIVMSG, "The api " + name + " was reloaded.");

    }

    private void stats(IrcCommandEvent ice) {
        String parserName = extractParserName(ice.getCommand());
        StatEntry stat = requestMap.get(parserName);
        if (stat == null || stat.getLastRequest() == LastRequest.NONE) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(),
                    MessageTypes.PRIVMSG, "No previous query.");
            return;
        }

        int fields = ApiSearchUtil.fieldCount(ParserFactory.getInstance()
                .getParser(parserName).getClassInfos());
        int methods = ApiSearchUtil.methodCount(ParserFactory.getInstance()
                .getParser(parserName).getClassInfos());
        int classes = ApiSearchUtil.classCount(ParserFactory.getInstance()
                .getParser(parserName).getClassInfos());

        long diff = stat.getTimeDiff();
        String time = (diff / 1000) + " µs";

        String answer = "";
        int total = 0;
        switch (stat.getLastRequest()) {
        case ALL:
            answer = "Searched " + classes + " classes and " + methods
                    + " methods in " + time;
            total = classes + methods;
            break;
        case CLASSES:
            answer = "Searched " + classes + " classes in " + time;
            total = classes;
            break;
        case METHODS:
            answer = "Searched " + methods + " methods in " + time;
            total = methods;
            break;
        case FIELDS:
            answer = "Searched " + fields + " fields in " + time;
            total = methods;
            break;
        }

        double perItem = diff / (1000. * total);
        String perItemString = new DecimalFormat("0.00 µs").format(perItem);

        answer += " (" + perItemString + " / item).";

        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(),
                MessageTypes.PRIVMSG, answer);
    }

    private void api(IrcCommandEvent ice) {
        String parserName = extractParserName(ice.getCommand());
        StatEntry stat = new StatEntry();
        stat.start(LastRequest.ALL);
        Set<String> matches = ApiSearchUtil.parseAll(ParserFactory
                .getInstance().getParser(parserName).getClassInfos(), ice
                .getMessageContent());
        stat.end();
        requestMap.put(parserName, stat);
        reply(ice, matches);
    }

    private void classes(IrcCommandEvent ice) {
        String parserName = extractParserName(ice.getCommand());
        StatEntry stat = new StatEntry();
        stat.start(LastRequest.CLASSES);
        /*Set<String> matches = RequestHandler.getInstance().parseClasses(
                ice.getMessageContent());*/
        Set<String> matches = ApiSearchUtil.parseClasses(ParserFactory
                .getInstance().getParser(parserName).getClassInfos(), ice
                .getMessageContent());
        stat.end();
        requestMap.put(parserName, stat);
        reply(ice, matches);
    }

    private void methods(IrcCommandEvent ice) {
        String parserName = extractParserName(ice.getCommand());
        StatEntry stat = new StatEntry();
        stat.start(LastRequest.METHODS);
        Set<String> matches = ApiSearchUtil.parseMethods(ParserFactory
                .getInstance().getParser(parserName).getClassInfos(), ice
                .getMessageContent());
        stat.end();
        requestMap.put(parserName, stat);
        reply(ice, matches);
    }
    
    private void fields(IrcCommandEvent ice) {
        String parserName = extractParserName(ice.getCommand());
        StatEntry stat = new StatEntry();
        stat.start(LastRequest.FIELDS);
        Set<String> matches = ApiSearchUtil.parseFields(ParserFactory
                .getInstance().getParser(parserName).getClassInfos(), ice
                .getMessageContent());
        stat.end();
        requestMap.put(parserName, stat);
        reply(ice, matches);
    }


    private void reply(IrcCommandEvent ice, Set<String> matches) {
        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(),
                MessageTypes.PRIVMSG, format(matches));
    }

    private String format(Set<String> matches) {
        if (matches.size() == 0) {
            return "No matches found.";
        }

        StringBuilder sb = new StringBuilder();
        for (String match : matches) {
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

    /*
     * (non-Javadoc)
     * 
     * @see de.berlios.suzy.irc.Plugin#getHelp(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public String[] getHelp(IrcCommandEvent ice) {
        String message = ice.getMessageContent();

        if (message.equals("apiplugin")) {
            return new String[] { "Capable of parsing the java api. See "
                    + ice.getPrefix() + "help api." };
        } else if (message.equals("api")) {
            return new String[] {
                    "Search all information (classes+methods) within the api for the given term.",
                    "Example: " + ice.getPrefix() + "api String.toS*" };
        } else if (message.equals("class")) {
            return new String[] {
                    "Search all classes within the api for the given term.",
                    "Example: " + ice.getPrefix() + "class String*" };
        } else if (message.equals("method")) {
            return new String[] {
                    "Search all methods within the api for the given term.",
                    "Example: " + ice.getPrefix() + "method String.toS*" };
        } else if (message.equals("field")) {
            return new String[] {
                    "Search all fields within the api for the given term.",
                    "Example: " + ice.getPrefix() + "field String.toS*" };
        } else if (message.equals("apistat")) {
            return new String[] { "Give some statistics for the api plugin." };
        } else if (message.equals("apireload")) {
            return new String[] { "Causes the api.dat file to be reloaded. If no such file exists, it will be rebuilt as specified in the config." };
        }

        return null;
    }

    private static String extractParserName(String cmd) {
        int pos;
        String ret = "default";
        if ((pos = cmd.indexOf(".")) != -1) {
            ret = cmd.substring(++pos);
        }
        return ret;
    }

    private static class StatEntry {

        private long start;

        private long end;

        private LastRequest lastRequest;

        public void start(LastRequest request) {
            lastRequest = request;
            start = getCurrentTime();
        }

        public void end() {
            end = getCurrentTime();
        }

        public long getTimeDiff() {
            return end - start;
        }

        public LastRequest getLastRequest() {
            return lastRequest;
        }

        private long getCurrentTime() {
            return System.nanoTime();
        }

    }
}
