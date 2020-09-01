package org.codefx.java_after_eight.recommendation;

import org.codefx.java_after_eight.genealogy.Relation;
import org.codefx.java_after_eight.post.Post;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

// Don't judge me for the name - recommend a better one (see what I did there?)
public class Recommender {

	public Stream<Recommendation> recommend(Stream<Relation> relations, int perPost) {
		if (perPost < 1)
			throw new IllegalArgumentException(
					"Number of recommendations per post must be greater zero: " + perPost);

		Comparator<Relation> byPostThenByDecreasingScore =
				comparing((Relation relation) -> relation.post1().slug())
						.thenComparing(Relation::score)
						.reversed();
		Map<Post, List<Relation>> byPost = relations
				.sorted(byPostThenByDecreasingScore)
				.collect(groupingBy(Relation::post1));
		return byPost
				.entrySet().stream()
				.map(postWithRelations -> Recommendation.from(
						postWithRelations.getKey(),
						postWithRelations.getValue().stream().map(Relation::post2),
						perPost));

	}

}
