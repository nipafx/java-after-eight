package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.article.Article;
import org.codefx.java_after_eight.genealogist.TypedRelation;

import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.Math.round;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class Relation {

	private final Article article1;
	private final Article article2;
	private final long score;

	Relation(Article article1, Article article2, long score) {
		this.article1 = requireNonNull(article1);
		this.article2 = requireNonNull(article2);
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

	public Article article1() {
		return article1;
	}

	public Article article2() {
		return article2;
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
				article1.equals(relation.article1) &&
				article2.equals(relation.article2);
	}

	@Override
	public int hashCode() {
		return Objects.hash(article1, article2, score);
	}

	@Override
	public String toString() {
		return "Relation{" +
				"article1=" + article1.slug().value() +
				", article2=" + article2.slug().value() +
				", score=" + score +
				'}';
	}

	private static class UnfinishedRelation {

		private final Article article1;
		private final Article article2;
		private double scoreTotal;
		private long scoreCount;

		public UnfinishedRelation(TypedRelation relation, double weight) {
			this.article1 = relation.article1();
			this.article2 = relation.article2();
			this.scoreTotal = relation.score() * weight;
			this.scoreCount = 1;
		}

		public UnfinishedRelation fold(UnfinishedRelation other) {
			if (article1 != other.article1)
				throw new IllegalArgumentException(format(
						"All typed relations must belong to the same article: %s vs %s", article1, other.article1));
			if (article2 != other.article2)
				throw new IllegalArgumentException(format(
						"All typed relations must belong to the same article: %s vs %s", article2, other.article2));
			scoreTotal += other.scoreTotal;
			scoreCount += other.scoreCount;
			return this;
		}

		public Relation finish() {
			return new Relation(article1, article2, round(scoreTotal / scoreCount));
		}

	}

}
