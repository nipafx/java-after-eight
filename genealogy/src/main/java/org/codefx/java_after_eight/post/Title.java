package org.codefx.java_after_eight.post;

import org.codefx.java_after_eight.Utils;

import static java.util.Objects.requireNonNull;

public record Title(String text) {

	public Title {
		requireNonNull(text);
		var unquotedText = Utils.removeOuterQuotationMarks(text);
		if (unquotedText.isBlank())
			throw new IllegalArgumentException("Titles can't have an empty text.");
		text = unquotedText;
	}

}
