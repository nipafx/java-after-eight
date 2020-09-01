package org.codefx.java_after_eight.recommendation;

import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.post.PostTestHelper;
import org.codefx.java_after_eight.genealogy.Relation;
import org.codefx.java_after_eight.genealogy.RelationTestHelper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RecommenderTests {

	private final Post postA = PostTestHelper.createWithSlug("a");
	private final Post postB = PostTestHelper.createWithSlug("b");
	private final Post postC = PostTestHelper.createWithSlug("c");

	private final Relation relation_AB = RelationTestHelper.create(postA, postB, 60L);
	private final Relation relation_AC = RelationTestHelper.create(postA, postC, 40L);
	private final Relation relation_BA = RelationTestHelper.create(postB, postA, 50L);
	private final Relation relation_BC = RelationTestHelper.create(postB, postC, 70L);
	private final Relation relation_CA = RelationTestHelper.create(postC, postA, 80L);
	private final Relation relation_CB = RelationTestHelper.create(postC, postB, 60L);

	private final Recommender recommender = new Recommender();

	@Test
	void forOnePost_oneRelation() {
		Stream<Recommendation> recommendations = recommender.recommend(
				Stream.of(relation_AC),
				1);

		assertThat(recommendations).containsExactlyInAnyOrder(
				new Recommendation(postA, Arrays.asList(postC)));
	}

	@Test
	void forOnePost_twoRelations() {
		Stream<Recommendation> recommendations = recommender.recommend(
				Stream.of(relation_AB, relation_AC),
				1);

		assertThat(recommendations).containsExactlyInAnyOrder(
				new Recommendation(postA, Arrays.asList(postB)));
	}

	@Test
	void forManyPosts_oneRelationEach() {
		Stream<Recommendation> recommendations = recommender.recommend(
				Stream.of(relation_AC, relation_BC, relation_CB),
				1);

		assertThat(recommendations).containsExactlyInAnyOrder(
				new Recommendation(postA, Arrays.asList(postC)),
				new Recommendation(postB, Arrays.asList(postC)),
				new Recommendation(postC, Arrays.asList(postB))
		);
	}

	@Test
	void forManyPosts_twoRelationsEach() {
		Stream<Recommendation> recommendations = recommender.recommend(
				Stream.of(relation_AB, relation_AC, relation_BA, relation_BC, relation_CA, relation_CB),
				1);

		assertThat(recommendations).containsExactlyInAnyOrder(
				new Recommendation(postA, Arrays.asList(postB)),
				new Recommendation(postB, Arrays.asList(postC)),
				new Recommendation(postC, Arrays.asList(postA))
		);
	}

}
