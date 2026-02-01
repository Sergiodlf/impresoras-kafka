package com.sergiodlf.impresoraskafka.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
	public static void ensureDir(String dir) {
		try {
			Files.createDirectories(Path.of(dir));
		} catch (Exception e) {
			throw new RuntimeException("Cannot create dir " + dir + ": " + e.getMessage(), e);
		}
	}

	public static void writeTextFile(String path, String content) {
		try {
			Path p = Path.of(path);
			Files.createDirectories(p.getParent());
			Files.writeString(p, content, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException("Cannot write file " + path + ": " + e.getMessage(), e);
		}
	}
}