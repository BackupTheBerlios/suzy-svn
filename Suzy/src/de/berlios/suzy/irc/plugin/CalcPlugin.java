/*
 * Copyright (C) yyyy  name of author
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package de.berlios.suzy.irc.plugin;

import org.nfunk.jep.JEP;
import org.nfunk.jep.type.Complex;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.Plugin;

public class CalcPlugin implements Plugin {
    private JEP myParser;


    public String[] getCommands() {
        return new String[] {
                "calc"
        };
    }

    public String[] getRestrictedCommands() {
        return new String[] {
        };
    }

    public void handleEvent(IrcCommandEvent ice) {
        if (ice.getCommand().equals("calc")) {
            calc(ice);
        }
    }

    public CalcPlugin() {
        myParser = new JEP();
        myParser.addStandardConstants();
        myParser.addStandardFunctions();
        myParser.addComplex();
    }


    private void calc(IrcCommandEvent ice) {
        myParser.parseExpression(ice.getMessageContent());
        if (!myParser.hasError()) {
            Complex result = myParser.getComplexValue();
            if (result.im() != 0) {
                ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, myParser.getComplexValue().toString());
            } else {
                ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, String.valueOf(myParser.getComplexValue().re()));
            }
        } else {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "Error: "+myParser.getErrorInfo());
        }
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getHelp(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public String[] getHelp(IrcCommandEvent ice) {
        String message = ice.getMessageContent();

        if (message.equals("calcplugin")) {
            return new String[] {
                    "A calculator plugin. See "+ice.getPrefix()+"calc"
            };
        } else if (message.equals("calc")){
            return new String[] {
                    "Calculate the result for the given expression.",
                    "Example: "+ice.getPrefix()+"calc 13^4+i*sin(3)"
            };
        }


        return null;
    }

}
