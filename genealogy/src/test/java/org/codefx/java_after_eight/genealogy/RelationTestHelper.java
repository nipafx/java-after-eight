package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.article.Article;

public class RelationTestHelper {

	public static Relation create(Article article1, Article article2, long score) {
		return new Relation(article1, article2, score);
	}

}
