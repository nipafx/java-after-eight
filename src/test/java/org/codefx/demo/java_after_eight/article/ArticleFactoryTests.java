package org.codefx.demo.java_after_eight.article;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleFactoryTests {

	/**
	 * Tests all variations of front matter failures
	 */
	@Nested
	class FromFrontMatter {

		/*
		 * TODO: tests for...
		 *  - lines without colon
		 *  - lines with 3+ colon
		 *  - lines with empty key
		 *  - lines with empty value
		 *  - missing lines
		 *  - superfluous lines
		 */

		@Test
		void createFromFrontMatter_allTagsCorrect_getValidArticle() {
			// REFACTOR 9: collection factories
			List<String> frontMatter = Arrays.asList(
					"title: A cool blog post",
					"tags: [$TAG, $TOG]",
					"date: 2020-01-23",
					"description: \"Very blog, much post, so wow\"",
					"slug: cool-blog-post"
			);

			Article article = ArticleFactory.createArticle(frontMatter);

			assertThat(article.title().text()).isEqualTo("A cool blog post");
			assertThat(article.tags()).extracting(Tag::text).containsExactlyInAnyOrder("$TAG", "$TOG");
			assertThat(article.date()).isEqualTo(LocalDate.of(2020, 1, 23));
			assertThat(article.description().text()).isEqualTo("Very blog, much post, so wow");
			assertThat(article.slug().value()).isEqualTo("cool-blog-post");
		}

	}

	/**
	 * Relies on {@link FromFrontMatter} testing failure cases and focuses on front matter extraction logic.
	 */
	@Nested
	class FromFile {

		@Test
		void createFromFile_allTagsCorrect_getValidArticle() {
			// REFACTOR 13: text blocks
			String file = ""
					+ "---\n"
					+ "title: A cool blog post\n"
					+ "tags: [$TAG, $TOG]\n"
					+ "date: 2020-01-23\n"
					+ "description: \"Very blog, much post, so wow\"\n"
					+ "slug: cool-blog-post\n"
					+ "---\n"
					+ "\n"
					+ "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n"
					+ "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\n"
					+ "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.\n"
					+ "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

			Article article = ArticleFactory.createArticle(file);

			assertThat(article.title().text()).isEqualTo("A cool blog post");
			assertThat(article.tags()).extracting(Tag::text).containsExactlyInAnyOrder("$TAG", "$TOG");
			assertThat(article.date()).isEqualTo(LocalDate.of(2020, 1, 23));
			assertThat(article.description().text()).isEqualTo("Very blog, much post, so wow");
			assertThat(article.slug().value()).isEqualTo("cool-blog-post");
		}

	}

}
