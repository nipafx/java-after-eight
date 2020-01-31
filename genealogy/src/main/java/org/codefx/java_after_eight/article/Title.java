package org.codefx.java_after_eight.article;

import org.codefx.java_after_eight.Utils;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Title {

	private final String text;

	private Title(String text) {
		this.text = text;
	}

	static Title from(String text) {
		requireNonNull(text);
		String unquotedText = Utils.removeOuterQuotationMarks(text);
		if (unquotedText.isEmpty())
			throw new IllegalArgumentException("Titles can't have an empty text.");
		return new Title(unquotedText);
	}

	public String text() {
		return text;
	}

	@Override
	public boolean equals(Object o) {
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
