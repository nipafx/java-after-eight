package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.article.Article;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

// REFACTOR 14: record
public class TypedRelation {

	private final Article article1;
	private final Article article2;
	private final RelationType type;
	private final long score;

	private TypedRelation(Article article1, Article article2, RelationType type, long score) {
		this.article1 = requireNonNull(article1);
		this.article2 = requireNonNull(article2);
		this.type = requireNonNull(type);
		this.score = requireNonNull(score);

		if (score < 0 || 100 < score)
			throw new IllegalArgumentException("Score should be in interval [0; 100]: " + toString());
	}

	public static TypedRelation from(Article article1, Article article2, RelationType type, long score) {
		return new TypedRelation(article1, article2, type, score);
	}

	public Article article1() {
		return article1;
	}

	public Article article2() {
		return article2;
	}

	public RelationType type() {
		return type;
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
		TypedRelation that = (TypedRelation) o;
		return score == that.score &&
				article1.equals(that.article1) &&
				article2.equals(that.article2) &&
				type.equals(that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(article1, article2, type, score);
	}

	@Override
	public String toString() {
		return "Relation{" +
				"article1=" + article1.slug().value() +
				", article2=" + article2.slug().value() +
				", type='" + type + '\'' +
				", score=" + score +
				'}';
	}

}
