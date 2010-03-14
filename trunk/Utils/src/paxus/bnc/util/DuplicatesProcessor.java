package paxus.bnc.util;

import java.io.*;
import java.util.Arrays;

public class DuplicatesProcessor {

	private static final String DICT_SRC = "/res/dict/src";
	private static final String DICT_DIST = "/res/dict/dist";
	static File fs;
	static File fd;

	public static void main(String[] args) throws Exception {
		init(args);
		
		BufferedReader fr = new BufferedReader(new FileReader(fs));
		BufferedWriter fw = new BufferedWriter(new FileWriter(fd));
		try {
			fw.write("");
			String line;
			while ((line = fr.readLine()) != null) {
				if (noDuplicateSymbols(line))
					fw.append(line + "\r\n");	//+ 2 bytes on each line
			}
			System.out.println("Work is done with no errors");
		} finally {
			if (fr != null) try {fr.close();} catch(Exception e) {};
			if (fw != null) try {fw.close();} catch(Exception e) {};
		}
	}

	public static void init(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("Processed dictionary file. \n" +
					"Usage: \n" +
					"\tsource_filename [dest_filename]\n" +
					"\nNote: files should be located at " + DICT_SRC);
			System.exit(1);
		}
		
		System.out.println("args: " + Arrays.toString(args));
		String source = args[0];
		String distr = args.length > 1 ? args[1] : source;
		String path = new File(DuplicatesProcessor.class.getClassLoader().getResource("anchor").toURI())
				.getParentFile().getParentFile().getAbsolutePath();
		System.out.println("\nPath = " + path);
		
		fs = new File(path + DICT_SRC, source);
		if (!fs.exists()) {
			System.out.println("Source file does not exist: " + fs.getAbsolutePath());
			System.exit(1);
		}
		System.out.println("Source file \"" + fs.getName() + "\" found");
		fd = new File(path + DICT_DIST, distr);
		System.out.println("Dest file \"" + fd.getAbsolutePath() + "\" creating");
	}
	
	public static boolean noDuplicateSymbols(String line) {
		for (int i = 0; i < line.length() - 1; i++)
			if (line.lastIndexOf(line.charAt(i)) > i)
				return false;
		return true;
	}
}
