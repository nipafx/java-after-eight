package org.codefx.java_after_eight.post;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public record Article(
		Title title,
		Set<Tag> tags,
		LocalDate date,
		Description description,
		Slug slug,
		Optional<Repository> repository,
		Content content) implements Post {

	public Article {
		requireNonNull(title);
		requireNonNull(tags);
		requireNonNull(date);
		requireNonNull(description);
		requireNonNull(slug);
		requireNonNull(repository);
		requireNonNull(content);
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
		Article article = (Article) o;
		return slug.equals(article.slug);
	}

	@Override
	public int hashCode() {
		return Objects.hash(slug);
	}

}
