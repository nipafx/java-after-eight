package org.codefx.java_after_eight.article;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Article {

	private final Title title;
	private final Set<Tag> tags;
	private final LocalDate date;
	private final Description description;
	private final Slug slug;
	private final Content content;

	Article(Title title, Set<Tag> tags, LocalDate date, Description description, Slug slug, Content content) {
		this.title = requireNonNull(title);
		this.tags = requireNonNull(tags);
		this.date = requireNonNull(date);
		this.description = requireNonNull(description);
		this.slug = requireNonNull(slug);
		this.content = requireNonNull(content);
	}

	public Title title() {
		return title;
	}

	public Stream<Tag> tags() {
		return tags.stream();
	}

	public LocalDate date() {
		return date;
	}

	public Description description() {
		return description;
	}

	public Slug slug() {
		return slug;
	}

	public Content content() {
		return content;
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
				", description=" + description +
				", slug=" + slug +
				'}';
	}

}
