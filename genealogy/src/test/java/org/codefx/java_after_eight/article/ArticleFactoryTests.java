package org.codefx.java_after_eight.article;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
		 *  - lines with empty key
		 *  - lines with empty value
		 *  - missing lines
		 *  - superfluous lines
		 */

		@Test
		void createFromFrontMatter_multipleColons_getValidArticle() {
			// REFACTOR 9: collection factories
			List<String> frontMatter = Arrays.asList(
					"title: Cool: A blog post",
					"tags: [$TAG, $TOG]",
					"date: 2020-01-23",
					"description: \"Very blog, much post, so wow\"",
					"slug: cool-blog-post"
			);

			Article article = ArticleFactory.createArticle(frontMatter, Stream::empty);

			assertThat(article.title().text()).isEqualTo("Cool: A blog post");
			assertThat(article.tags()).extracting(Tag::text).containsExactlyInAnyOrder("$TAG", "$TOG");
			assertThat(article.date()).isEqualTo(LocalDate.of(2020, 1, 23));
			assertThat(article.description().text()).isEqualTo("Very blog, much post, so wow");
			assertThat(article.slug().value()).isEqualTo("cool-blog-post");
		}

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

			Article article = ArticleFactory.createArticle(frontMatter, Stream::empty);

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
		void createFromFile_allTagsCorrect_getValidArticle() throws IOException {
			List<String> file = Arrays.asList(
					"---",
					"title: A cool blog post",
					"tags: [$TAG, $TOG]",
					"date: 2020-01-23",
					"description: \"Very blog, much post, so wow\"",
					"slug: cool-blog-post",
					"---",
					"",
					"Lorem ipsum dolor sit amet.",
					"Ut enim ad minim veniam.",
					"Duis aute irure dolor in reprehenderit.",
					"Excepteur sint occaecat cupidatat non proident.");

			Article article = ArticleFactory.createArticle(file);

			assertThat(article.title().text()).isEqualTo("A cool blog post");
			assertThat(article.tags()).extracting(Tag::text).containsExactlyInAnyOrder("$TAG", "$TOG");
			assertThat(article.date()).isEqualTo(LocalDate.of(2020, 1, 23));
			assertThat(article.description().text()).isEqualTo("Very blog, much post, so wow");
			assertThat(article.slug().value()).isEqualTo("cool-blog-post");
			assertThat(article.content().get()).containsExactly(
					"",
					"Lorem ipsum dolor sit amet.",
					"Ut enim ad minim veniam.",
					"Duis aute irure dolor in reprehenderit.",
					"Excepteur sint occaecat cupidatat non proident.");
		}

	}

}
