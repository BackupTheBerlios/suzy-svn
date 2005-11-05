package de.berlios.suzy.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.berlios.suzy.irc.plugin.LoaderPlugin;

/**
 * This class is an IRC client. It will connect to the specified host and port.
 * The client will try to reconnect if it gets disconnected.
 * <br>
 * Plugins are used to handle commands that start with the COMMAND_MODIFIER.
 *
 *
 * @author honk
 */
public class IrcClient implements IrcSender {
    private String commandModifier;
    private String adminChannel;
    private String adminChannelPassword;
    private long timeout;

    //reconnect throttling
    long lastConnectTry = 0;
    final int steps = 10;
    final int minWait = 5;
    final int maxWait = 60;
    int badTries = 0;


    private String server;
    private String nickName;
    private String desiredNickName;
    private int port;
    private Socket sock;
    private BufferedReader br;
    private PrintWriter pw;
    private LoaderPlugin loaderPlugin;
    private ConnectThread connectThread;

    private Set<String> admins = new TreeSet<String>();


    /**
     * Creates a new IrcClient which will cause plugins to be loaded.
     * @param network name of the network (used for config files e.g.)
     * @param server address of the irc server
     * @param port port of the irc server
     * @param nickName the bot's name on irc
     * @param adminChannel a channel which is used for determining administrator status
     * @param adminChannelPassword the password for the adminChannel
     * @param timeout timeout after which the bot will reconnect to the irc server in seconds (120 seconds should be a good value)
     * @param commandModifier prefix the bot will look for when searching for commands from users (e.g. "!" if you want the bot to react to "!help")
     */
    public IrcClient(String network, String server, int port, String nickName, String adminChannel, String adminChannelPassword, int timeout, String commandModifier) {
        this.server = server;
        this.port = port;
        this.nickName = nickName;
        this.desiredNickName = nickName;
        this.adminChannel = adminChannel;
        this.adminChannelPassword = adminChannelPassword;
        this.timeout = timeout*1000;    //store as ms instead of second
        this.commandModifier = commandModifier;

        loaderPlugin = new LoaderPlugin(network);

        connectThread = new ConnectThread();
        connect();

        new Thread(connectThread).start();
    }

    private void connect() {
        try {
            admins.clear();

            sock = new Socket(server, port);
            sock.setKeepAlive(true);
            br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            pw = new PrintWriter(sock.getOutputStream());

            new IrcHandler().start();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
            }

            send("USER hapi 0 0 :Honks Api Test");
            send("NICK "+nickName);
            connectThread.connectSucceeded();
            return;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("Connect failed - retrying");
        connectThread.connectFailed();
    }


    private void handleJoinedChanel(String[] cmd) {

        String channel = cmd[3].split(" ")[1];

        if (channel.equals(adminChannel)) {
            String message = cmd[3];
            String userString = message.substring(message.indexOf(':')+1);

            String[] users = userString.replaceAll("[@\\+]", "").split(" ");

            System.out.println("channel: "+channel+" users "+Arrays.toString(users));

            for(String user: users) {
                admins.add(user);
            }
        }
    }


    private void handleNickInUse(String[] cmd) {
        String myNick = cmd[2];

        if (myNick.equals("*")) {
            String nickInUse = cmd[3].substring(0, cmd[3].indexOf(' '));
            String newNickName = nickName+Math.round(Math.random()*10);
            System.out.println("Nickname "+nickInUse+" in use, changing to: "+newNickName);
            send("NICK "+newNickName);
            this.nickName = newNickName;
        }
    }

    private void handleQuit(String[] cmd) {
        String target = parseHostToNick(cmd[0]);
        admins.remove(target);
    }

    private void handleNick(String[] cmd) {
        //:_biO_!~bio@peon.workwork.de NICK :_bio_
        String from = parseHostToNick(cmd[0]);
        String to = cmd[2].substring(1);
        if (admins.contains(from)) {
            admins.remove(from);
            admins.add(to);
        }

        if (from.equals(nickName)) {
            nickName = to;
        }
    }

    private void handleJoin(String[] cmd) {
        String[] channels = cmd[2].split("[:,]");
        for (String channel: channels) {
            if (channel.equals(adminChannel)) {
                String target = parseHostToNick(cmd[0]);
                admins.add(target);
            }
        }
    }

