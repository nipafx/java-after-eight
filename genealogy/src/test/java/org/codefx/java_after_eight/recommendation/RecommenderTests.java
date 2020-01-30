package org.codefx.java_after_eight.recommendation;

import org.codefx.java_after_eight.article.Article;
import org.codefx.java_after_eight.article.ArticleTestHelper;
import org.codefx.java_after_eight.genealogy.Relation;
import org.codefx.java_after_eight.genealogy.RelationTestHelper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RecommenderTests {

	private final Article articleA = ArticleTestHelper.createWithSlug("a");
	private final Article articleB = ArticleTestHelper.createWithSlug("b");
	private final Article articleC = ArticleTestHelper.createWithSlug("c");

	private final Relation relation_AB = RelationTestHelper.create(articleA, articleB, 60L);
	private final Relation relation_AC = RelationTestHelper.create(articleA, articleC, 40L);
	private final Relation relation_BA = RelationTestHelper.create(articleB, articleA, 50L);
	private final Relation relation_BC = RelationTestHelper.create(articleB, articleC, 70L);
	private final Relation relation_CA = RelationTestHelper.create(articleC, articleA, 80L);
	private final Relation relation_CB = RelationTestHelper.create(articleC, articleB, 60L);

	private final Recommender recommender = new Recommender();

	@Test
	void forOneArticle_oneRelation() {
		Stream<Recommendation> recommendations = recommender.recommend(
				Stream.of(relation_AC),
				1);

		// REFACTOR 9: collection factories
		assertThat(recommendations).containsExactlyInAnyOrder(
				new Recommendation(articleA, Arrays.asList(articleC)));
	}

	@Test
	void forOneArticle_twoRelations() {
		Stream<Recommendation> recommendations = recommender.recommend(
				Stream.of(relation_AB, relation_AC),
				1);

		// REFACTOR 9: collection factories
		assertThat(recommendations).containsExactlyInAnyOrder(
				new Recommendation(articleA, Arrays.asList(articleB)));
	}

	@Test
	void forManyArticles_oneRelationEach() {
		Stream<Recommendation> recommendations = recommender.recommend(
				Stream.of(relation_AC, relation_BC, relation_CB),
				1);

		// REFACTOR 9: collection factories
		assertThat(recommendations).containsExactlyInAnyOrder(
				new Recommendation(articleA, Arrays.asList(articleC)),
				new Recommendation(articleB, Arrays.asList(articleC)),
				new Recommendation(articleC, Arrays.asList(articleB))
		);
	}

	@Test
	void forManyArticles_twoRelationsEach() {
		Stream<Recommendation> recommendations = recommender.recommend(
				Stream.of(relation_AB, relation_AC, relation_BA, relation_BC, relation_CA, relation_CB),
				1);

		// REFACTOR 9: collection factories
		assertThat(recommendations).containsExactlyInAnyOrder(
				new Recommendation(articleA, Arrays.asList(articleB)),
				new Recommendation(articleB, Arrays.asList(articleC)),
				new Recommendation(articleC, Arrays.asList(articleA))
		);
	}

}
