package de.berlios.suzy.irc;

/**
 * Specifies a target on irc. Typically this would be a channel or a user.
 *
 * @author honk
 */
public class IrcTarget {
    private String user;
    private String channel;
    private boolean isPrivate;

    /**
     * Creates a new Target with the given user and channel
     * @param user the user that is specified by this target
     * @param channel channel that is specified by this target, if any
     * @param isPrivate whether or not the target is created from a user which
     * expects text in a private message rather than a channel
     */
    public IrcTarget(String user, String channel, boolean isPrivate) {
        this.user = user;
        this.channel = channel;
        this.isPrivate = isPrivate;
    }

    /**
     * This method tells you if the user specified by this target expects
     * a message in a channel or in private. Typically this will tell you
     * if the user contacted us via a query in a channel.
     *
     * @return whether or not the user expects a private message or a channel wide message
     */
    public boolean isPrivate() {
        return isPrivate;
    }
    /**
     * the user that is specified by this target
     * @return the user that is specified by this target
     */
    public String getUser() {
        return user;
    }
    /**
     * the channel that is specified by this target, if any
     * @return the channel that is specified by this target, if any; null if no channel was specified
     */
    public String getChannel() {
        return channel;
    }

    /**
     * This will return the appropriate method to contact the target specified by this
     * instance. Either a channel or a user.
     * @return The contact point that the target specified by this instance expects
     */
    public String getDefaultTarget() {
        if (!isPrivate) {
            return channel;
        }
        return user;
    }
}
