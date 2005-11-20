package de.berlios.suzy.irc.plugin;

import java.lang.reflect.Modifier;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.Plugin;

import de.berlios.suzy.parser.RequestHandler;
import de.berlios.suzy.parser.ClassInfo;

/**
 * Slap plugin that.picks a random class from the class library, adds a short
 * description and slaps the target with it.
 *
 * @author fx21
 */

public class SlapPlugin implements Plugin {

    // command listing

    public String[] getCommands() {
        return new String[] { "slap" };
    }

    // restricted command listing

    public String[] getRestrictedCommands() {
        return new String[] {};
    }

    // method for generating slap strings, to be used by the slap command

    public String doSlap(String target) {

        String randomClass = getRandomClass();
        String descriptor;
        String smiley = getRandomSmiley();

        try {

            // classloader? might need to use the system class loader instead

            Class<?> theClass = Class.forName(randomClass, false, getClass()
                    .getClassLoader());

            // a couple of ifs to add a colourful description of the
            // class we're using

            if (theClass.isInterface()) {
                descriptor = "an implementation of";
            }
            if (Modifier.isAbstract(theClass.getModifiers())) {
                descriptor = "a subclass of abstract";
            } else {
                descriptor = "an instance of";
            }

        } catch (Throwable t) {
            t.printStackTrace();
            // nice and simple fallback position
            descriptor = "the class";
        }

        // put together the final string

        return "slaps " + target + " around a bit with " + descriptor + " "
                + randomClass + " " + smiley;

    }

    // method for returning a random smiley to append

    public String getRandomSmiley() {

        String[] smileys = { ":)", ":P", ":D", "^^", "=)" };

        return smileys[(int) (Math.random() * smileys.length)];

    }

    // method to get a random class for slapping

    public String getRandomClass() {

        ClassInfo[] classes = RequestHandler.getInstance().getClasses();

        return classes[(int) (classes.length * Math.random())]
                .getQualifiedNameWithCase();

    }

    public String[] getHelp(IrcCommandEvent ice) {
        return null;
    }

    public void handleEvent(IrcCommandEvent ice) {
        if (ice.getCommand().equals("slap")) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(),
                    MessageTypes.ACTION, doSlap(ice.getMessageContent()));
        }

    }

}
