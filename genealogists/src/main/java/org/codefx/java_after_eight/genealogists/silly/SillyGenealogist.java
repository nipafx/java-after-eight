package org.codefx.java_after_eight.genealogists.silly;

import org.codefx.java_after_eight.article.Article;
import org.codefx.java_after_eight.genealogy.Genealogist;
import org.codefx.java_after_eight.genealogy.RelationType;
import org.codefx.java_after_eight.genealogy.TypedRelation;

import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.round;
import static java.util.stream.Collectors.toSet;

public class SillyGenealogist implements Genealogist {

	private static final RelationType TYPE = RelationType.from("silly");

	@Override
	public TypedRelation infer(Article article1, Article article2) {
		Set<Integer> article1Letters = titleLetters(article1);
		Set<Integer> article2Letters = titleLetters(article2);
		Set<Integer> intersection = new HashSet<>(article1Letters);
		intersection.retainAll(article2Letters);
		long score = round((100.0 * intersection.size()) / article1Letters.size());

		return TypedRelation.from(article1, article2, TYPE, score);
	}

	private static Set<Integer> titleLetters(Article article) {
		return article
				.title()
				.text()
				.toLowerCase()
				.chars().boxed()
				.collect(toSet());
	}

}
