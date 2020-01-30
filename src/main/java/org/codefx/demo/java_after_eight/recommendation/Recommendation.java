package org.codefx.demo.java_after_eight.recommendation;

import org.codefx.demo.java_after_eight.article.Article;
import org.codefx.demo.java_after_eight.article.Slug;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class Recommendation {

	private final Article article;
	private final List<Article> recommendations;

	Recommendation(Article article, List<Article> recommendations) {
		this.article = requireNonNull(article);
		this.recommendations = requireNonNull(recommendations);
	}

	static Recommendation from(Article article, Stream<Article> sortedRecommendations, int perArticle) {
		List<Article> recommendations = sortedRecommendations.limit(perArticle).collect(toList());
		return new Recommendation(article, recommendations);
	}

	public Article article() {
		return article;
	}

	public Stream<Article> recommendations() {
		return recommendations.stream();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Recommendation that = (Recommendation) o;
		return article.equals(that.article) &&
				recommendations.equals(that.recommendations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(article);
	}

	@Override
	public String toString() {
		return "Recommendation{" +
				"article=" + article.slug().value() +
				", recommendations=" + recommendations.stream()
				.map(Article::slug)
				.map(Slug::value)
				.collect(joining(", ")) +
				'}';
	}

}
