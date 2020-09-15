package org.codefx.java_after_eight.recommendation;

import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.post.Slug;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableList;

public class Recommendation {

	private final Post post;
	private final List<Post> recommendedPosts;

	Recommendation(Post post, List<Post> recommendedPosts) {
		this.post = requireNonNull(post);
		this.recommendedPosts = requireNonNull(recommendedPosts);
	}

	static Recommendation from(Post post, Stream<Post> sortedRecommendations, int perPost) {
		var recommendations = sortedRecommendations.limit(perPost).collect(toUnmodifiableList());
		return new Recommendation(requireNonNull(post), recommendations);
	}

	public Post post() {
		return post;
	}

	public Stream<Post> recommendedPosts() {
		return recommendedPosts.stream();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Recommendation that = (Recommendation) o;
		return post.equals(that.post) &&
				recommendedPosts.equals(that.recommendedPosts);
	}

	@Override
	public int hashCode() {
		return Objects.hash(post);
	}

	@Override
	public String toString() {
		return "Recommendation{" +
				"post=" + post.slug().value() +
				", recommendedPosts=" + recommendedPosts.stream()
				.map(Post::slug)
				.map(Slug::value)
				.collect(joining(", ")) +
				'}';
	}

}
