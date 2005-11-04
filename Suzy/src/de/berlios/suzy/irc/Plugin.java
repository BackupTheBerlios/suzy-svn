package de.berlios.suzy.irc;

/**
 * An extension for the irc client. Plugins may be loaded and unloaded
 * at runtime. Plugins must either offer the default constructor or
 * a constructor with 1 String as argument. This will be called
 * preferably (default is fallback). The argument will be the network
 * name.
 * <br>
 * Plugins have to be in the default package in order to be loaded.
 *
 * @author honk
 */
public interface Plugin {
    /**
     * A list of commands that this plugins want to handle. Each
     * command has to be plain text without any prefixes.
     * @return a list of commands handled by this plugin
     */
    public String[] getCommands();
    /**
     * A list of restricted commands that this plugins want to handle. Each
     * command has to be plain text without any prefixes. The access will
     * only be allowed if the user has access to the admin functions.
     *
     * @return a list of restricted commands handled by this plugin
     */
    public String[] getRestrictedCommands();
    /**
     * This method will be called whenever an event this plugin can handle
     * occurs. The {@link IrcCommandEvent} will hold the information
     * necessary to find out which command was called.
     *
     * @param ice the Event that occured
     */
    public void handleEvent(IrcCommandEvent ice);
}
