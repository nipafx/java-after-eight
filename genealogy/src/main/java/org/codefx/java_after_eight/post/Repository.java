package org.codefx.java_after_eight.post;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Repository {

	private final String identifier;

	public Repository(String identifier) {
		this.identifier = requireNonNull(identifier);
		if (identifier.isEmpty())
			throw new IllegalArgumentException("Repositories can't have an empty identifier.");
	}

	public String identifier() {
		return identifier;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Repository that = (Repository) o;
		return identifier.equals(that.identifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier);
	}

	@Override
	public String toString() {
		return "Repository{" +
				"identifier='" + identifier + '\'' +
				'}';
	}

}
