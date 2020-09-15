package org.codefx.java_after_eight.post.factories;

import org.codefx.java_after_eight.post.Description;
import org.codefx.java_after_eight.post.Repository;
import org.codefx.java_after_eight.post.Slug;
import org.codefx.java_after_eight.post.Tag;
import org.codefx.java_after_eight.post.Title;
import org.codefx.java_after_eight.post.Video;
import org.codefx.java_after_eight.post.VideoSlug;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.codefx.java_after_eight.post.factories.PostFactory.DATE;
import static org.codefx.java_after_eight.post.factories.PostFactory.DESCRIPTION;
import static org.codefx.java_after_eight.post.factories.PostFactory.REPOSITORY;
import static org.codefx.java_after_eight.post.factories.PostFactory.SLUG;
import static org.codefx.java_after_eight.post.factories.PostFactory.TAGS;
import static org.codefx.java_after_eight.post.factories.PostFactory.TITLE;
import static org.codefx.java_after_eight.post.factories.PostFactory.VIDEO;

public final class VideoFactory {

	private VideoFactory() {
		// private constructor to prevent accidental instantiation of utility class
	}

	public static Video createVideo(Path file) {
		try {
			RawPost post = PostFactory.readPost(file);
			return createVideo(post);
		} catch (RuntimeException ex) {
			throw new RuntimeException("Creating video failed: " + file, ex);
		}
	}

	private static Video createVideo(RawPost post) {
		RawFrontMatter frontMatter = post.frontMatter();
		return new Video(
				Title.from(frontMatter.requiredValueOf(TITLE)),
				Tag.from(frontMatter.requiredValueOf(TAGS)),
				LocalDate.parse(frontMatter.requiredValueOf(DATE)),
				Description.from(frontMatter.requiredValueOf(DESCRIPTION)),
				Slug.from(frontMatter.requiredValueOf(SLUG)),
				VideoSlug.from(frontMatter.requiredValueOf(VIDEO)),
				frontMatter.valueOf(REPOSITORY).map(Repository::from));
	}

}
