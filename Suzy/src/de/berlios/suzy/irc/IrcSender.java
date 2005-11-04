package de.berlios.suzy.irc;

/**
 * An IrcSender can send texts to an irc server.
 *
 * @author honk
 */
public interface IrcSender {
    /**
     * Sends a message with the specified text and type to the target.
     * @param target the target to send to (user/channel)
     * @param type type of the message to send
     * @param text text to send
     */
    public void sendMessageTo(String target, MessageTypes type, String text);
    /**
     * Sends the given message as raw text to the irc server.
     * @param text text to send
     */
    public void send(String text);
}
