package paxus.bnc.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;

public class DictProcessor {

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Processed dictionary file. \n" +
					"Usage: \n" +
					"\tjava.exe DictProcessor source_filename result_filename\n" +
					"\nNote: files should be included into classpath, name based on package distribution\n" +
					"(\"dict/src/name.txt\" for file \"res/dict/src/name.txt\")");
			System.exit(1);
		}
		System.out.println("args: " + Arrays.toString(args));
		String source = args[0];
		String distr = args[1];
		
		File fs = new File(DictProcessor.class.getClassLoader().getResource(source).toURI());
		if (!fs.exists()) {
			System.out.println("Source file does not exist: " + fs.getAbsolutePath());
			System.exit(1);
		}
		
		FileReader fsr = new FileReader(fs);
		try {
			while (fsr.re)
		} finally {
			fsr.close();
		}
	}

}
