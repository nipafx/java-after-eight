package org.codefx.java_after_eight.post.factories;

import org.codefx.java_after_eight.post.Article;
import org.codefx.java_after_eight.post.Description;
import org.codefx.java_after_eight.post.Repository;
import org.codefx.java_after_eight.post.Slug;
import org.codefx.java_after_eight.post.Tag;
import org.codefx.java_after_eight.post.Title;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.codefx.java_after_eight.post.factories.PostFactory.DATE;
import static org.codefx.java_after_eight.post.factories.PostFactory.DESCRIPTION;
import static org.codefx.java_after_eight.post.factories.PostFactory.REPOSITORY;
import static org.codefx.java_after_eight.post.factories.PostFactory.SLUG;
import static org.codefx.java_after_eight.post.factories.PostFactory.TAGS;
import static org.codefx.java_after_eight.post.factories.PostFactory.TITLE;

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
				new Title(frontMatter.requiredValueOf(TITLE)),
				Tag.from(frontMatter.requiredValueOf(TAGS)),
				LocalDate.parse(frontMatter.requiredValueOf(DATE)),
				new Description(frontMatter.requiredValueOf(DESCRIPTION)),
				new Slug(frontMatter.requiredValueOf(SLUG)),
				frontMatter.valueOf(REPOSITORY).map(Repository::new),
				post.content());
	}

}
