package org.codefx.java_after_eight;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class Utils {

	private Utils() {
		// private constructor to prevent accidental instantiation of utility class
	}

	public static String removeOuterQuotationMarks(String string) {
		return string.replaceAll("^\"|\"$", "");
	}

	public static Stream<Path> uncheckedFilesList(Path dir) {
		try {
			return Files.list(dir);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	public static <T> void uncheckedFilesWrite(Path path, String content) {
		try {
			Files.write(path, Arrays.asList(content));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	public static List<String> uncheckedFilesReadAllLines(Path file) {
		try {
			return Files.readAllLines(file);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

}
