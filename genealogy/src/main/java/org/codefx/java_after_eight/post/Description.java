package org.codefx.java_after_eight.post;

import org.codefx.java_after_eight.Utils;

import static java.util.Objects.requireNonNull;

public record Description(String text) {

	public Description {
		requireNonNull(text);
		text = Utils.removeOuterQuotationMarks(text).strip();
		if (text.isBlank())
			throw new IllegalArgumentException("Description can't have an empty text.");
	}

}
