package org.codefx.java_after_eight.genealogist;

import static java.util.Objects.requireNonNull;

public record RelationType(String value) {

	// `RelationType` is a string (and not an enum) because {@code Genealogist} implementations
	// can be plugged in via services, which means their type is unknown at runtime.

	public RelationType {
		requireNonNull(value);
		if (value.isBlank())
			throw new IllegalArgumentException("Relation types can't have an empty value.");
	}

}
