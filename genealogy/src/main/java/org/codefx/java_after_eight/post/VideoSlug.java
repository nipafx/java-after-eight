package org.codefx.java_after_eight.post;

import static java.util.Objects.requireNonNull;

public record VideoSlug(String value) implements Comparable<VideoSlug> {

	public VideoSlug {
		requireNonNull(value);
		if (value.isBlank())
			throw new IllegalArgumentException("Slugs can't have an empty value.");
	}

	@Override
	public int compareTo(VideoSlug right) {
		return this.value.compareTo(right.value);
	}

}
