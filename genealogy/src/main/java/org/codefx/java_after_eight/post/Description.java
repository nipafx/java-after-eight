package org.codefx.java_after_eight.post;

import org.codefx.java_after_eight.Utils;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Description {

	private final String text;

	public Description(String text) {
		requireNonNull(text);
		String unquotedText = Utils.removeOuterQuotationMarks(text).trim();
		if (unquotedText.isEmpty())
			throw new IllegalArgumentException("Description can't have an empty text.");
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
