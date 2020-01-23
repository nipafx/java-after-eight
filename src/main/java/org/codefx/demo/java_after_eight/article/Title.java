package org.codefx.demo.java_after_eight.article;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Title {

	private final String text;

	private Title(String text) {
		this.text = requireNonNull(text);
	}

	public static Title from(String text) {
		return new Title(text.replaceAll("^\"|\"$", ""));
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
