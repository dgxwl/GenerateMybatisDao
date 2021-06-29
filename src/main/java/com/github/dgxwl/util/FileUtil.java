package com.github.dgxwl.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileUtil {

	private FileUtil() {}
	
	public static boolean mkdirIfNotExists(String path) {
		File f = new File(path);
		if (!f.exists()) {
			return f.mkdirs();
		}
		return true;
	}
	
	public static String writeTextToFile(String filename, String content) {
		try {
			File entity = new File(filename);
			entity.createNewFile();
		} catch (IOException e) {
			return "create file error: " + e.getMessage();
		}
		try (
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
		) {
			bw.write(content);
			bw.flush();
			return "";
		} catch (Exception e) {
			return "write file error: " + e.getMessage();
		}
	}
}
