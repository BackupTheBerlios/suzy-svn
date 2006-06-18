package de.berlios.suzy.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Very simple SearchUtil class refactored from {@link RequestHandler}.
 * 
 * @author Antubis
 */
public class ApiSearchUtil {

    /**
     * number of classes that are parsed
     * 
     * @return number of classes that are parsed
     */
    public static int classCount(ClassInfo[] classes) {
        return classes.length;
    }

    /**
     * number of methods that are parsed
     * 
     * @return number of classes that are parsed
     */
    public static int methodCount(ClassInfo[] classes) {
        int methods = 0;
        for (ClassInfo ci : classes) {
            methods += ci.getMetods().length;
        }
        return methods;
    }

    /**
     * Searches classes for the pattern specified. If a result is found, the
     * matching URL will be added to a List. <br>
     * The pattern will be parsed to strip all '*' from it. The rest of the
     * pattern is matched against the class. At places where a '*' was, an
     * arbitrary number of characters will be allowed.
     * 
     * @param pattern
     *            pattern to be matched
     * @return a List containing all matches
     */
    public static Set<String> parseClasses(ClassInfo[] classes, String pattern) {
        String[] splitPattern = processPattern(pattern);

        Set<String> foundClasses = findClasses(classes, splitPattern);

        return foundClasses;
    }

    /**
     * Searches methods for the pattern specified. If a result is found, the
     * matching URL will be added to a List. <br>
     * The pattern will be parsed to strip all '*' from it. The rest of the
     * pattern is matched against the method. At places where a '*' was, an
     * arbitrary number of characters will be allowed.
     * 
     * @param pattern
     *            pattern to be matched
     * @return a List containing all matches
     */
    public static Set<String> parseMethods(ClassInfo[] classes, String pattern) {
        String[] splitPattern = processPattern(pattern);

        Set<String> methods = findMethods(classes, splitPattern);

        return methods;
    }

    /**
     * Searches classes and methods for the pattern specified. If a result is
     * found, the matching URL will be added to a List. <br>
     * The pattern will be parsed to strip all '*' from it. The rest of the
     * pattern is matched against the class or method. At places where a '*'
     * was, an arbitrary number of characters will be allowed.
     * 
     * @param pattern
     *            pattern to be matched
     * @return a List containing all matches
     */
    public static Set<String> parseAll(ClassInfo[] classes, String pattern) {
        String[] splitPattern = processPattern(pattern);

        Set<String> results = findClasses(classes, splitPattern);
        results = findMethods(classes, splitPattern, results);

        return results;
    }

    private static String[] processPattern(String pattern) {
        boolean lastCharWasStar = false;
        ArrayList<String> splitPattern = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int start = 0;
        if (pattern.charAt(0) == '*') {
            splitPattern.add("");
            lastCharWasStar = true;
            start = 1;
        }
        for (int i = start; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '*') {
                if (!lastCharWasStar) {
                    splitPattern.add(sb.toString());
                    // splitPattern.add("");
                    sb = new StringBuilder();
                    lastCharWasStar = true;
                }
            } else {
                sb.append(Character.toLowerCase(c));
                lastCharWasStar = false;
            }
        }
        if (pattern.charAt(pattern.length() - 1) == '*') {
            splitPattern.add("");
        } else {
            if (!lastCharWasStar) {
                splitPattern.add(sb.toString());
            }
        }
        return splitPattern.toArray(new String[splitPattern.size()]);
    }

    private static Set<String> findMethods(ClassInfo[] classes,
            String[] splitPattern) {
        return findMethods(classes, splitPattern, new HashSet<String>());
    }

    private static Set<String> findMethods(ClassInfo[] classes,
            String[] splitPattern, Set<String> results) {
        for (ClassInfo ci : classes) {
            for (MethodInfo mi : ci.getMetods()) {
                if (!mi.getQualifiedName().endsWith(
                        splitPattern[splitPattern.length - 1])) {
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

                for (String currentPattern : splitPattern) {
                    if (currentPattern.length() == 0) {
                        continue;
                    }
                    int indexOf = mi.getQualifiedName().indexOf(currentPattern,
                            oldIndexOf + 1);
                    if (indexOf == -1) {
                        found = false;
                        break;
                    }
                    oldIndexOf = indexOf;
                }
                if (found) {
                    if (splitPattern.length == 1) { // special case: ending ==
                                                    // beginning (e.g.
                                                    // Integer.getInteger, no
                                                    // wildcard)
                        if (!mi.getQualifiedName().equals(splitPattern[0])
                                && !mi.getHalfQualifiedName().equals(
                                        splitPattern[0])
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

    private static Set<String> findClasses(ClassInfo[] classes,
            String[] splitPattern) {
        return findClasses(classes, splitPattern, new HashSet<String>());
    }

    private static Set<String> findClasses(ClassInfo[] classes,
            String[] splitPattern, Set<String> results) {
        for (ClassInfo ci : classes) {
            if (!ci.getQualifiedName().endsWith(
                    splitPattern[splitPattern.length - 1])) {
                continue;
            }
            if (!ci.getQualifiedName().startsWith(splitPattern[0])) {
                if (!ci.getName().startsWith(splitPattern[0])) {
                    continue;
                }
            }
            int oldIndexOf = -1;
            boolean found = true;
            for (String currentPattern : splitPattern) {
                if (currentPattern.length() == 0) {
                    continue;
                }
                int indexOf = ci.getQualifiedName().indexOf(currentPattern,
                        oldIndexOf + 1);
                if (indexOf == -1) {
                    found = false;
                    break;
                }
                oldIndexOf = indexOf;
            }
            if (found) {
                if (splitPattern.length == 1) { // special case: ending ==
                                                // beginning (no wildcard)
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
