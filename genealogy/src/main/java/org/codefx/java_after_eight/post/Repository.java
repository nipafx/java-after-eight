package org.codefx.java_after_eight.post;

import static java.util.Objects.requireNonNull;

public record Repository(String identifier) {

	public Repository {
		identifier = requireNonNull(identifier);
		if (identifier.isBlank())
			throw new IllegalArgumentException("Repositories can't have an empty identifier.");
	}

}
