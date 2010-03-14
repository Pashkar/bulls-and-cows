package paxus.bnc.util;

import java.io.*;
import java.util.Arrays;

public class DictProcessor {

	private static final String DICT_SRC = "/res/dict/src";
	private static final String DICT_DIST = "/res/dict/dist";

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("Processed dictionary file. \n" +
					"Usage: \n" +
					"\tjava.exe DictProcessor source_filename [result_filename]\n" +
					"\nNote: files should be located at " + DICT_SRC);
			System.exit(1);
		}
		System.out.println("args: " + Arrays.toString(args));
		String source = args[0];
		String distr = args.length > 1 ? args[1] : source;
		String path = new File(DictProcessor.class.getClassLoader().getResource("anchor").toURI())
				.getParentFile().getParentFile().getAbsolutePath();
		System.out.println("\nPath = " + path);
		
		File fs = new File(path + DICT_SRC, source);
		if (!fs.exists()) {
			System.out.println("Source file does not exist: " + fs.getAbsolutePath());
			System.exit(1);
		}
		System.out.println("Source file \"" + fs.getName() + "\" found");
		File fd = new File(path + DICT_DIST, distr);
		System.out.println("Dest file \"" + fd.getAbsolutePath() + "\" creating");
		
		BufferedReader fr = new BufferedReader(new FileReader(fs));
		BufferedWriter fw = new BufferedWriter(new FileWriter(fd));
		try {
			fw.write("");
			String line;
			while ((line = fr.readLine()) != null) {
				if (filtereDuplicateSymbols(line))
					fw.append(line + "\r\n");	//+ 2 bytes on each line
			}
			System.out.println("Work is done with no errors");
		} finally {
			fr.close();
			fw.close();
		}
	}

	private static boolean filtereDuplicateSymbols(String line) {
		for (int i = 0; i < line.length() - 1; i++)
			if (line.lastIndexOf(line.charAt(i)) > i)
				return false;
		return true;
	}
}
