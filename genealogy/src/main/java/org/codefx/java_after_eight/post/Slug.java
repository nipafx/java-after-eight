package org.codefx.java_after_eight.post;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Slug implements Comparable<Slug> {

	private final String value;

	public Slug(String value) {
		this.value = requireNonNull(value);
		if (value.isBlank())
			throw new IllegalArgumentException("Slugs can't have an empty value.");
	}

	public String value() {
		return value;
	}

	@Override
	public int compareTo(Slug right) {
		return this.value.compareTo(right.value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Slug slug = (Slug) o;
		return value.equals(slug.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return "Slug{" +
				"value='" + value + '\'' +
				'}';
	}

}
