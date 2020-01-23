package org.codefx.demo.java_after_eight.article;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class Tag {

	private final String text;

	private Tag(String text) {
		this.text = requireNonNull(text);
		if (text.isEmpty())
			throw new IllegalArgumentException("Tags can't have an empty text.");
	}

	public static List<Tag> from(String tagsText) {
		Stream<String> tags = Stream.of(tagsText.replaceAll("^\\[|\\]$", "")
				.split(","));
		return tags
				// REFACTOR 11: String::strip
				.map(String::trim)
				.map(Tag::new)
				// REFACTOR 9: Collectors::toImmutableList
				.collect(toList());
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
		Tag tag = (Tag) o;
		return text.equals(tag.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text);
	}

	@Override
	public String toString() {
		return "Tag{" +
				"text='" + text + '\'' +
				'}';
	}

}
