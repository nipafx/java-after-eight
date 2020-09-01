package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.post.PostTestHelper;
import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.RelationType;
import org.codefx.java_after_eight.genealogist.TypedRelation;
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

	private final Post postA = PostTestHelper.createWithSlug("a");
	private final Post postB = PostTestHelper.createWithSlug("b");
	private final Post postC = PostTestHelper.createWithSlug("c");

	private final RelationType tagRelation = RelationType.from("tag");
	private final RelationType linkRelation = RelationType.from("link");

	private final Genealogist tagGenealogist = (Post1, Post2) ->
			TypedRelation.from(Post1, Post2, tagRelation, tagScore(Post1, Post2));
	private final Genealogist linkGenealogist = (Post1, Post2) ->
			TypedRelation.from(Post1, Post2, linkRelation, linkScore(Post1, Post2));

	private final Weights weights;

	GenealogyTests() {
		Map<RelationType, Double> weights = new HashMap<>();
		weights.put(tagRelation, TAG_WEIGHT);
		weights.put(linkRelation, LINK_WEIGHT);
		this.weights = Weights.from(weights, 0.5);
	}

	private int tagScore(Post post1, Post post2) {
		if (post1 == post2)
			return 100;
		if (post1 == postA && post2 == postB)
			return TAG_SCORE_A_B;
		if (post1 == postA && post2 == postC)
			return TAG_SCORE_A_C;
		if (post1 == postB && post2 == postA)
			return TAG_SCORE_B_A;
		if (post1 == postB && post2 == postC)
			return TAG_SCORE_B_C;
		if (post1 == postC && post2 == postA)
			return TAG_SCORE_C_A;
		if (post1 == postC && post2 == postB)
			return TAG_SCORE_C_B;
		return 0;
	}

	private int linkScore(Post post1, Post post2) {
		if (post1 == post2)
			return 100;
		if (post1 == postA && post2 == postB)
			return LINK_SCORE_A_B;
		if (post1 == postA && post2 == postC)
			return LINK_SCORE_A_C;
		if (post1 == postB && post2 == postA)
			return LINK_SCORE_B_A;
		if (post1 == postB && post2 == postC)
			return LINK_SCORE_B_C;
		if (post1 == postC && post2 == postA)
			return LINK_SCORE_C_A;
		if (post1 == postC && post2 == postB)
			return LINK_SCORE_C_B;
		return 0;
	}

	@Test
	void oneGenealogist_twoPosts() {
		Genealogy genealogy = new Genealogy(
				Arrays.asList(postA, postB),
				Arrays.asList(tagGenealogist),
				weights);

		Stream<Relation> relations = genealogy.inferRelations();

		assertThat(relations).containsExactlyInAnyOrder(
				new Relation(postA, postB, round(TAG_SCORE_A_B * TAG_WEIGHT)),
				new Relation(postB, postA, round(TAG_SCORE_B_A * TAG_WEIGHT))
		);
	}

	@Test
	void otherGenealogist_twoPosts() {
		Genealogy genealogy = new Genealogy(
				Arrays.asList(postA, postB),
				Arrays.asList(linkGenealogist),
				weights);

		Stream<Relation> relations = genealogy.inferRelations();

		assertThat(relations).containsExactlyInAnyOrder(
				new Relation(postA, postB, round(LINK_SCORE_A_B * LINK_WEIGHT)),
				new Relation(postB, postA, round(LINK_SCORE_B_A * LINK_WEIGHT))
		);
	}

	@Test
	void oneGenealogist_threePosts() {
		Genealogy genealogy = new Genealogy(
				Arrays.asList(postA, postB, postC),
				Arrays.asList(tagGenealogist),
				weights);

		Stream<Relation> relations = genealogy.inferRelations();

		assertThat(relations).containsExactlyInAnyOrder(
				new Relation(postA, postB, round(TAG_SCORE_A_B * TAG_WEIGHT)),
				new Relation(postA, postC, round(TAG_SCORE_A_C * TAG_WEIGHT)),
				new Relation(postB, postA, round(TAG_SCORE_B_A * TAG_WEIGHT)),
				new Relation(postB, postC, round(TAG_SCORE_B_C * TAG_WEIGHT)),
				new Relation(postC, postA, round(TAG_SCORE_C_A * TAG_WEIGHT)),
				new Relation(postC, postB, round(TAG_SCORE_C_B * TAG_WEIGHT))
		);
	}

	@Test
	void twoGenealogists_threePosts() {
		Genealogy genealogy = new Genealogy(
				Arrays.asList(postA, postB, postC),
				Arrays.asList(tagGenealogist, linkGenealogist),
				weights);

		Stream<Relation> relations = genealogy.inferRelations();

		assertThat(relations).containsExactlyInAnyOrder(
				new Relation(postA, postB, round((TAG_SCORE_A_B * TAG_WEIGHT + LINK_SCORE_A_B * LINK_WEIGHT) / 2)),
				new Relation(postA, postC, round((TAG_SCORE_A_C * TAG_WEIGHT + LINK_SCORE_A_C * LINK_WEIGHT) / 2)),
				new Relation(postB, postA, round((TAG_SCORE_B_A * TAG_WEIGHT + LINK_SCORE_B_A * LINK_WEIGHT) / 2)),
				new Relation(postB, postC, round((TAG_SCORE_B_C * TAG_WEIGHT + LINK_SCORE_B_C * LINK_WEIGHT) / 2)),
				new Relation(postC, postA, round((TAG_SCORE_C_A * TAG_WEIGHT + LINK_SCORE_C_A * LINK_WEIGHT) / 2)),
				new Relation(postC, postB, round((TAG_SCORE_C_B * TAG_WEIGHT + LINK_SCORE_C_B * LINK_WEIGHT) / 2))
		);
	}

}
