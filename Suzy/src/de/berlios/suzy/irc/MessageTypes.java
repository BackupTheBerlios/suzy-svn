package de.berlios.suzy.irc;

/**
 * This enumeration represents the  supported message types.
 *
 * @author honk
 */
public enum MessageTypes {
    /**
     * a PRIVMSG, the normal message type.
     */
    PRIVMSG("PRIVMSG"),
    /**
     * an ACTION.
     */
    ACTION("PRIVMSG", "\1ACTION ", "\1"),
    /**
     * a NOTICE
     */
    NOTICE("NOTICE");


    private String prefix;
    private String postfix = "";
    private String postpostfix = "";

    private MessageTypes(String prefix) {
        this.prefix = prefix;
    }

    private MessageTypes(String prefix, String postfix, String postpostfix) {
        this.prefix = prefix;
        this.postfix = postfix;
        this.postpostfix = postpostfix;
    }

    public String getMessage(String target, String text) {
        return prefix+" "+target+" :"+postfix+text+postpostfix;
    }
}