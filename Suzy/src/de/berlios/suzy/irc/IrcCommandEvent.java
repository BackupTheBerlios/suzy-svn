package de.berlios.suzy.irc;

/**
 * This is the Event that a command from irc will be passed to the appropriate plugin with.
 * It contains a reference to the {@link IrcSender} which is responsible for answering, the
 * target the message came from, the command itself and the message after the command if any.
 *
 * @author honk
 */
public class IrcCommandEvent {
    private IrcClient source;
    private IrcTarget target;
    private String prefix;
    private String command;
    private String message;


    /**
     * Constructs a new IrcCommandEvent with the given data.
     * @param source the source where the event occured (typically an IrcClient)
     * @param target contains information about who caused the event
     * @param command the command that was issued
     * @param message the message following the command, stripped of leading and trailing spaces
     */
    public IrcCommandEvent(IrcClient source, IrcTarget target, String prefix, String command, String message) {
        this.source = source;
        this.target = target;
        this.prefix = prefix;
        this.command = command;
        this.message = message;
    }

    /**
     * returns the source for this event
     * @return the source where the event occured (typically an IrcClient)
     */
    public IrcClient getSource() {
        return source;
    }
    /**
     * returns the target that caused this event and probably expects an answer
     * @return contains information about who caused the event
     */
    public IrcTarget getTarget() {
        return target;
    }
    /**
     * returns the command that was issued
     * @return the command that was issued
     */
    public String getCommand() {
        return command;
    }
    /**
     * returns the message following the command, stripped of leading and trailing spaces
     * @return the message following the command, stripped of leading and trailing spaces
     */
    public String getMessageContent() {
        return message;
    }

    /**
     * returns the command prefix
     * @return the command prefix
     */
    public String getPrefix() {
        return prefix;
    }
}
