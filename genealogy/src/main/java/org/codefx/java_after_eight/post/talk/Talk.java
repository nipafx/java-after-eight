package org.codefx.java_after_eight.post.talk;

import org.codefx.java_after_eight.post.Description;
import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.post.Slug;
import org.codefx.java_after_eight.post.Tag;
import org.codefx.java_after_eight.post.Title;
import org.codefx.java_after_eight.post.VideoSlug;

import java.net.URI;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Talk implements Post {

	private final Title title;
	private final Set<Tag> tags;
	private final LocalDate date;
	private final Description description;
	private final Slug slug;

	private final URI slides;
	private final Optional<VideoSlug> video;

	public Talk(Title title, Set<Tag> tags, LocalDate date, Description description, Slug slug, URI slides, Optional<VideoSlug> video) {
		this.title = requireNonNull(title);
		this.tags = requireNonNull(tags);
		this.date = requireNonNull(date);
		this.description = requireNonNull(description);
		this.slug = requireNonNull(slug);
		this.slides = requireNonNull(slides);
		this.video = requireNonNull(video);
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

	public URI slides() {
		return slides;
	}

	public Optional<VideoSlug> video() {
		return video;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Talk video = (Talk) o;
		return slug.equals(video.slug);
	}

	@Override
	public int hashCode() {
		return Objects.hash(slug);
	}

	@Override
	public String toString() {
		return "Video{" +
				"title=" + title +
				", tags=" + tags +
				", date=" + date +
				", description=" + description +
				", slug=" + slug +
				", slides=" + slides +
				", video=" + video +
				'}';
	}

}
