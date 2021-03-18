package org.codefx.java_after_eight.genealogist;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class RelationType {

	// `RelationType` is a string (and not an enum) because {@code Genealogist} implementations
	// can be plugged in via services, which means their type is unknown at runtime.

	private final String value;

	public RelationType(String value) {
		this.value = requireNonNull(value);
		if (value.isEmpty())
			throw new IllegalArgumentException("Relation types can't have an empty value.");
	}

	public String value() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		RelationType slug = (RelationType) o;
		return value.equals(slug.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return "RelationType{" +
				"value='" + value + '\'' +
				'}';
	}

}
