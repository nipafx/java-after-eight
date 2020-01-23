package org.codefx.demo.java_after_eight.article;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Description {

	private final String text;

	private Description(String text) {
		this.text = requireNonNull(text);
	}

	public static Description from(String text) {
		return new Description(Utils.removeOuterQuotationMarks(text));
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
