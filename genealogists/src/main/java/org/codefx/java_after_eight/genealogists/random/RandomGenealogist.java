package org.codefx.java_after_eight.genealogists.random;

import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.RelationType;
import org.codefx.java_after_eight.genealogist.TypedRelation;
import org.codefx.java_after_eight.post.Post;

import java.util.random.RandomGenerator;

import static java.util.Objects.requireNonNull;

public class RandomGenealogist implements Genealogist {

	private static final RelationType TYPE = new RelationType("random");

	private final RandomGenerator random;

	public RandomGenealogist(RandomGenerator random) {
		this.random = requireNonNull(random);
	}

	@Override
	public TypedRelation infer(Post post1, Post post2) {
		long score = random.nextLong(101);
		return new TypedRelation(post1, post2, TYPE, score);
	}

}