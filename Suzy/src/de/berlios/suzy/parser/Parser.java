package de.berlios.suzy.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;

/**
 * The Parser class will create a List of classes. Each class will contain information about all methods and fields.
 * The entire api available will be written to a file api.dat. It can be read by invoking {@link #read()}. The parsing
 * does require a lot of memory and takes quite some time, be patient. Once the api.dat file is created, it will no
 * be changed.
 * <br>
 * suggested when creating a new api.dat: -Xmx1024M
 * <br>
 * api.dat is just a List<ClassInfo> written using an ObjectOutputStream and read using an ObjectInputStream.
 *
 * @author honk
 */
public class Parser {
    private final static String parserPath = "bin/";

    private static void createTree() {
        Process javadocProcess;
        List<ParseEntry> parseEntries = null;
        try {
            parseEntries = readConfig();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        for (ParseEntry parseEntry: parseEntries) {
            System.out.println("******* "+parseEntry.getPath()+" *******");
            try {
                javadocProcess = Runtime.getRuntime().exec("javadoc -sourcepath "+parseEntry.getPath()+" -J-Xmx1024m -J-DbaseUrl="+parseEntry.getBaseUrl()+" -subpackages "+parseEntry.getPackages()+" -doclet de.berlios.suzy.parser.Parser -docletpath "+parserPath);
                new ReaderThread(javadocProcess.getInputStream()).start();
                new ReaderThread(javadocProcess.getErrorStream()).start();

                try {
                    javadocProcess.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        }

        List<ClassInfo> l = readTemp();
        write(l);

        File f = new File("api-temp.dat");
        f.delete();
    }

    private static List<ParseEntry> readConfig() throws IOException, NoSuchElementException {
        List<ParseEntry> config = new ArrayList<ParseEntry>();

        String path = null;
        String packages = null;
        String baseUrl = null;

        Scanner scanner = new Scanner(new File("parser.conf"));


        while(scanner.hasNext()) {

            path = getLine(scanner);
            packages = getLine(scanner);
            baseUrl = getLine(scanner);
            if (!baseUrl.endsWith("/")) {
                baseUrl = baseUrl + "/";
            }

            config.add(new ParseEntry(path, packages, baseUrl));
        }

        scanner.close();

        return config;
    }

    private static String getLine(Scanner scanner) {
        String line;
        do {
            line = scanner.nextLine().trim();
        } while (line.startsWith("//") || line.length() == 0);

        return line;
    }

    private static class ParseEntry {
        private String path;
        private String packages;
        private String baseUrl;
        public ParseEntry(String path, String packages, String baseUrl) {
            this.path = path;
            this.packages = packages;
            this.baseUrl = baseUrl;
        }
        public String getBaseUrl() {
            return baseUrl;
        }
        public String getPackages() {
            return packages;
        }
        public String getPath() {
            return path;
        }
    }


    /**
     * The entry point for javadoc. This method actually creates all
     * @param root the root node javadoc gives
     * @return true
     */
    public static boolean start(RootDoc root) {
        List<ClassInfo> classes = readTemp();

        String baseUrl = System.getProperty("baseUrl");

        for (ClassDoc classDoc : root.classes()) {
            ClassInfo ci = new ClassInfo(classDoc.name(), classDoc.qualifiedName(), baseUrl);

            do {
                ClassInfo superCi = new ClassInfo(classDoc.name(), classDoc.qualifiedName(), baseUrl);

                for (MethodDoc md : classDoc.methods(true)) {
                    MethodInfo mi = new MethodInfo(ci, md.name(), md.signature(), baseUrl, superCi);
                    ci.addMethod(mi);
                }

                for (FieldDoc fd : classDoc.fields(true)) {
                    FieldInfo fi = new FieldInfo(fd.name(), fd.type().qualifiedTypeName());
                    ci.addField(fi);
                }

                Type superClass = classDoc.superclassType();
                if (superClass == null) {
                    classDoc = null;
                } else {
                    classDoc = superClass.asClassDoc();
                }

            } while (classDoc != null);

            classes.add(ci);

            ci.update();
        }

        writeTemp(classes);
        return true;
    }

    private static void write(List<ClassInfo> list) {
        try {
            File f = new File("api.dat");
            f.createNewFile();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(list.toArray(new ClassInfo[list.size()]));
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeTemp(List<ClassInfo> list) {
        try {
            File f = new File("api-temp.dat");
            f.createNewFile();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(list);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * Reads the file api.dat and creates an array containsing all classes in form of
     * ClassInfo objects. Every class contains a List with methods as MethodInfo
     * and fields as FieldInfo.
     * @return a List containing all classes from the available api
     */
    @SuppressWarnings("unchecked")
    public static ClassInfo[] read() {
        try {
            File f = new File("api.dat");
            if (!f.exists()) {
                //Parser.createTree("classes/", "bin/", "com:java:javax:org:sun:sunw");
                Parser.createTree();
            }

            f = new File("api.dat");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            ClassInfo[] list = (ClassInfo[])ois.readObject();
            ois.close();
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    private static List<ClassInfo> readTemp() {
        try {
            File f = new File("api-temp.dat");
            if (!f.exists()) {
                return new ArrayList<ClassInfo>(1024);
            }

            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            List<ClassInfo> list = (List<ClassInfo>)ois.readObject();
            ois.close();
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static class ReaderThread extends Thread{
        private BufferedReader br;
        public ReaderThread(InputStream is) {
            this.br = new BufferedReader(new InputStreamReader(is));
        }
        public void run() {
            String line;
            try {
                while ((line=br.readLine())!=null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
