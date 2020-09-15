package org.codefx.java_after_eight.post.factories;

import org.codefx.java_after_eight.post.Description;
import org.codefx.java_after_eight.post.Slug;
import org.codefx.java_after_eight.post.Tag;
import org.codefx.java_after_eight.post.Talk;
import org.codefx.java_after_eight.post.Title;
import org.codefx.java_after_eight.post.VideoSlug;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.codefx.java_after_eight.post.factories.PostFactory.DATE;
import static org.codefx.java_after_eight.post.factories.PostFactory.DESCRIPTION;
import static org.codefx.java_after_eight.post.factories.PostFactory.SLIDES;
import static org.codefx.java_after_eight.post.factories.PostFactory.SLUG;
import static org.codefx.java_after_eight.post.factories.PostFactory.TAGS;
import static org.codefx.java_after_eight.post.factories.PostFactory.TITLE;
import static org.codefx.java_after_eight.post.factories.PostFactory.VIDEO;

public final class TalkFactory {

	private TalkFactory() {
		// private constructor to prevent accidental instantiation of utility class
	}

	public static Talk createTalk(Path file) {
		try {
			RawPost post = PostFactory.readPost(file);
			return createTalk(post);
		} catch (RuntimeException ex) {
			throw new RuntimeException("Creating talk failed: " + file, ex);
		}
	}

	private static Talk createTalk(RawPost post) {
		RawFrontMatter frontMatter = post.frontMatter();
		try {
			return new Talk(
					Title.from(frontMatter.requiredValueOf(TITLE)),
					Tag.from(frontMatter.requiredValueOf(TAGS)),
					LocalDate.parse(frontMatter.requiredValueOf(DATE)),
					Description.from(frontMatter.requiredValueOf(DESCRIPTION)),
					Slug.from(frontMatter.requiredValueOf(SLUG)),
					new URI(frontMatter.requiredValueOf(SLIDES)),
					frontMatter.valueOf(VIDEO).map(VideoSlug::from));
		} catch (URISyntaxException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

}
