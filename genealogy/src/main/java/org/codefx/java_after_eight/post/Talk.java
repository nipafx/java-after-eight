package org.codefx.java_after_eight.post;

import java.net.URI;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public record Talk(
		Title title,
		Set<Tag> tags,
		LocalDate date,
		Description description,
		Slug slug,
		URI slides,
		Optional<VideoSlug> video) implements Post {

	public Talk {
		requireNonNull(title);
		requireNonNull(tags);
		requireNonNull(date);
		requireNonNull(description);
		requireNonNull(slug);
		requireNonNull(slides);
		requireNonNull(video);
	}

	@Override
	public Set<Tag> tags() {
		return Set.copyOf(tags);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Talk talk = (Talk) o;
		return slug.equals(talk.slug);
	}

	@Override
	public int hashCode() {
		return Objects.hash(slug);
	}

}
