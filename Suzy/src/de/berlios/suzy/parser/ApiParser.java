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

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;

public class ApiParser {

	protected final static String parserPath = "bin/";

	protected List<ParseEntry> parseEntries;

	protected ClassInfo[] classInfos;

	protected String fileName;

	public ApiParser(String fileName, List<ParseEntry> parseEntries) {
		if (fileName == null || parseEntries == null) {
			throw new IllegalArgumentException(
					"fileName and/or parseEntries might not be null");
		}
		this.fileName = fileName;
		this.parseEntries = parseEntries;
	}

	public void reload() {
		try {
			File f = new File(fileName);
			if (!f.exists()) {
				createTree();
				f = new File(fileName);
			}
			ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream(f));
			classInfos = (ClassInfo[]) ois.readObject();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected void createTree() {
		Process javadocProcess;
		if (parseEntries == null || fileName == null) {
			System.out.println("wrong config");
			System.exit(-1);
		}

		for (ParseEntry parseEntry : parseEntries) {
			System.out.println("******* " + parseEntry.getPath() + " *******");
			try {
				javadocProcess = Runtime.getRuntime().exec(
						"javadoc -sourcepath " + parseEntry.getPath()
								+ " -J-Xmx1024m -J-DbaseUrl="
								+ parseEntry.getBaseUrl() + " -J-DdataFile="
								+ fileName + " -subpackages "
								+ parseEntry.getPackages() + " -doclet "
								+ ApiParser.class.getCanonicalName()
								+ " -docletpath " + parserPath);
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
	}

	public ClassInfo[] getClassInfos() {
		if (classInfos == null) {
			reload();
		}
		return classInfos;
	}

	private static void write(String fileName, List<ClassInfo> list) {
		try {
			File f = new File(fileName);
			// useless f.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(f));
			oos.writeObject(list.toArray(new ClassInfo[list.size()]));
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The entry point for javadoc. This method actually creates all
	 * 
	 * @param root
	 *            the root node javadoc gives
	 * @return true
	 */
	public static boolean start(RootDoc root) {
		List<ClassInfo> classes = new ArrayList<ClassInfo>();

		String baseUrl = System.getProperty("baseUrl");
		String fileName = System.getProperty("dataFile");

		for (ClassDoc classDoc : root.classes()) {
			ClassInfo ci = new ClassInfo(classDoc.name(), classDoc
					.qualifiedName(), baseUrl);

			do {
				ClassInfo superCi = new ClassInfo(classDoc.name(), classDoc
						.qualifiedName(), baseUrl);

				for (MethodDoc md : classDoc.methods(true)) {
					MethodInfo mi = new MethodInfo(ci, md.name(), md
							.signature(), baseUrl, superCi);
					ci.addMethod(mi);
				}

				for (FieldDoc fd : classDoc.fields(true)) {
					FieldInfo fi = new FieldInfo(fd.name(), fd.type()
							.qualifiedTypeName());
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

		write(fileName, classes);
		return true;
	}

	public static class ParseEntry {
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

	private static class ReaderThread extends Thread {
		private BufferedReader br;

		public ReaderThread(InputStream is) {
			this.br = new BufferedReader(new InputStreamReader(is));
		}

		public void run() {
			String line;
			try {
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public List<ParseEntry> getParseEntries() {
		return parseEntries;
	}

	public void setParseEntries(List<ParseEntry> parseEntries) {
		this.parseEntries = parseEntries;
	}

	public void removeParseEntry(ParseEntry pe) {
		parseEntries.remove(pe);
	}

	public void addParseEntry(ParseEntry parseEntry) {
		parseEntries.add(parseEntry);
	}
}
