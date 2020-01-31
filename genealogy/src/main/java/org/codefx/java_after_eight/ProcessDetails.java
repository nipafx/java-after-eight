package org.codefx.java_after_eight;

import java.util.Optional;

import static java.lang.String.format;

public class ProcessDetails {

	public static String details() {
		return format(
				"Process ID: %s | Major Java version: %s",
				ProcessHandle.current().pid(),
				getMajorJavaVersion().map(Object::toString).orElse("unknown"));
	}

	public static Optional<Integer> getMajorJavaVersion() {
		try {
			String version = System.getProperty("java.version");
			if (version.startsWith("1."))
				return Optional.of(Integer.parseInt(version.substring(2, 3)));

			if (version.contains("."))
				return Optional.of(Integer.parseInt(version.split("\\.")[0]));

			// hail mary
			return Optional.of(Integer.parseInt(version));
		} catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

}
