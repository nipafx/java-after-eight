package org.codefx.java_after_eight;

public class ProcessDetails {

	public static String details() {
		return "Process ID: %s | Major Java version: %s".formatted(
				ProcessHandle.current().pid(),
				Runtime.version().major());
	}

}
