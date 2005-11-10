package de.berlios.suzy;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import de.berlios.suzy.irc.IrcClient;
import de.berlios.suzy.parser.RequestHandler;



/**
 * This class is the entrypoint to the ApiBot. It starts the {@link IrcClient}
 * and causes the {@link RequestHandler} to load the api.
 *
 * @author honk
 */
public class Suzy {
    /**
     * Entry point. Causes the api to be loaded and starts an {@link IrcClient}.
     * @param args command line parameters, unused for now
     */
    public static void main(String[] args) {
        try {
            Scanner configScanner = new Scanner(new File("servers.conf"));


            ArrayList<String> config = new ArrayList<String>();
            while (configScanner.hasNext()) {
                String val = configScanner.nextLine();
                val = val.trim();

                if (val.length() != 0 && !val.startsWith("//")) {
                    config.add(val);
                    System.out.println(val);
                }
            }

            configScanner.close();


            int index = 0;
            while (index < config.size()) {
                String network = config.get(index++);
                String server = config.get(index++);
                int port = Integer.parseInt(config.get(index++));
                String nickName = config.get(index++);
                String adminChannel = config.get(index++);
                String adminChannelPassword = config.get(index++);
                int timeout = Integer.parseInt(config.get(index++));
                String commandModifier = config.get(index++);

                new IrcClient(network, server, port, nickName, adminChannel, adminChannelPassword, timeout, commandModifier);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("Could not load server list - aborting");
        } catch (ArrayIndexOutOfBoundsException aiob) {
            aiob.printStackTrace();
            System.err.println("Server list (servers.conf) is not in a valid format - aborting");
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            System.err.println("Non-integer found for port or timeout in servers.conf - aborting");
        }
    }

}
