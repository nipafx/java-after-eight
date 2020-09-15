package org.codefx.java_after_eight.post.factories;

import java.util.Map;
import java.util.Optional;

class RawFrontMatter {

	private final Map<String, String> lines;

	RawFrontMatter(Map<String, String> lines) {
		this.lines = lines;
	}

	public Optional<String> valueOf(String key) {
		return Optional.ofNullable(lines.get(key));
	}

	public String requiredValueOf(String key) {
		return valueOf(key).orElseThrow(
				() -> new IllegalArgumentException("Required key '" + key + "' not present in front matter."));
	}

}
