package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.genealogist.TypedRelation;
import org.codefx.java_after_eight.post.Post;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.Math.round;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.teeing;
import static org.codefx.java_after_eight.Utils.collectEqualElement;

public class Relation {

	private final Post post1;
	private final Post post2;
	private final long score;

	Relation(Post post1, Post post2, long score) {
		this.post1 = requireNonNull(post1);
		this.post2 = requireNonNull(post2);
		this.score = score;

		if (score < 0 || 100 < score)
			throw new IllegalArgumentException("Score should be in interval [0; 100]: " + toString());
	}

	static Relation aggregate(Stream<TypedRelation> typedRelations, Weights weights) {
		return typedRelations.collect(
				teeing(
						mapping(
								rel -> new Post[]{ rel.post1(), rel.post2() },
								collectEqualElement(Arrays::equals)),
						averagingDouble(rel -> rel.score() * weights.weightOf(rel.type())),
						(posts, score) -> posts.map(ps -> new Relation(ps[0], ps[1], round(score)))
				))
				.orElseThrow(() -> new IllegalArgumentException("Can't create relation from zero typed relations."));
	}

	public Post post1() {
		return post1;
	}

	public Post post2() {
		return post2;
	}

	public long score() {
		return score;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Relation relation = (Relation) o;
		return score == relation.score &&
				post1.equals(relation.post1) &&
				post2.equals(relation.post2);
	}

	@Override
	public int hashCode() {
		return Objects.hash(post1, post2, score);
	}

	@Override
	public String toString() {
		return "Relation{" +
				"post1=" + post1.slug().value() +
				", post2=" + post2.slug().value() +
				", score=" + score +
				'}';
	}

}