    private void handlePart(String[] cmd) {
        String[] channels = cmd[2].split("[:,]");
        for (String channel: channels) {
            if (channel.equals(adminChannel)) {
                String target = parseHostToNick(cmd[0]);
                admins.remove(target);
            }
        }
    }

    /**
     * Sends this text to the irc server.
     * If the text has more than 511 chars, it will be cut off.
     * A linefeed will be added to the end of the String.
     * @param text text to send
     */
    public void send(String text) {
        if (pw == null) {
            System.err.println("Trying to send without being connected.");
            return;
        }

        //TODO throttle
        System.out.println("--> "+text);

        text = text.substring(0, Math.min(511, text.length()));
        text = text+"\n";

        pw.write(text);
        pw.flush();
    }


    private void handlePrivmsg(String[] cmd) {
        String[] messageContent = cmd[3].trim().substring(1).split("\\s+", 2);

        String command = messageContent[0].toLowerCase();
        if (!command.startsWith(commandModifier)) {
            return;
        } else {
            command = messageContent[0].substring(commandModifier.length());
        }

        String message = messageContent.length>1?messageContent[1]:"";
        IrcTarget target = getTarget(cmd);

		boolean restricted = false;
        Plugin plugin = loaderPlugin.getPluginList().get(command);
        if (plugin == null) {
        	// restricted?
        	plugin = loaderPlugin.getRestrictedPluginList().get(command);
        	if (plugin != null) {
        		restricted = true;
        	} else {
        		// we could be using a namespace
				int colonIndex;
		        if ((colonIndex = command.indexOf(':')) != -1) {
		            String pluginName = command.substring(0, colonIndex);
		            command = command.substring(colonIndex + 1);
					plugin = loaderPlugin.getPluginList().get(command);
			        if (plugin == null) {
			        	// restricted?
			        	plugin = loaderPlugin.getRestrictedPluginList().get(command);
			        	if (plugin != null) {
			        		restricted = true;
			        	} else {
			        		return; // no handler
			        	}
			        }
		        	String adjustedClassName = pluginName + "plugin";
		        	String actualClassName = plugin.getClass().getSimpleName().toLowerCase();
		        	if (!adjustedClassName.equals(actualClassName)) {
		        		return; // incorrect namespace
		        	}
			    } else {
			    	return; // or maybe there is just no handler
			    }
        	}
        }

        // command could be still using namespace (case of duplicates)
        int colonIndex;
        if ((colonIndex = command.indexOf(':')) != -1) {
            command = command.substring(colonIndex + 1);
		}

        if (restricted) {
            if (admins.contains(target.getUser())) {
            } else {
                sendMessageTo(target.getUser(), MessageTypes.PRIVMSG, "Sorry, you do not have access to this command.");
                return;
            }
        }

        IrcCommandEvent ircCmdEvent = new IrcCommandEvent(this, target, commandModifier, command, message);

        try {
            plugin.handleEvent(ircCmdEvent);
        } catch (Throwable t) {
            sendMessageTo(ircCmdEvent.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "Execution failed: "+t.getClass().getName()+": "+t.getMessage());
            t.printStackTrace();
        }

    }



    private IrcTarget getTarget(String[] cmd) {
        if(cmd[2].equals(nickName) || cmd[2].equals(desiredNickName)) {
            return new IrcTarget(parseHostToNick(cmd[0]), null,true);
        } else {
            return new IrcTarget(parseHostToNick(cmd[0]), cmd[2], false);
        }
    }


    //:Hapi!~h@p5481B69C.dip0.t-ipconnect.de
    // -> Hapi
    private String parseHostToNick(String host) {
        return host.substring(1, host.indexOf("!"));
    }

