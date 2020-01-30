package org.codefx.java_after_eight.article;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

// REFACTOR 14: records
public class Title {

	private final String text;

	private Title(String text) {
		this.text = requireNonNull(text);
		if (text.isEmpty())
			throw new IllegalArgumentException("Titles can't have an empty text.");
	}

	static Title from(String text) {
		return new Title(Utils.removeOuterQuotationMarks(text));
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
		Title title = (Title) o;
		return text.equals(title.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text);
	}

	@Override
	public String toString() {
		return "Title{" +
				"value='" + text + '\'' +
				'}';
	}

}
