package org.codefx.java_after_eight.post;

import org.codefx.java_after_eight.Utils;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Title {

	private final String text;

	public Title(String text) {
		requireNonNull(text);
		var unquotedText = Utils.removeOuterQuotationMarks(text);
		if (unquotedText.isEmpty())
			throw new IllegalArgumentException("Titles can't have an empty text.");
		this.text = unquotedText;
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
