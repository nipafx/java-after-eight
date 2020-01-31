package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.genealogist.RelationType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeightsTests {

	public static final RelationType TAG_TYPE = new RelationType("tag");
	public static final RelationType LIST_TYPE = new RelationType("list");

	@Test
	void nullRelationType_throwsException() {
		Map<RelationType, Double> weightMap = new HashMap<>();
		weightMap.put(null, 1.0);
		assertThatThrownBy(() -> new Weights(weightMap, 0.5)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void nullWeight_throwsException() {
		Map<RelationType, Double> weightMap = new HashMap<>();
		weightMap.put(TAG_TYPE, null);
		assertThatThrownBy(() -> new Weights(weightMap, 0.5)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void knownRelationType_returnsWeight() {
		Weights weights = new Weights(Map.of(TAG_TYPE, 0.42), 0.5);

		assertThat(weights.weightOf(TAG_TYPE)).isEqualTo(0.42);
	}

	@Test
	void unknownRelationType_returnsDefaultWeight() {
		Weights weights = new Weights(Map.of(TAG_TYPE, 0.42), 0.5);

		assertThat(weights.weightOf(LIST_TYPE)).isEqualTo(0.5);
	}

}
