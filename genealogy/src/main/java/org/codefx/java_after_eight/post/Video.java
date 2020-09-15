package org.codefx.java_after_eight.post;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public record Video(
		Title title,
		Set<Tag> tags,
		LocalDate date,
		Description description,
		Slug slug,
		VideoSlug video,
		Optional<Repository> repository) implements Post {

	public Video {
		requireNonNull(title);
		requireNonNull(tags);
		requireNonNull(date);
		requireNonNull(description);
		requireNonNull(slug);
		requireNonNull(video);
		requireNonNull(repository);
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
		Video video = (Video) o;
		return slug.equals(video.slug);
	}

	@Override
	public int hashCode() {
		return Objects.hash(slug);
	}

}
