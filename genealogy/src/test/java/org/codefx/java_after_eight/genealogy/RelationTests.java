package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.article.Article;
import org.codefx.java_after_eight.article.ArticleTestHelper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RelationTests {

	private static final double TAG_WEIGHT = 1.0;
	private static final double LINK_WEIGHT = 0.25;

	private final Article articleA = ArticleTestHelper.createWithSlug("a");
	private final Article articleB = ArticleTestHelper.createWithSlug("b");

	private final RelationType tagRelation = RelationType.from("tag");
	private final RelationType linkRelation = RelationType.from("link");

	// REFACTOR 9: collection factories (remove constructor)
	private final Weights weights;

	RelationTests() {
		Map<RelationType, Double> weights = new HashMap<>();
		weights.put(tagRelation, TAG_WEIGHT);
		weights.put(linkRelation, LINK_WEIGHT);
		this.weights = Weights.from(weights, 0.5);
	}

	/*
	 * TODO: various failure states
	 */

	@Test
	void singleTypedRelation_weightOne_sameArticlesAndScore() {
		int score = 60;
		Stream<TypedRelation> typedRelations = Stream.of(
				TypedRelation.from(articleA, articleB, tagRelation, score)
		);

		Relation relation = Relation.aggregate(typedRelations, weights);

		assertThat(relation.article1()).isEqualTo(articleA);
		assertThat(relation.article2()).isEqualTo(articleB);
		assertThat(relation.score()).isEqualTo(score);
	}

	@Test
	void twoTypedRelation_weightOne_averagedScore() {
		Stream<TypedRelation> typedRelations = Stream.of(
				TypedRelation.from(articleA, articleB, tagRelation, 40),
				TypedRelation.from(articleA, articleB, tagRelation, 80)
		);

		Relation relation = Relation.aggregate(typedRelations, weights);

		assertThat(relation.score()).isEqualTo((40 + 80) / 2);
	}

	@Test
	void twoTypedRelation_differingWeight_weightedScore() {
		Stream<TypedRelation> typedRelations = Stream.of(
				TypedRelation.from(articleA, articleB, tagRelation, 40),
				TypedRelation.from(articleA, articleB, linkRelation, 80)
		);

		Relation relation = Relation.aggregate(typedRelations, weights);

		double expectedScore = (40 * TAG_WEIGHT + 80 * LINK_WEIGHT) / 2;
		assertThat(relation.score()).isEqualTo(Math.round(expectedScore));
	}

}
