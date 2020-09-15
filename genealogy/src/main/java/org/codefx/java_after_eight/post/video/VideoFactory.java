package org.codefx.java_after_eight.post.video;

import org.codefx.java_after_eight.post.Description;
import org.codefx.java_after_eight.post.PostFactory;
import org.codefx.java_after_eight.post.PostFactory.RawFrontMatter;
import org.codefx.java_after_eight.post.PostFactory.RawPost;
import org.codefx.java_after_eight.post.Repository;
import org.codefx.java_after_eight.post.Slug;
import org.codefx.java_after_eight.post.Tag;
import org.codefx.java_after_eight.post.Title;
import org.codefx.java_after_eight.post.VideoSlug;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.codefx.java_after_eight.post.PostFactory.DATE;
import static org.codefx.java_after_eight.post.PostFactory.DESCRIPTION;
import static org.codefx.java_after_eight.post.PostFactory.REPOSITORY;
import static org.codefx.java_after_eight.post.PostFactory.SLUG;
import static org.codefx.java_after_eight.post.PostFactory.TAGS;
import static org.codefx.java_after_eight.post.PostFactory.TITLE;
import static org.codefx.java_after_eight.post.PostFactory.VIDEO;

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
