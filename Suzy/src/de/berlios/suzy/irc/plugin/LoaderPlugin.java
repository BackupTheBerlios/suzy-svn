package de.berlios.suzy.irc.plugin;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.berlios.suzy.irc.IrcCommandEvent;
import de.berlios.suzy.irc.MessageTypes;
import de.berlios.suzy.irc.PerformOnConnectPlugin;
import de.berlios.suzy.irc.Plugin;

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
	public final String DEFAULT_PACKAGE = "de.berlios.suzy.irc.plugin";
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
        } catch(Throwable t) {
        	t.printStackTrace();
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
                "allcommands",
                "admincommands"
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
        } else if (ice.getCommand().equals("admincommands")) {
            showRestrictedCommands(ice);
        } else if (ice.getCommand().equals("allcommands")) {
            showAllCommands(ice);
        }
    }


    private void showUnrestrictedCommands(IrcCommandEvent ice) {
        StringBuilder unrestrictedCommands = new StringBuilder("Available commands: ");
        for (Iterator<String> it = pluginList.keySet().iterator(); it.hasNext();) {
        	String command = it.next();
            unrestrictedCommands.append(command);
            if (it.hasNext()) {
                unrestrictedCommands.append(", ");
            }

        }
        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, unrestrictedCommands.toString());
    }

    private void showRestrictedCommands(IrcCommandEvent ice) {
        StringBuilder restrictedCommands = new StringBuilder("Admin commands: ");
        for (Iterator<String> it = restrictedPluginList.keySet().iterator(); it.hasNext();) {
        	String command = it.next();
            restrictedCommands.append(command);
            if (it.hasNext()) {
                restrictedCommands.append(", ");
            }

        }
        ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, restrictedCommands.toString());
    }

    private void showAllCommands(IrcCommandEvent ice) {
        showUnrestrictedCommands(ice);
        showRestrictedCommands(ice);
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

    private String removePlugin(Plugin p) {
    	StringBuilder sb = new StringBuilder();
    	String pluginName = p.getClass().getName();

    	List<String> toRemove = new ArrayList<String>();
    	Set<Map.Entry<String,Plugin>> actionSet = pluginList.entrySet();
    	for (Map.Entry<String,Plugin> action : actionSet) {
            if (action.getValue().getClass().getName().equals(pluginName)) {
            	toRemove.add(action.getKey());
		    	if (sb.length() != 0) {
		            sb.append(", ");
		        }
		        sb.append(action.getKey());
            }
        }
        for (String key : toRemove) {
        	pluginList.remove(key);
        }

		toRemove = new ArrayList<String>();
    	Set<Map.Entry<String,Plugin>> restrictedActionSet = restrictedPluginList.entrySet();
        for (Map.Entry<String,Plugin> action : restrictedActionSet) {
            if (action.getValue().getClass().getName().equals(pluginName)) {
            	toRemove.add(action.getKey());
		    	if (sb.length() != 0) {
		            sb.append(", ");
		        }
		        sb.append(action.getKey());
            }
        }
        for (String key : toRemove) {
        	restrictedPluginList.remove(key);
        }
        return sb.toString();
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
            String commands = removePlugin(p);
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "Unloaded successfully: "+commands);
        } catch (Throwable t) {
            ice.getSource().sendMessageTo(ice.getTarget().getDefaultTarget(), MessageTypes.PRIVMSG, "Unloading failed: "+t.getMessage());
           t.printStackTrace();
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

   private Plugin loadPlugin(String className) throws IOException,
            ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalArgumentException,
            IllegalAccessException, InstantiationException
    {
    	final String initPluginPackage;
    	final boolean initPluginWithoutPackage;
    	int lastDotIndex = className.lastIndexOf('.');
    	if (lastDotIndex != -1) {
    		initPluginWithoutPackage = false;
    		initPluginPackage = className.substring(0, lastDotIndex);
    	} else {
    		initPluginWithoutPackage = true;
    		initPluginPackage = null;
    	}
        ClassLoader cl = new ClassLoader() {
        	String pluginPackage = initPluginPackage;
        	boolean pluginWithoutPackage = initPluginWithoutPackage;
            public Class<?> loadClass(String className) throws ClassNotFoundException {
                if ((pluginWithoutPackage && className.contains("."))
                    || (!pluginWithoutPackage && !className.startsWith(pluginPackage))) {
                	return getParent().loadClass(className);
                }
 				String currentClassName = className;
            	String classLocation = "/" + className.replace('.','/') + ".class";
                URL url = getClass().getResource(classLocation);
                if (url == null && pluginWithoutPackage) {
                	// it could be in the default plugin package
                	url = getClass().getResource("/" + DEFAULT_PACKAGE.replace('.','/') + classLocation);
                	if (url != null) {
                		className = DEFAULT_PACKAGE + "." + className;
                		pluginPackage = DEFAULT_PACKAGE;
                		pluginWithoutPackage = false;
                	}
                }

				if (url == null) {
					// possibly short hand name?
					String simpleClassName;
					String packageName;
					if (pluginWithoutPackage) {
						simpleClassName = className;
						packageName = "";
					} else {
						simpleClassName = className.substring(className.lastIndexOf('.')+1);
						packageName =  className.substring(0, className.lastIndexOf('.'));

					}
					className = packageName + simpleClassName.substring(0, 1).toUpperCase() +
								simpleClassName.substring(1) + "Plugin";
	            	classLocation = "/" + className.replace('.','/') + ".class";
	                url = getClass().getResource(classLocation);

	                if (url == null && pluginWithoutPackage) {
	                	// it could be in the default plugin package
	                	url = getClass().getResource("/" + DEFAULT_PACKAGE.replace('.','/') + classLocation);
	                	className = DEFAULT_PACKAGE + "." + className;
	                	pluginPackage = DEFAULT_PACKAGE;
	                	pluginWithoutPackage = false;
	                }
				}
				if (url == null) {
                	throw new ClassNotFoundException("Cannot find class "
                        + currentClassName + ".");
				}
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


        Class<?> c = (Class<?>)cl.loadClass(className);

        Plugin plugin;

        try {   //try constructor with server first
            Constructor<?> constructor = c.getConstructor(new Class[] {String.class});
            plugin = (Plugin)constructor.newInstance(new Object[] {network});
        } catch (NoSuchMethodException nme) {   //try constructor without server as argument
            Constructor<?> constructor = c.getConstructor(new Class[] {});
            plugin = (Plugin)constructor.newInstance(new Object[] {});
        }

        System.out.println("--- loading plugin: " + className + " with actions: ");
        System.out.println("  - " + Arrays.toString(plugin.getCommands()));
        System.out.println("  - " + Arrays.toString(plugin.getRestrictedCommands()));
        System.out.println("  - hash: "+c.hashCode());


        return plugin;
    }

   /* (non-Javadoc)
    * @see de.berlios.suzy.irc.Plugin#getHelp(de.berlios.suzy.irc.IrcCommandEvent)
    */
   public String[] getHelp(IrcCommandEvent ice) {
       String message = ice.getMessageContent();

       if (message.equals("loaderplugin")) {
           return new String[] {
                   "Loads and unloads plugins at runtime.",
           };
       } else if (message.equals("load")){
           return new String[] {
                   "Loads the specified plugin. Short names (api instead of ApiPlugin possible).",
           };
       } else if (message.equals("unload")){
           return new String[] {
                   "Unloads the specified plugin. Short names (api instead of ApiPlugin possible).",
           };
       } else if (message.equals("commands")){
           return new String[] {
                   "Lists all non-admin commands currently registered.",
           };
       } else if (message.equals("allcommands")){
           return new String[] {
                   "Lists all commands currently registered.",
           };
       } else if (message.equals("admincommands")){
           return new String[] {
                   "Lists all admin commands currently registered.",
           };
       }

       return null;
   }
}
