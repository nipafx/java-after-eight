package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.post.Post;

public class RelationTestHelper {

	public static Relation create(Post post1, Post post2, long score) {
		return new Relation(post1, post2, score);
	}

}
