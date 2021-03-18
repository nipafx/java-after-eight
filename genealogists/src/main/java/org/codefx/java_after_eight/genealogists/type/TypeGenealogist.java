package org.codefx.java_after_eight.genealogists.type;

import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.RelationType;
import org.codefx.java_after_eight.genealogist.TypedRelation;
import org.codefx.java_after_eight.post.Post;

public class TypeGenealogist implements Genealogist {

	private static final RelationType TYPE = new RelationType("type");

	@Override
	public TypedRelation infer(Post post1, Post post2) {
		long score = 0;
		switch (post2.getClass().getSimpleName()) {
			case "Article":
				score = 50;
				break;
			case "Video":
				score = 90;
				break;
			case "Talk":
				score = 20;
				break;
		}

		return new TypedRelation(post1, post2, TYPE, score);
	}

}
