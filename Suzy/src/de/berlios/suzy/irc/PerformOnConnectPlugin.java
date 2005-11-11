package de.berlios.suzy.irc;

/**
 * A plugin that wants to be called whenever a new connection
 * to irc is made.
 *
 * @author honk
 */
public interface PerformOnConnectPlugin extends Plugin {
    /**
     * This method will be called when a new connection to an irc server is
     * made, if the plugin is loaded at that time.
     *
     * @param ircSender a sender that is capable of sending text to server
     * that the connection was made to.
     */
    public void perform(IrcClient ircSender);
}
