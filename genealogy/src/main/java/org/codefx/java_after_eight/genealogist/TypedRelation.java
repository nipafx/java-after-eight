package org.codefx.java_after_eight.genealogist;

import org.codefx.java_after_eight.post.Post;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class TypedRelation {

	private final Post post1;
	private final Post post2;
	private final RelationType type;
	private final long score;

	public TypedRelation(Post post1, Post post2, RelationType type, long score) {
		this.post1 = requireNonNull(post1);
		this.post2 = requireNonNull(post2);
		this.type = requireNonNull(type);
		this.score = score;
		if (score < 0 || 100 < score)
			throw new IllegalArgumentException("Score should be in interval [0; 100]: " + score);
	}

	public Post post1() {
		return post1;
	}

	public Post post2() {
		return post2;
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
				post1.equals(that.post1) &&
				post2.equals(that.post2) &&
				type.equals(that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(post1, post2, type, score);
	}

	@Override
	public String toString() {
		return "Relation{" +
				"post1=" + post1.slug().value() +
				", post2=" + post2.slug().value() +
				", type='" + type + '\'' +
				", score=" + score +
				'}';
	}

}
