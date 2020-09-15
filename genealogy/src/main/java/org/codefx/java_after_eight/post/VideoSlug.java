package org.codefx.java_after_eight.post;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class VideoSlug implements Comparable<VideoSlug> {

	private final String value;

	public VideoSlug(String value) {
		this.value = requireNonNull(value);
		if (value.isBlank())
			throw new IllegalArgumentException("Slugs can't have an empty value.");
	}

	public String value() {
		return value;
	}

	@Override
	public int compareTo(VideoSlug right) {
		return this.value.compareTo(right.value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		VideoSlug slug = (VideoSlug) o;
		return value.equals(slug.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return "VideoSlug{" +
				"value='" + value + '\'' +
				'}';
	}

}
