package org.codefx.java_after_eight.post.article;

import org.codefx.java_after_eight.post.Content;
import org.codefx.java_after_eight.post.Description;
import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.post.Repository;
import org.codefx.java_after_eight.post.Slug;
import org.codefx.java_after_eight.post.Tag;
import org.codefx.java_after_eight.post.Title;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Article implements Post {

	private final Title title;
	private final Set<Tag> tags;
	private final LocalDate date;
	private final Description description;
	private final Slug slug;

	private final Optional<Repository> repository;
	private final Content content;

	public Article(Title title, Set<Tag> tags, LocalDate date, Description description, Slug slug, Optional<Repository> repository, Content content) {
		this.title = requireNonNull(title);
		this.tags = requireNonNull(tags);
		this.date = requireNonNull(date);
		this.description = requireNonNull(description);
		this.slug = requireNonNull(slug);
		this.repository = requireNonNull(repository);
		this.content = requireNonNull(content);
	}

	@Override
	public Title title() {
		return title;
	}

	@Override
	public Stream<Tag> tags() {
		return tags.stream();
	}

	@Override
	public LocalDate date() {
		return date;
	}

	@Override
	public Description description() {
		return description;
	}

	@Override
	public Slug slug() {
		return slug;
	}

	public Optional<Repository> repository() {
		return repository;
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
				", repository=" + repository +
				'}';
	}

}
