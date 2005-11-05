package de.berlios.suzy.irc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This plugin is responsible for loading and unloading other plugins at runtime.
 * It will also keep a list of loaded plugins.
 * <br>
 * A LoaderPlugin cannot be loaded at Runtime.
 * <br>
 * <br>
 *
 * <TABLE border="1">
 *   <CAPTION>Commands available</CAPTION>
 *   <TR><TH>load<TD>*<TD>loads a plugin
 *   <TR><TH>unload<TD>*<TD>unloads a plugin
 *   <TR><TH>commands<TD><TD>shows all commands that are not access restricted
 *   <TR><TH>allcommands<TD>*<TD>shows all commands
 * </table>
 * * restricted
 *
 * @author honk
 */
public class LoaderPlugin implements Plugin {
    private Map<String, Plugin> pluginList = new HashMap<String, Plugin>();
    private Map<String, Plugin> restrictedPluginList = new HashMap<String, Plugin>();
    private List<PerformOnConnectPlugin> performOnConnectList = new ArrayList<PerformOnConnectPlugin>();

    private String network;

    /**
     * Creates a new instance.
     * The plugin lists in new instance will contain this plugin as well as all plugins specified by
     * the plugins.conf
     *
     */
    public LoaderPlugin(String network) {
        this.network = network;
        addPlugin(this);

        loadPluginList("plugins.conf");
        loadPluginList("plugins_"+network+".conf");
    }

