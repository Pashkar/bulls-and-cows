package paxus.bnc.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class OzhegovProcessor2 {
	public static void main(String[] args) throws Exception {
		DuplicatesProcessor.init(args);
		int wordLength = Integer.parseInt(args[2]);
		
		BufferedReader fr = new BufferedReader(new FileReader(DuplicatesProcessor.fs));
		BufferedWriter fw = new BufferedWriter(new FileWriter(DuplicatesProcessor.fd));
		try {
			fw.write("");
			System.out.println("====================");
			
			String line;
			int count = 0;
			while ((line = fr.readLine()) != null) {
				if ((line = process(line, wordLength)) != null) {
					fw.append(line + "\r\n");	//+ 2 bytes on each line
					count++;
				}
			}
			System.out.println("Work is done with no errors, lines stored: " + count);
		} finally {
			if (fr != null) try {fr.close();} catch(Exception e) {};
			if (fw != null) try {fw.close();} catch(Exception e) {};
		}
	}

	private static String process(String line, int wordLength) {
		if (line.length() != wordLength)
			return null;
		System.out.println(line);
		return line;
	}
}
