package org.codefx.java_after_eight.post.video;

import org.codefx.java_after_eight.post.Description;
import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.post.Slug;
import org.codefx.java_after_eight.post.Tag;
import org.codefx.java_after_eight.post.Title;

import java.net.URI;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Video implements Post {

	private final Title title;
	private final Set<Tag> tags;
	private final LocalDate date;
	private final Description description;
	private final Slug slug;
	private final URI uri;

	public Video(Title title, Set<Tag> tags, LocalDate date, Description description, Slug slug, URI uri) {
		this.title = requireNonNull(title);
		this.tags = requireNonNull(tags);
		this.date = requireNonNull(date);
		this.description = requireNonNull(description);
		this.slug = requireNonNull(slug);
		this.uri = requireNonNull(uri);
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

}
