package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.article.Article;
import org.codefx.java_after_eight.article.ArticleTestHelper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Math.round;
import static org.assertj.core.api.Assertions.assertThat;

class GenealogyTests {

	private static final int TAG_SCORE_A_B = 80;
	private static final int TAG_SCORE_A_C = 60;
	private static final int TAG_SCORE_B_A = 70;
	private static final int TAG_SCORE_B_C = 50;
	private static final int TAG_SCORE_C_A = 50;
	private static final int TAG_SCORE_C_B = 40;

	private static final int LINK_SCORE_A_B = 60;
	private static final int LINK_SCORE_A_C = 40;
	private static final int LINK_SCORE_B_A = 50;
	private static final int LINK_SCORE_B_C = 30;
	private static final int LINK_SCORE_C_A = 30;
	private static final int LINK_SCORE_C_B = 20;

	private static final double TAG_WEIGHT = 1.0;
	private static final double LINK_WEIGHT = 0.75;

	private final Article articleA = ArticleTestHelper.createWithSlug("a");
	private final Article articleB = ArticleTestHelper.createWithSlug("b");
	private final Article articleC = ArticleTestHelper.createWithSlug("c");

	private final RelationType tagRelation = RelationType.from("tag");
	private final RelationType linkRelation = RelationType.from("link");

	private final Genealogist tagGenealogist = (article1, article2) ->
			TypedRelation.from(article1, article2, tagRelation, tagScore(article1, article2));
	private final Genealogist linkGenealogist = (article1, article2) ->
			TypedRelation.from(article1, article2, linkRelation, linkScore(article1, article2));

	private final Weights weights;

	GenealogyTests() {
		Map<RelationType, Double> weights = new HashMap<>();
		weights.put(tagRelation, TAG_WEIGHT);
		weights.put(linkRelation, LINK_WEIGHT);
		this.weights = Weights.from(weights, 0.5);
	}

	private int tagScore(Article article1, Article article2) {
		if (article1 == article2)
			return 100;
		if (article1 == articleA && article2 == articleB)
			return TAG_SCORE_A_B;
		if (article1 == articleA && article2 == articleC)
			return TAG_SCORE_A_C;
		if (article1 == articleB && article2 == articleA)
			return TAG_SCORE_B_A;
		if (article1 == articleB && article2 == articleC)
			return TAG_SCORE_B_C;
		if (article1 == articleC && article2 == articleA)
			return TAG_SCORE_C_A;
		if (article1 == articleC && article2 == articleB)
			return TAG_SCORE_C_B;
		return 0;
	}

	private int linkScore(Article article1, Article article2) {
		if (article1 == article2)
			return 100;
		if (article1 == articleA && article2 == articleB)
			return LINK_SCORE_A_B;
		if (article1 == articleA && article2 == articleC)
			return LINK_SCORE_A_C;
		if (article1 == articleB && article2 == articleA)
			return LINK_SCORE_B_A;
		if (article1 == articleB && article2 == articleC)
			return LINK_SCORE_B_C;
		if (article1 == articleC && article2 == articleA)
			return LINK_SCORE_C_A;
		if (article1 == articleC && article2 == articleB)
			return LINK_SCORE_C_B;
		return 0;
	}

	@Test
	void oneGenealogist_twoArticles() {
		Genealogy genealogy = new Genealogy(
				Arrays.asList(articleA, articleB),
				Arrays.asList(tagGenealogist),
				weights);

		Stream<Relation> relations = genealogy.inferRelations();

		assertThat(relations).containsExactlyInAnyOrder(
				new Relation(articleA, articleB, round(TAG_SCORE_A_B * TAG_WEIGHT)),
				new Relation(articleB, articleA, round(TAG_SCORE_B_A * TAG_WEIGHT))
		);
	}

	@Test
	void otherGenealogist_twoArticles() {
		Genealogy genealogy = new Genealogy(
				Arrays.asList(articleA, articleB),
				Arrays.asList(linkGenealogist),
				weights);

		Stream<Relation> relations = genealogy.inferRelations();

		assertThat(relations).containsExactlyInAnyOrder(
				new Relation(articleA, articleB, round(LINK_SCORE_A_B * LINK_WEIGHT)),
				new Relation(articleB, articleA, round(LINK_SCORE_B_A * LINK_WEIGHT))
		);
	}

	@Test
	void oneGenealogist_threeArticles() {
		Genealogy genealogy = new Genealogy(
				Arrays.asList(articleA, articleB, articleC),
				Arrays.asList(tagGenealogist),
				weights);

		Stream<Relation> relations = genealogy.inferRelations();

		assertThat(relations).containsExactlyInAnyOrder(
				new Relation(articleA, articleB, round(TAG_SCORE_A_B * TAG_WEIGHT)),
				new Relation(articleA, articleC, round(TAG_SCORE_A_C * TAG_WEIGHT)),
				new Relation(articleB, articleA, round(TAG_SCORE_B_A * TAG_WEIGHT)),
				new Relation(articleB, articleC, round(TAG_SCORE_B_C * TAG_WEIGHT)),
				new Relation(articleC, articleA, round(TAG_SCORE_C_A * TAG_WEIGHT)),
				new Relation(articleC, articleB, round(TAG_SCORE_C_B * TAG_WEIGHT))
		);
	}

	@Test
	void twoGenealogists_threeArticles() {
		Genealogy genealogy = new Genealogy(
				Arrays.asList(articleA, articleB, articleC),
				Arrays.asList(tagGenealogist, linkGenealogist),
				weights);

		Stream<Relation> relations = genealogy.inferRelations();

		assertThat(relations).containsExactlyInAnyOrder(
				new Relation(articleA, articleB, round((TAG_SCORE_A_B * TAG_WEIGHT + LINK_SCORE_A_B * LINK_WEIGHT) / 2)),
				new Relation(articleA, articleC, round((TAG_SCORE_A_C * TAG_WEIGHT + LINK_SCORE_A_C * LINK_WEIGHT) / 2)),
				new Relation(articleB, articleA, round((TAG_SCORE_B_A * TAG_WEIGHT + LINK_SCORE_B_A * LINK_WEIGHT) / 2)),
				new Relation(articleB, articleC, round((TAG_SCORE_B_C * TAG_WEIGHT + LINK_SCORE_B_C * LINK_WEIGHT) / 2)),
				new Relation(articleC, articleA, round((TAG_SCORE_C_A * TAG_WEIGHT + LINK_SCORE_C_A * LINK_WEIGHT) / 2)),
				new Relation(articleC, articleB, round((TAG_SCORE_C_B * TAG_WEIGHT + LINK_SCORE_C_B * LINK_WEIGHT) / 2))
		);
	}

}
