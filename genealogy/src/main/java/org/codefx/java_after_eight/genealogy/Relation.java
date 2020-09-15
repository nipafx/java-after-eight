package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.genealogist.TypedRelation;
import org.codefx.java_after_eight.post.Post;

import java.util.stream.Stream;

import static java.lang.Math.round;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.teeing;
import static org.codefx.java_after_eight.Utils.collectEqualElement;

public record Relation(
		Post post1,
		Post post2,
		long score) {

	public Relation {
		requireNonNull(post1);
		requireNonNull(post2);
		if (score < 0 || 100 < score)
			throw new IllegalArgumentException("Score should be in interval [0; 100]: " + toString());
	}

	static Relation aggregate(Stream<TypedRelation> typedRelations, Weights weights) {
		record Posts(Post post1, Post post2) { }
		return typedRelations.collect(
				teeing(
						mapping(
								rel -> new Posts(rel.post1(), rel.post2()),
								collectEqualElement()),
						averagingDouble(rel -> rel.score() * weights.weightOf(rel.type())),
						(posts, score) -> posts.map(ps -> new Relation(ps.post1(), ps.post2(), round(score)))
				))
				.orElseThrow(() -> new IllegalArgumentException("Can't create relation from zero typed relations."));
	}

}