    private void disconnect() {
        send("QUIT");
        pw.flush();

        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private interface IrcAction {
        public void run(String[] cmd);
    }

    private void handleConnected() {
        for (PerformOnConnectPlugin p: loaderPlugin.getPerformOnConnectList()) {
            p.perform(this);
        }

        send("JOIN "+adminChannel+" "+adminChannelPassword);

    }

    private class IrcHandler extends Thread {

        private Map<String, IrcAction> actionMap = new HashMap<String, IrcAction>();
        public IrcHandler() {
            actionMap.put("PING", new IrcAction() {
                public void run(String[] cmd) {
                    send("PONG "+cmd[1]);
                }
            });
            actionMap.put("PONG", new IrcAction() {
                public void run(String[] cmd) {
                    connectThread.pongReceived();
                }
            });
            actionMap.put("376", new IrcAction() {
                public void run(String[] cmd) {
                    handleConnected();
                }
            });
            actionMap.put("433", new IrcAction() {
                public void run(String[] cmd) {
                    handleNickInUse(cmd);
                }
            });
            actionMap.put("353", new IrcAction() {
                public void run(String[] cmd) {
                    handleJoinedChanel(cmd);
                }
            });
            actionMap.put("JOIN", new IrcAction() {
                public void run(String[] cmd) {
                    handleJoin(cmd);
                }
            });
            actionMap.put("QUIT", new IrcAction() {
                public void run(String[] cmd) {
                    handleQuit(cmd);
                }
            });
            actionMap.put("NICK", new IrcAction() {
                public void run(String[] cmd) {
                    handleNick(cmd);
                }
            });
            actionMap.put("PART", new IrcAction() {
                public void run(String[] cmd) {
                    handlePart(cmd);
                }
            });
            actionMap.put("PRIVMSG", new IrcAction() {
                public void run(String[] cmd) {
                    handlePrivmsg(cmd);
                }
            });
        }

        public void run() {
            String line;
            try {
                while (true) {
                    line = br.readLine();
                    System.out.println("<-- "+line);
                    if (line == null) {
                        break;
                    }
                    handle(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.err.println("Connection lost - reconnecting");
            disconnect();
            throttleReconnect();
            connect();
        }

        private void handle(String line) {
            String[] input = line.split(" ", 4);

            IrcAction cmd = actionMap.get(input[0].toUpperCase());
            if (cmd != null) {
                cmd.run(input);
            } else {
                cmd = actionMap.get(input[1].toUpperCase());
                if (cmd != null) {
                    cmd.run(input);
                }
            }
        }
    }

    /**
     * Sends this text to the target (user/channel) specified.
     * If the text has more than 511 chars, it will be cut off.
     * A linefeed will be added to the end of the String.
     * @param target specifies where the text should be send to
     * @param type type of the message to send (message or notice)
     * @param text text to send
     */
    public void sendMessageTo(String target, MessageTypes type, String text) {
        switch (type) {
            case PRIVMSG: send("PRIVMSG "+target+" :"+text);
                break;
            case NOTICE: send("NOTICE "+target+" :"+text);
                break;
        }
    }


    private class ConnectThread implements Runnable {
        private long lastPingReceived;
        private boolean connectFailed = false;
        public ConnectThread() {
            restartTimer();
        }
        private void restartTimer() {
            lastPingReceived = System.currentTimeMillis();
        }
        public void connectFailed() {
            connectFailed = true;
        }
        public void connectSucceeded() {
            connectFailed = false;
        }
        public void pongReceived() {
            restartTimer();
        }
        public void run() {
            while(true) {
                if (!connectFailed) {
                    if (System.currentTimeMillis()-lastPingReceived > timeout) {
                        System.err.println("Timeout - forcing reconnect");
                        disconnect();
                        throttleReconnect();
                        connect();
                        restartTimer();
                    }

                    send("PING :livecheck");

                    if (!IrcClient.this.desiredNickName.equals(IrcClient.this.nickName)) {
                        send("NICK :"+desiredNickName);
                    }

                    try {
                        Thread.sleep(timeout/5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    throttleReconnect();

                    connect();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void throttleReconnect() {
        int timeSinceLastTry = (int)((System.currentTimeMillis() - lastConnectTry)/1000);
        if (timeSinceLastTry < maxWait) {
            badTries++;
        } else {
            badTries = 0;
        }


        try {
            int sleepModifier = badTries*maxWait/steps + minWait;
            sleepModifier = Math.min(sleepModifier, maxWait);
            Thread.sleep(sleepModifier*1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        lastConnectTry = System.currentTimeMillis();
    }

}
