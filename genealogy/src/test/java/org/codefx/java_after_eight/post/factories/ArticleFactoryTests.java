package org.codefx.java_after_eight.post.factories;

import org.codefx.java_after_eight.post.Article;
import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.post.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleFactoryTests {

	@Test
	void createFromFrontMatter_multipleColons_getValidArticle() {
		List<String> file = Arrays.asList(
				"---",
				"title: Cool: A blog post",
				"tags: [$TAG, $TOG]",
				"date: 2020-01-23",
				"description: \"Very blog, much post, so wow\"",
				"slug: cool-blog-post",
				"---",
				""
		);

		Post post = ArticleFactory.createArticle(file);

		assertThat(post.title().text()).isEqualTo("Cool: A blog post");
		assertThat(post.tags()).extracting(Tag::text).containsExactlyInAnyOrder("$TAG", "$TOG");
		assertThat(post.date()).isEqualTo(LocalDate.of(2020, 1, 23));
		assertThat(post.description().text()).isEqualTo("Very blog, much post, so wow");
		assertThat(post.slug().value()).isEqualTo("cool-blog-post");
	}

	@Test
	void createFromFrontMatter_allTagsCorrect_getValidArticle() {
		List<String> file = Arrays.asList(
				"---",
				"title: A cool blog post",
				"tags: [$TAG, $TOG]",
				"date: 2020-01-23",
				"description: \"Very blog, much post, so wow\"",
				"slug: cool-blog-post",
				"---",
				""
		);

		Post article = ArticleFactory.createArticle(file);

		assertThat(article.title().text()).isEqualTo("A cool blog post");
		assertThat(article.tags()).extracting(Tag::text).containsExactlyInAnyOrder("$TAG", "$TOG");
		assertThat(article.date()).isEqualTo(LocalDate.of(2020, 1, 23));
		assertThat(article.description().text()).isEqualTo("Very blog, much post, so wow");
		assertThat(article.slug().value()).isEqualTo("cool-blog-post");
	}

	@Test
	void createFromFile_allTagsCorrect_getValidArticle() {
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
