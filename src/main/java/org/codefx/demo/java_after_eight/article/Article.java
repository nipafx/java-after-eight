package org.codefx.demo.java_after_eight.article;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

// REFACTOR 14: records
public class Article {

	private final Title title;
	private final List<Tag> tags;
	private final ZonedDateTime date;
	private final Description description;
	private final Slug slug;

	public Article(Title title, List<Tag> tags, ZonedDateTime date, Description description, Slug slug) {
		this.title = requireNonNull(title);
		this.tags = requireNonNull(tags);
		this.date = requireNonNull(date);
		this.description = requireNonNull(description);
		this.slug = requireNonNull(slug);
	}

	public Title title() {
		return title;
	}

	public Stream<Tag> tags() {
		return tags.stream();
	}

	public ZonedDateTime date() {
		return date;
	}

	public Description description() {
		return description;
	}

	public Slug slug() {
		return slug;
	}

	@Override
	public boolean equals(Object o) {
		// REFACTOR 14: pattern matching
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

	@Override
	public String toString() {
		return "Article{" +
				"title=" + title +
				", tags=" + tags +
				", date=" + date +
				", description=" + description +
				", slug=" + slug +
				'}';
	}

}
