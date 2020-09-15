package org.codefx.java_after_eight.post;

import static java.util.Objects.requireNonNull;

public record Slug(String value) implements Comparable<Slug> {

	public Slug {
		value = requireNonNull(value);
		if (value.isBlank())
			throw new IllegalArgumentException("Slugs can't have an empty value.");
	}

	@Override
	public int compareTo(Slug right) {
		return this.value.compareTo(right.value);
	}

}
