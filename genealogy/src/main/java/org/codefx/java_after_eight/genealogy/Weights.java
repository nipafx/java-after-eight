package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.genealogist.RelationType;

import java.util.Map;

public class Weights {

	private final Map<RelationType, Double> weights;
	private final double defaultWeight;

	public Weights(Map<RelationType, Double> weights, double defaultWeight) {
		this.weights = Map.copyOf(weights);
		this.defaultWeight = defaultWeight;
	}

	public static Weights allEqual() {
		return new Weights(Map.of(), 1);
	}

	public double weightOf(RelationType genealogistType) {
		return weights.getOrDefault(genealogistType, defaultWeight);
	}

}
