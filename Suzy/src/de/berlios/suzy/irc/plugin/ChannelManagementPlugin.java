package de.berlios.suzy.irc.plugin;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.Plugin;

/**
 * Adds some functionality for channelmanagement
 * <br>
 * <br>
 * <TABLE border="1">
 *   <CAPTION><b>Commands available</b></CAPTION>
 *   <TR><TH>kick<TD>*<TD>kicks a user out of the channel
 *   <TR><TH>ban<TD>*<TD>kicks and bans a user out of the channel
 *   <TR><TH>unban<TD>*<TD>unbans up to 6 users in the channel
 *   <TR><TH>mute<TD>*<TD>bans a user in the channel
 *   <TR><TH>unmute<TD>*<TD>unbans up to 6 users in the channel
 *   <TR><TH>mode<TD>*<TD>sets a channel-mode
 *   <TR><TH>topic<TD>*<TD>sets a new topic for the channel
 *   <TR><TH>op<TD>*<TD>ops up to 6 users in the channel
 *   <TR><TH>deop<TD>*<TD>deops up to 6 users in the channel
 *   <TR><TH>voice<TD>*<TD>voices up to 6 users in the channel
 *   <TR><TH>devoice<TD>*<TD>devoices up to 6users in the channel
 *   <TR><TH>limit<TD>*<TD>sets a user-limit for the channel
 *   <TR><TH>invite<TD>*<TD>invites somebody to the channel
 * </table>
 * * restricted
 *
 * @author sbeh
 */
public class ChannelManagementPlugin implements Plugin {
	private enum Command { KICK, BAN, UNBAN, MUTE, UNMUTE, MODE, TOPIC, OP, DEOP, VOICE, DEVOICE, LIMIT, INVITE }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#handleEvent(de.berlios.suzy.irc.IrcCommandEvent)
     */
	public void handleEvent(IrcCommandEvent ice) {
		if(ice.getTarget().isPrivate())
			return;
		for(Command cmd : Command.values()) {
			if(!cmd.name().equals(ice.getCommand().toUpperCase()))
				continue;
			String[] target;
			switch(cmd) {
			case KICK:
				target = ice.getMessageContent().split(" ", 2);
				if(target.length == 2)
					ice.getSource().send("KICK " + ice.getTarget().getChannel() + " " + target[0] + " :" + target[1]);
				else if(target.length == 1)
					ice.getSource().send("KICK " + ice.getTarget().getChannel() + " " + target[0]);
				else
					ice.getSource().sendMessageTo(ice.getTarget().getChannel(), MessageTypes.PRIVMSG, "No victim specified.");
				break;
			case BAN:
				target = ice.getMessageContent().split(" ", 2);
				if(target.length == 2) {
					ice.getSource().send("KICK " + ice.getTarget().getChannel() + " " + target[0] + " :" + target[1]);
					ice.getSource().send("MODE " + ice.getTarget().getChannel() + " +b " + target[0]);
				} else if(target.length == 1) {
					ice.getSource().send("KICK " + ice.getTarget().getChannel() + " " + target[0]);
					ice.getSource().send("MODE " + ice.getTarget().getChannel() + " +b " + target[0]);
				} else {
					ice.getSource().sendMessageTo(ice.getTarget().getChannel(), MessageTypes.PRIVMSG, "No victim specified.");
				}
				break;
			case MUTE:
				ice.getSource().send("MODE " + ice.getTarget().getChannel() + " -vo+b " + ice.getMessageContent() + " " + ice.getMessageContent() + " " + ice.getMessageContent());
				break;
			case UNBAN:
			case UNMUTE:
				ice.getSource().send("MODE " + ice.getTarget().getChannel() + " -bbbbbb " + ice.getMessageContent());
				break;
			case MODE:
				ice.getSource().send("MODE " + ice.getTarget().getChannel() + " " + ice.getMessageContent());
				break;
			case TOPIC:
				ice.getSource().send("TOPIC " + ice.getTarget().getChannel() + " :" + ice.getMessageContent());
				break;
			case OP:
				ice.getSource().send("MODE " + ice.getTarget().getChannel() + " +oooooo " + ice.getMessageContent());
				break;
			case DEOP:
				ice.getSource().send("MODE " + ice.getTarget().getChannel() + " -oooooo " + ice.getMessageContent());
				break;
			case VOICE:
				ice.getSource().send("MODE " + ice.getTarget().getChannel() + " +vvvvvv " + ice.getMessageContent());
				break;
			case DEVOICE:
				ice.getSource().send("MODE " + ice.getTarget().getChannel() + " -vvvvvv " + ice.getMessageContent());
				break;
			case LIMIT:
				ice.getSource().send("MODE " + ice.getTarget().getChannel() + " +l " + ice.getMessageContent());
				break;
			case INVITE:
				ice.getSource().send("INVITE " + ice.getTarget().getChannel() + " " + ice.getMessageContent());
			}
		}
	}

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getRestrictedCommands()
     */
	public String[] getRestrictedCommands() {
		int count = Command.values().length;
		String[] cmds = new String[count];
		for(Command cmd : Command.values()) {
			cmds[--count] = cmd.name().toLowerCase();
		}
		return cmds;
	}

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getCommands()
     */
	public String[] getCommands() {
		return new String[0];
	}

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getHelp(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public String[] getHelp(IrcCommandEvent ice) {
    	for(Command cmd : Command.values()) {
    		if(!cmd.name().equals(ice.getMessageContent().toUpperCase()))
    			continue;
    		switch(cmd) {
    		case KICK:
    			return new String[] {
    				"Kicks a user out of the channel",
    				"Example: "+ice.getPrefix()+"kick Honk"
    			};
    		case BAN:
    			return new String[] {
    				"kicks and bans a user out of the channel",
    				"Example: "+ice.getPrefix()+"ban Honk"
    			};
    		case MUTE:
    			return new String[] {
    				"bans,devoices and deops a user in the channel, so that he can not write new messages",
    				"Example: "+ice.getPrefix()+"mute Honk"
    			};
    		case MODE:
    			return new String[] {
    				"Sets a channelmode",
    				"Example: "+ice.getPrefix()+"mode +is"
    			};
    		case TOPIC:
    			return new String[] {
    				"Sets a new topic for the channel",
    				"Example: "+ice.getPrefix()+"topic welcome to " + ice.getTarget().getChannel()
    			};
    		case LIMIT:
    			return new String[] {
    				"Sets a new userlimit for the channel",
    				"Example: "+ice.getPrefix()+"limit 13"
    			};
    		case INVITE:
    			return new String[] {
    				"Invites an user into the channel",
    				"Example: "+ice.getPrefix()+"invite Honk"
    			};
    		case OP: case DEOP: case VOICE: case DEVOICE: case UNBAN: case UNMUTE:
    			return new String[] {
    				cmd.name().toLowerCase() + "s up to 6 users in the channel",
    				"Example: "+ice.getPrefix()+cmd.name().toLowerCase()+" Honk sbeh"
    			};
    		}
    	}
		return null;
    }
}
