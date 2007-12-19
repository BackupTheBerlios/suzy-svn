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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.Plugin;

public class ExecPlugin implements Plugin {
    private final static int DELAY = 1000;


    public String[] getCommands() {
        return new String[] { "exec", "sysout" };
    }

    public String[] getRestrictedCommands() {
        return new String[] {/* "exec"*/ };
    }

    public void handleEvent(IrcCommandEvent ice) {
        /*if (ice.getCommand().equals("execp")) {
            exec(ice, false, false);
        } else */
        
        if (ice.getCommand().equals("exec")) {
            exec(ice, true, false);
        } else if (ice.getCommand().equals("sysout")) {
            exec(ice, true, true);
        }
    }

    public ExecPlugin() {
    }


    private void exec(IrcCommandEvent ice, boolean isChanMessage, boolean isSysoutOnly) {
        StringWriter compilerOutput = new StringWriter();
        String file = "execPlugin/Exec.java";

        List<String> output = Collections.synchronizedList(new ArrayList<String>());
        String target = isChanMessage ? ice.getTarget().getDefaultTarget() : ice.getTarget().getUser();


        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
            writer.println("import java.util.*;");
            writer.println("import java.util.concurrent.*;");
            writer.println("import java.util.regex.*;");
            writer.println("import java.util.zip.*;");
            writer.println("import java.lang.reflect.*;");
            writer.println("import java.math.*;");
            writer.println("import java.text.*;");
            writer.println("public class Exec {");
            writer.println("    public static void sysout(Object object) {");
            writer.println("        if (object instanceof Object[]) {");
            writer.println("            System.out.println(Arrays.deepToString((Object[])object));");
            writer.println("        } else if (object instanceof byte[]) {");
            writer.println("            System.out.println(Arrays.toString((byte[])object));");
            writer.println("        } else if (object instanceof short[]) {");
            writer.println("            System.out.println(Arrays.toString((short[])object));");
            writer.println("        } else if (object instanceof int[]) {");
            writer.println("            System.out.println(Arrays.toString((int[])object));");
            writer.println("        } else if (object instanceof long[]) {");
            writer.println("            System.out.println(Arrays.toString((long[])object));");
            writer.println("        } else if (object instanceof float[]) {");
            writer.println("            System.out.println(Arrays.toString((float[])object));");
            writer.println("        } else if (object instanceof double[]) {");
            writer.println("            System.out.println(Arrays.toString((double[])object));");
            writer.println("        } else if (object instanceof char[]) {");
            writer.println("            char[] chars = (char[])object;");
            writer.println("            String[] out = new String[chars.length];");
            writer.println("            for (int i=0;i<chars.length;i++) {");
            writer.println("                if (Character.isISOControl(chars[i])) {");
            writer.println("                    out[i] = \"\\\\\"+(int)chars[i];");
            writer.println("                } else {");
            writer.println("                    out[i] = Character.toString(chars[i]);");
            writer.println("                }");
            writer.println("            }");
            writer.println("            System.out.println(Arrays.toString(out));");
            writer.println("        } else if (object instanceof boolean[]) {");
            writer.println("            System.out.println(Arrays.toString((boolean[])object));");
            writer.println("        } else {");
            writer.println("            System.out.println(object);");
            writer.println("        }");
            writer.println("    }");
            writer.println("    public static void main(String[] args) throws Throwable {");
            writer.println(!isSysoutOnly?ice.getMessageContent() : "sysout(" + ice.getMessageContent()+"\n);");
            writer.println("    }");
            writer.println("}");

        } catch (Exception e) {
            e.printStackTrace();

            ice.getSource().sendMessageTo(target, MessageTypes.PRIVMSG, e.toString());

            return;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }


        int error = com.sun.tools.javac.Main.compile(new String[] { file }, new PrintWriter(compilerOutput));
        if (error != 0) {

            String[] compOut = compilerOutput.toString().split("[\n\r]+");
            for (String out : compOut) {
                output.add(out);
            }
        } else {
            try {
                final Process process = Runtime.getRuntime().exec("/bin/sh execPlugin.sh");
                new StreamReader(process.getInputStream(), output).start();
                new StreamReader(process.getErrorStream(), output).start();

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        process.destroy();
                    }
                }, DELAY);

                process.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
                output.add("Error executing: " + e.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
                output.add("Error executing: " + e.toString());
            }
        }

        //int count = isAdminCmd ? 3 : 2;
        int count = 3;
        for (int i = 0; i < Math.min(count, output.size()); i++) {
            ice.getSource().sendMessageTo(target, MessageTypes.PRIVMSG, output.get(i).substring(0, Math.min(128, output.get(i).length())));
        }
        
        if (output.size() == 0) {
            ice.getSource().sendMessageTo(target, MessageTypes.PRIVMSG, "Execution successful. No output.");
        }
    }

    class StreamReader extends Thread {

        private InputStream stream;
        private List<String> output;

        public StreamReader(InputStream stream, List<String> output) {
            this.stream = stream;
            this.output = output;
        }


        @Override
        public void run() {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    output.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getHelp(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public String[] getHelp(IrcCommandEvent ice) {
        String message = ice.getMessageContent();

        if (message.equals("execplugin")) {
            return new String[] { "Plugin that allows random code execution. See " + ice.getPrefix() + "exec" };
        } else if (message.equals("exec")) {
            return new String[] { "Compile and execute the given java code.",
                    "Example: " + ice.getPrefix() + "exec System.out.println(3 + 5);" };
        } else if (message.equals("sysout")) {
            return new String[] { "Compile and execute the given java code. Answer in private.",
                    "Example: " + ice.getPrefix() + "sysout 3 + Math.random()" };
        } /*else if (message.equals("execp")) {
            return new String[] { "Compile and execute the given java code. Answer in private.",
                    "Example: " + ice.getPrefix() + "exec System.out.println(3 + 5);" };
        }*/


        return null;
    }

}
