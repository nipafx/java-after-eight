package org.codefx.java_after_eight.post;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class Video implements Post {

	private final Title title;
	private final Set<Tag> tags;
	private final LocalDate date;
	private final Description description;
	private final Slug slug;

	private final VideoSlug video;
	private final Optional<Repository> repository;

	public Video(Title title, Set<Tag> tags, LocalDate date, Description description, Slug slug, VideoSlug video, Optional<Repository> repository) {
		this.title = requireNonNull(title);
		this.tags = requireNonNull(tags);
		this.date = requireNonNull(date);
		this.description = requireNonNull(description);
		this.slug = requireNonNull(slug);
		this.video = requireNonNull(video);
		this.repository = requireNonNull(repository);
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

	public VideoSlug video() {
		return video;
	}

	public Optional<Repository> repository() {
		return repository;
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

	@Override
	public String toString() {
		return "Video{" +
				"title=" + title +
				", tags=" + tags +
				", date=" + date +
				", description=" + description +
				", slug=" + slug +
				", url=" + video +
				", repository=" + repository +
				'}';
	}

}
