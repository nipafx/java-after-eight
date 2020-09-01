package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.genealogist.TypedRelation;
import org.codefx.java_after_eight.post.Post;

import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.Math.round;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

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
		return typedRelations
				.map(relation -> new UnfinishedRelation(relation, weights.weightOf(relation.type())))
				.reduce(UnfinishedRelation::fold)
				.map(UnfinishedRelation::finish)
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

	private static class UnfinishedRelation {

		private final Post post1;
		private final Post post2;
		private double scoreTotal;
		private long scoreCount;

		public UnfinishedRelation(TypedRelation relation, double weight) {
			this.post1 = relation.post1();
			this.post2 = relation.post2();
			this.scoreTotal = relation.score() * weight;
			this.scoreCount = 1;
		}

		public UnfinishedRelation fold(UnfinishedRelation other) {
			if (post1 != other.post1)
				throw new IllegalArgumentException(format(
						"All typed relations must belong to the same post: %s vs %s", post1, other.post1));
			if (post2 != other.post2)
				throw new IllegalArgumentException(format(
						"All typed relations must belong to the same post: %s vs %s", post2, other.post2));
			scoreTotal += other.scoreTotal;
			scoreCount += other.scoreCount;
			return this;
		}

		public Relation finish() {
			return new Relation(post1, post2, round(scoreTotal / scoreCount));
		}

	}

}
