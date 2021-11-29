package org.codefx.java_after_eight.genealogists.random;

import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.RelationType;
import org.codefx.java_after_eight.genealogist.TypedRelation;
import org.codefx.java_after_eight.post.Post;

import java.util.Random;

import static java.util.Objects.requireNonNull;

public class RandomGenealogist implements Genealogist {

	private static final RelationType TYPE = new RelationType("random");

	private final Random random;

	public RandomGenealogist(Random random) {
		this.random = requireNonNull(random);
	}

	@Override
	public TypedRelation infer(Post post1, Post post2) {
		long score = random.nextLong();
		return new TypedRelation(post1, post2, TYPE, score);
	}

}
