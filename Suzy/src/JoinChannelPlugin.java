import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.IrcSender;
import de.berlios.suzy.irc.PerformOnConnectPlugin;

/**
 * This channel joins a predefined set of channels when the client
 * connects to irc.
 * There are also commands to join and part channels manually
 *
 * <TABLE border="1">
 *   <CAPTION>Commands available</CAPTION>
 *   <TR><TH>join<TD>*<TD>joins a channel
 *   <TR><TH>part<TD>*<TD>parts a channel
 * </table>
 * * restricted
 *
 * @author honk
 */
public class JoinChannelPlugin implements PerformOnConnectPlugin {
    private Set<String> channels;

    /**
     * creates a new instance that will join the channels for this server as specified in
     * channels_server.conf
     * @param server server to load channels for
     */
    public JoinChannelPlugin(String server) {
        loadChannelList(server);
    }

    private void loadChannelList(String server) {
        Set<String> channels = new HashSet<String>();

        Scanner sc;
        try {
            sc = new Scanner(new File("channels_"+server+".conf"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            this.channels = new HashSet<String>();
            return;
        }

        while (sc.hasNext()) {
            String chan = sc.nextLine().trim();
            System.out.println(chan);
            if (!chan.startsWith("//") && chan.length() != 0) {
                channels.add(chan);
            }
        }

        sc.close();

        this.channels = channels;
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.PerformOnConnectPlugin#perform(de.berlios.suzy.irc.IrcSender)
     */
    public void perform(IrcSender ircSender) {
        StringBuilder channelString = new StringBuilder("");
        for (String chan: channels) {
            if (channelString.length() != 0) {
                channelString.append(",");
            }
            channelString.append(chan);
        }
        ircSender.send("JOIN "+channelString.toString());
    }


    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getCommands()
     */
    public String[] getCommands() {
        return new String[0];
    }
    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getRestrictedCommands()
     */
    public String[] getRestrictedCommands() {
        return new String[] {
                "join",
                "part"
        };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#handleEvent(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public void handleEvent(IrcCommandEvent ice) {
        System.out.println(ice.getCommand()+ " - "+ice.getMessageContent());
        if (ice.getCommand().equals("join")) {
            join(ice);
        } else if (ice.getCommand().equals("part")) {
            part(ice);
        }
    }

    private void part(IrcCommandEvent ice) {
        channels.remove(ice.getMessageContent());
        ice.getSource().send("PART "+ice.getMessageContent());
    }

    private void join(IrcCommandEvent ice) {
        channels.add(ice.getMessageContent());
        ice.getSource().send("JOIN "+ice.getMessageContent());
    }

}
