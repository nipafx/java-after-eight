package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.post.PostTestHelper;
import org.codefx.java_after_eight.genealogist.RelationType;
import org.codefx.java_after_eight.genealogist.TypedRelation;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Math.round;
import static org.assertj.core.api.Assertions.assertThat;

class RelationTests {

	private static final double TAG_WEIGHT = 1.0;
	private static final double LINK_WEIGHT = 0.25;

	private final Post postA = PostTestHelper.createWithSlug("a");
	private final Post postB = PostTestHelper.createWithSlug("b");

	private final RelationType tagRelation = new RelationType("tag");
	private final RelationType linkRelation = new RelationType("link");

	private final Weights weights = new Weights(
			Map.of(
					tagRelation, TAG_WEIGHT,
					linkRelation, LINK_WEIGHT),
			0.5);

	/*
	 * TODO: various failure states
	 */

	@Test
	void singleTypedRelation_weightOne_samePostsAndScore() {
		int score = 60;
		Stream<TypedRelation> typedRelations = Stream.of(
				new TypedRelation(postA, postB, tagRelation, score)
		);

		Relation relation = Relation.aggregate(typedRelations, weights);

		assertThat(relation.post1()).isEqualTo(postA);
		assertThat(relation.post2()).isEqualTo(postB);
		assertThat(relation.score()).isEqualTo(score);
	}

	@Test
	void twoTypedRelation_weightOne_averagedScore() {
		Stream<TypedRelation> typedRelations = Stream.of(
				new TypedRelation(postA, postB, tagRelation, 40),
				new TypedRelation(postA, postB, tagRelation, 80)
		);

		Relation relation = Relation.aggregate(typedRelations, weights);

		assertThat(relation.score()).isEqualTo((40 + 80) / 2);
	}

	@Test
	void twoTypedRelation_differingWeight_weightedScore() {
		Stream<TypedRelation> typedRelations = Stream.of(
				new TypedRelation(postA, postB, tagRelation, 40),
				new TypedRelation(postA, postB, linkRelation, 80)
		);

		Relation relation = Relation.aggregate(typedRelations, weights);

		double expectedScore = (40 * TAG_WEIGHT + 80 * LINK_WEIGHT) / 2;
		assertThat(relation.score()).isEqualTo(round(expectedScore));
	}

}