    private void loadPluginList(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));

            String line;
            while((line=br.readLine()) != null) {
                try {
                    line = line.trim();
                    if (!line.startsWith("#") && line.length() != 0) {
                        Plugin p = loadPlugin(line);

                        addPlugin(p);
                    }
                } catch (Exception e) {
                    System.err.println("Cannot load plugin: "+line);
                    e.printStackTrace();
                }
            }

        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getCommands()
     */
    public String[] getCommands() {
        return new String[] {
                "commands",
        };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#getRestrictedCommands()
     */
    public String[] getRestrictedCommands() {
        return new String[] {
                "load",
                "unload",
                "allcommands"
        };
    }

    /* (non-Javadoc)
     * @see de.berlios.suzy.irc.Plugin#handleEvent(de.berlios.suzy.irc.IrcCommandEvent)
     */
    public void handleEvent(IrcCommandEvent ice) {
        if (ice.getCommand().equals("load")) {
            addPlugin(ice);
        } else if (ice.getCommand().equals("unload")) {
            removePlugin(ice);
        } else if (ice.getCommand().equals("commands")) {
            showUnrestrictedCommands(ice);
        } else if (ice.getCommand().equals("allcommands")) {
            showAllCommands(ice);
        }
    }


    private void showUnrestrictedCommands(IrcCommandEvent ice) {
        StringBuilder unrestrictedCommands = new StringBuilder();
        for (String s: pluginList.keySet()) {
            if (unrestrictedCommands.length() != 0) {
                unrestrictedCommands.append(", ");
            }
            unrestrictedCommands.append(s);
        }
        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, unrestrictedCommands.toString());
    }

    private void showAllCommands(IrcCommandEvent ice) {
        StringBuilder unrestrictedCommands = new StringBuilder();
        for (String s: pluginList.keySet()) {
            if (unrestrictedCommands.length() != 0) {
                unrestrictedCommands.append(", ");
            }
            unrestrictedCommands.append(s);
        }
        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, unrestrictedCommands.toString());

        StringBuilder restrictedCommands = new StringBuilder();
        for (String s: restrictedPluginList.keySet()) {
            if (restrictedCommands.length() != 0) {
                restrictedCommands.append(", ");
            }
            restrictedCommands.append(s);
        }
        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, restrictedCommands.toString());
    }

    private String addPlugin(Plugin p) {
        StringBuilder sb = new StringBuilder();
        String[] actions = p.getCommands();
        for (int i = 0; i < actions.length; i++) {
	        if (actions[i].indexOf(':') != -1) {
				// invalid; reject and move on
		    	if (sb.length() != 0) {
		            sb.append(", ");
		        }
		        sb.append(actions[i]);
		        sb.append(" (Not accessible! Remove colon in name)");
		        continue;
	        } else {
	        	System.out.println("accepted: "+actions[i]);
	        }
            Plugin oldP = pluginList.get(actions[i]);
            if (oldP == null || oldP.getClass().getName().equals(p.getClass().getName())) {
            	pluginList.put(actions[i], p);
            } else {
            	String className = p.getClass().getSimpleName();
            	if (className.endsWith("Plugin")) {
	            	int index = className.lastIndexOf("Plugin");
	            	String adjustedClassName = className.substring(0, index).toLowerCase();
					actions[i] = adjustedClassName + ":" + actions[i];
					pluginList.put(actions[i], p);
				} else {
					if (sb.length() != 0) {
			            sb.append(", ");
			        }
			        sb.append(actions[i]);
			        sb.append(" (Not accessible! Duplicate exists and class name does not follow standard format)");
		        	continue;
				}
            }
	    	if (sb.length() != 0) {
	            sb.append(", ");
	        }
	        sb.append(actions[i]);
        }
        
        String[] restrictedActions = p.getRestrictedCommands();
		for (int i = 0; i < restrictedActions.length; i++) {
	        if (restrictedActions[i].indexOf(':') != -1) {
				// invalid; reject and move on
		    	if (sb.length() != 0) {
		            sb.append(", ");
		        }
		        sb.append(restrictedActions[i]);
		        sb.append(" (Not accessible! Remove colon in name)");
		        continue;
	        } else {
	        	System.out.println("accepted: "+restrictedActions[i]);
	        }
            Plugin oldP = restrictedPluginList.get(restrictedActions[i]);
            if (oldP == null || oldP.getClass().getName().equals(p.getClass().getName())) {
            	restrictedPluginList.put(restrictedActions[i], p);
            } else {
            	String className = p.getClass().getSimpleName();
            	if (className.endsWith("Plugin")) {
	            	int index = className.lastIndexOf("Plugin");
	            	String adjustedClassName = className.substring(0, index).toLowerCase();
					restrictedActions[i] = adjustedClassName + ":" + restrictedActions[i];
					restrictedPluginList.put(restrictedActions[i], p);
				} else {
					if (sb.length() != 0) {
			            sb.append(", ");
			        }
			        sb.append(restrictedActions[i]);
			        sb.append(" (Not accessible! Duplicate exists and class name does not follow standard format)");
		        	continue;
				}
            }
	    	if (sb.length() != 0) {
	            sb.append(", ");
	        }
	        sb.append(restrictedActions[i]);
        }

        if (p instanceof PerformOnConnectPlugin) {
            performOnConnectList.add((PerformOnConnectPlugin)p);
        }

        return sb.toString();
    }

    private void removePlugin(Plugin p) {
        String[] actions = p.getCommands();
        for (String action : actions) {
            pluginList.remove(action);
        }

        String[] restrictedActions = p.getRestrictedCommands();
        for (String action : restrictedActions) {
            restrictedPluginList.remove(action);
        }

        if (p instanceof PerformOnConnectPlugin) {
            performOnConnectList.remove((PerformOnConnectPlugin)p);
        }
    }

    /**
     * Returns a List containing all plugins that expect to be called
     * whenever a new irc connection is opened.
     * @return a list of loaded plugins which want to perform something on connect
     */
    public List<PerformOnConnectPlugin> getPerformOnConnectList() {
        return performOnConnectList;
    }

    /**
     * Returns a mapping of commands and Plugins executing these.
     * @return a mapping of commands and Plugins executing these.
     */
    public Map<String, Plugin> getPluginList() {
        return pluginList;
    }

    /**
     * Returns a mapping of access restricted commands and Plugins executing these.
     * @return a mapping of access restricted commands and Plugins executing these.
     */
    public Map<String, Plugin> getRestrictedPluginList() {
        return restrictedPluginList;
    }



    private void removePlugin(IrcCommandEvent ice) {
        try {
            Plugin p = loadPlugin(ice.getMessageContent());
            removePlugin(p);
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "Unloaded successfully");
        } catch (Exception e) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "Unloading failed: "+e.getMessage());
            e.printStackTrace();
        }

    }


    private void addPlugin(IrcCommandEvent ice) {
        try {
            Plugin p = loadPlugin(ice.getMessageContent());
            String commands = addPlugin(p);
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "Loaded commands: "+commands);
         } catch (Throwable t) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "Loading failed: "+t.getMessage());
            t.printStackTrace();
        }
    }



    @SuppressWarnings("unchecked")
    private Plugin loadPlugin(String className) throws IOException,
            ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, InstantiationException
    {
    	final String pluginPackage;
    	final boolean pluginWithoutPackage;
    	int lastDotIndex = className.lastIndexOf('.');
    	if (lastDotIndex != -1) {
    		pluginWithoutPackage = false;
    		pluginPackage = className.substring(0, lastDotIndex);
    	} else {
    		pluginWithoutPackage = true;
    		pluginPackage = null;
    	}
        ClassLoader cl = new ClassLoader() {
            public Class<?> loadClass(String className) throws ClassNotFoundException {
                if ((pluginWithoutPackage && className.contains(".")) 
                    || (!pluginWithoutPackage && !className.startsWith(pluginPackage))) {
                	return getParent().loadClass(className);
                }
                String classLocation = "/" + className.replace('.','/') + ".class";
				
                URL url = getClass().getResource(classLocation);
                
                try {
                    InputStream is = url.openStream();
                    byte[] data = new byte[10240];

                    int length;
                    int total = 0;
                    while ((length = is.read(data, total, data.length-total)) != -1) {
                        total += length;
                        byte[] tmp = new byte[data.length*2];
                        System.arraycopy(data, 0, tmp, 0, data.length);
                        data = tmp;
                    }

                    Class<?> c = defineClass(className, data, 0, total);

                    return c;
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                throw new ClassNotFoundException("Cannot find class "
                        + className + ".");
            }
        };


        Class<Plugin> c = (Class<Plugin>)cl.loadClass(className);

        Plugin plugin;

        try {   //try constructor with server first
            Constructor<Plugin> constructor = c.getConstructor(new Class[] {String.class});
            plugin = constructor.newInstance(new Object[] {network});
        } catch (NoSuchMethodException nme) {   //try constructor without server as argument
            Constructor<Plugin> constructor = c.getConstructor(new Class[] {});
            plugin = constructor.newInstance(new Object[] {});
        }

        System.out.println("--- loading plugin: " + className + " with actions: ");
        System.out.println("  - " + Arrays.toString(plugin.getCommands()));
        System.out.println("  - " + Arrays.toString(plugin.getRestrictedCommands()));
        System.out.println("  - hash: "+c.hashCode());


        return plugin;
    }
}
