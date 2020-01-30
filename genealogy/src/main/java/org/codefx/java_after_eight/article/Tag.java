package org.codefx.java_after_eight.article;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class Tag {

	private final String text;

	private Tag(String text) {
		this.text = requireNonNull(text);
		if (text.isEmpty())
			throw new IllegalArgumentException("Tags can't have an empty text.");
	}

	static Set<Tag> from(String tagsText) {
		Set<Tag> tags = Stream.of(tagsText
				.replaceAll("^\\[|\\]$", "")
				.split(","))
				.map(String::trim)
				.filter(tag -> !tag.isEmpty())
				.map(Tag::new)
				.collect(toSet());
		return Collections.unmodifiableSet(tags);
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
