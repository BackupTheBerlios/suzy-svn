package de.berlios.suzy.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * RequestHandler is responsible for searching inside a parsed api tree.
 * The tree is expected to be inside api.dat. If no tree is found,
 * RequestHandler will use {@link Parser} to get a new api.dat.
 *
 * @author honk
 */
public class RequestHandler {
    private static ClassInfo[] classes;

    private static RequestHandler instance;

    /**
     * Get a shared ReqestHandler instance. The API is loaded only on the
     * first execution of this method. Subsequent calls will use the same
     * data.
     * @return a shared instance of RequestViewer
     */
    public synchronized static RequestHandler getInstance() {
        if (instance == null) {
            instance = new RequestHandler();
        }
        return instance;
    }




    private RequestHandler() {
        if (classes == null) {
            classes = Parser.read();
        }
    }

    /**
     * Forces the RequestHandler to reload api.dat.
     */
    public void reload() {
        classes = Parser.read();
    }


    private String[] processPattern(String pattern) {
        boolean lastCharWasStar = false;
        ArrayList<String> splitPattern = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int start = 0;

        if (pattern.charAt(0) == '*') {
            splitPattern.add("");
            lastCharWasStar = true;
            start = 1;
        }

        for (int i=start;i<pattern.length();i++) {
            char c = pattern.charAt(i);

            if (c == '*') {
                if (!lastCharWasStar) {
                    splitPattern.add(sb.toString());
                    //splitPattern.add("");
                    sb = new StringBuilder();
                    lastCharWasStar = true;
                }
            } else {
                sb.append(Character.toLowerCase(c));
                lastCharWasStar = false;
            }
        }

        if (pattern.charAt(pattern.length()-1) == '*') {
            splitPattern.add("");
        } else {
            if (!lastCharWasStar) {
                splitPattern.add(sb.toString());
            }
        }


        return splitPattern.toArray(new String[splitPattern.size()]);
    }

    /**
     * Searches classes for the pattern specified. If a result is found,
     * the matching URL will be added to a List.
     * <br>
     * The pattern will be parsed to strip all '*' from it. The rest of the pattern
     * is matched against the class. At places where a '*' was, an arbitrary
     * number of characters will be allowed.
     *
     * @param pattern pattern to be matched
     * @return a List containing all matches
     */
    public List<String> parseClasses(String pattern) {
        String[] splitPattern = processPattern(pattern);

        List<String> classes = findClasses(splitPattern);

        return classes;
    }

    /**
     * Searches methods for the pattern specified. If a result is found,
     * the matching URL will be added to a List.
     * <br>
     * The pattern will be parsed to strip all '*' from it. The rest of the pattern
     * is matched against the method. At places where a '*' was, an arbitrary
     * number of characters will be allowed.
     *
     * @param pattern pattern to be matched
     * @return a List containing all matches
     */
    public List<String> parseMethods(String pattern) {
        String[] splitPattern = processPattern(pattern);

        List<String> methods = findMethods(splitPattern);

        return methods;
    }

    /**
     * Searches classes and methods for the pattern specified. If a result is found,
     * the matching URL will be added to a List.
     * <br>
     * The pattern will be parsed to strip all '*' from it. The rest of the pattern
     * is matched against the class or method. At places where a '*' was, an arbitrary
     * number of characters will be allowed.
     *
     * @param pattern pattern to be matched
     * @return a List containing all matches
     */
    public List<String> parseAll(String pattern) {
        String[] splitPattern = processPattern(pattern);

        List<String> results = findClasses(splitPattern);
        results = findMethods(splitPattern, results);

        return results;
    }

    private List<String> findMethods(String[] splitPattern) {
        return findMethods(splitPattern, new ArrayList<String>());
    }

    private List<String> findMethods(String[] splitPattern, List<String> results) {
        for (ClassInfo ci: classes) {
            for (MethodInfo mi: ci.getMetods()) {
                if (!mi.getQualifiedName().endsWith(splitPattern[splitPattern.length-1])) {
                    continue;
                }
                if (!mi.getQualifiedName().startsWith(splitPattern[0])) {
                    if (!mi.getHalfQualifiedName().startsWith(splitPattern[0])) {
                        if (!mi.getName().startsWith(splitPattern[0])) {
                            continue;
                        }
                    }
                }
                int oldIndexOf = -1;
                boolean found = true;

                for (String currentPattern: splitPattern) {
                    if (currentPattern.length() == 0) {
                        continue;
                    }
                    int indexOf = mi.getQualifiedName().indexOf(currentPattern, oldIndexOf+1);
                    if (indexOf == -1) {
                        found = false;
                        break;
                    }
                    oldIndexOf = indexOf;
                }
                if (found) {
                    if (splitPattern.length == 1) { //special case: ending == beginning (e.g. Integer.getInteger, no wildcard)
                        if (!mi.getQualifiedName().equals(splitPattern[0])
                                && !mi.getHalfQualifiedName().equals(splitPattern[0])
                                && !mi.getName().equals(splitPattern[0])) {
                            continue;
                        }
                    }

                    results.add(mi.getURL());
                }
            }
        }
        return results;
    }



    private List<String> findClasses(String[] splitPattern) {
        return findClasses(splitPattern, new ArrayList<String>());
    }

    private List<String> findClasses(String[] splitPattern, List<String> results) {
        for (ClassInfo ci: classes) {
            if (!ci.getQualifiedName().endsWith(splitPattern[splitPattern.length-1])) {
                 continue;
            }
            if (!ci.getQualifiedName().startsWith(splitPattern[0])) {
                if (!ci.getName().startsWith(splitPattern[0])) {
                    continue;
                }
            }
            int oldIndexOf = -1;
            boolean found = true;
            for (String currentPattern: splitPattern) {
                if (currentPattern.length() == 0) {
                    continue;
                }
                int indexOf = ci.getQualifiedName().indexOf(currentPattern, oldIndexOf+1);
                if (indexOf == -1) {
                    found = false;
                    break;
                }
                oldIndexOf = indexOf;
            }
            if (found) {
                if (splitPattern.length == 1) { //special case: ending == beginning (no wildcard)
                    if (!ci.getQualifiedName().equals(splitPattern[0])
                            && !ci.getName().equals(splitPattern[0])) {
                        continue;
                    }
                }

                results.add(ci.getURL());
            }
        }
        return results;
    }

}
