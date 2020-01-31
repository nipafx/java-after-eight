package org.codefx.java_after_eight;

import static java.lang.String.format;

public class ProcessDetails {

	public static String details() {
		return format(
				"Process ID: %s | Major Java version: %s",
				ProcessHandle.current().pid(),
				Runtime.version().major());
	}

}
