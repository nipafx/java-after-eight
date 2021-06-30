package org.codefx.java_after_eight.genealogists.type;

import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.RelationType;
import org.codefx.java_after_eight.genealogist.TypedRelation;
import org.codefx.java_after_eight.post.Article;
import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.post.Talk;
import org.codefx.java_after_eight.post.Video;

public class TypeGenealogist implements Genealogist {

	private static final RelationType TYPE = new RelationType("type");

	@Override
	public TypedRelation infer(Post post1, Post post2) {
		long score = switch (post2) {
			case Article __ -> 50;
			case Video __ -> 90;
			case Talk __ -> 20;
		};

		return new TypedRelation(post1, post2, TYPE, score);
	}

}
