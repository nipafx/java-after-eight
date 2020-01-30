package org.codefx.java_after_eight.article;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

// REFACTOR 14: records
public class Description {

	private final String text;

	private Description(String text) {
		this.text = requireNonNull(text);
		if (text.isEmpty())
			throw new IllegalArgumentException("Description can't have an empty text.");
	}

	static Description from(String text) {
		return new Description(Utils.removeOuterQuotationMarks(text));
	}

	public String text() {
		return text;
	}

	@Override
	public boolean equals(Object o) {
		// REFACTOR 14: pattern matching
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Description that = (Description) o;
		return text.equals(that.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text);
	}

	@Override
	public String toString() {
		return "Description{" +
				"text='" + text + '\'' +
				'}';
	}

}
