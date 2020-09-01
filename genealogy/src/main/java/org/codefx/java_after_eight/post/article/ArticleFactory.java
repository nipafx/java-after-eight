package org.codefx.java_after_eight.post.article;

import org.codefx.java_after_eight.post.Description;
import org.codefx.java_after_eight.post.PostFactory;
import org.codefx.java_after_eight.post.PostFactory.RawFrontMatter;
import org.codefx.java_after_eight.post.PostFactory.RawPost;
import org.codefx.java_after_eight.post.Slug;
import org.codefx.java_after_eight.post.Tag;
import org.codefx.java_after_eight.post.Title;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.codefx.java_after_eight.post.PostFactory.DATE;
import static org.codefx.java_after_eight.post.PostFactory.DESCRIPTION;
import static org.codefx.java_after_eight.post.PostFactory.SLUG;
import static org.codefx.java_after_eight.post.PostFactory.TAGS;
import static org.codefx.java_after_eight.post.PostFactory.TITLE;

public final class ArticleFactory {

	private ArticleFactory() {
		// private constructor to prevent accidental instantiation of utility class
	}

	public static Article createArticle(Path file) {
		try {
			RawPost post = PostFactory.readPost(file);
			return createArticle(post);
		} catch (RuntimeException ex) {
			throw new RuntimeException("Creating article failed: " + file, ex);
		}
	}

	public static Article createArticle(List<String> fileLines) {
		RawPost post = PostFactory.readPost(fileLines);
		return createArticle(post);
	}

	private static Article createArticle(RawPost post) {
		RawFrontMatter frontMatter = post.frontMatter();
		return new Article(
				Title.from(frontMatter.requiredValueOf(TITLE)),
				Tag.from(frontMatter.requiredValueOf(TAGS)),
				LocalDate.parse(frontMatter.requiredValueOf(DATE)),
				Description.from(frontMatter.requiredValueOf(DESCRIPTION)),
				Slug.from(frontMatter.requiredValueOf(SLUG)),
				post.content());
	}

}
