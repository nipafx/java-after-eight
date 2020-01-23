package org.codefx.demo.java_after_eight.article;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Article {

	private final Title title;
	private final List<Tag> tags;
	private final ZonedDateTime date;
	private final Slug slug;

	public Article(Title title, Slug slug, ZonedDateTime date, List<Tag> tags) {
		this.title = requireNonNull(title);
		this.date = requireNonNull(date);
		this.slug = requireNonNull(slug);
		this.tags = requireNonNull(tags);
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

	public Slug slug() {
		return slug;
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

	@Override
	public String toString() {
		return "Article{" +
				"title=" + title +
				", tags=" + tags +
				", date=" + date +
				", slug=" + slug +
				'}';
	}

}
