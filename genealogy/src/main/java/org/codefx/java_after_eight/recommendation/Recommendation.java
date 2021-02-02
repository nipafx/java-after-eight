package org.codefx.java_after_eight.recommendation;

import org.codefx.java_after_eight.post.Post;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableList;

public record Recommendation(
		Post post,
		List<Post> recommendedPosts) {

	public Recommendation {
		requireNonNull(post);
		requireNonNull(recommendedPosts);
	}

	static Recommendation from(Post post, Stream<Post> sortedRecommendations, int perPost) {
		var recommendations = sortedRecommendations.limit(perPost).toList();
		return new Recommendation(requireNonNull(post), recommendations);
	}

	public List<Post> recommendedPosts() {
		return List.copyOf(recommendedPosts);
	}

}
