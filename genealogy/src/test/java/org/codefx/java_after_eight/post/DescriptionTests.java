package org.codefx.java_after_eight.post;

import org.codefx.java_after_eight.TextParserTests;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DescriptionTests {

	@Test
	void emptyText_exception() {
		assertThatThrownBy(() -> new Description("")).isInstanceOf(IllegalArgumentException.class);
	}

	@Nested
	class QuotationTests implements TextParserTests.QuotationTests {

		@Override
		public String parseCreateExtract(String text) {
			return new Description(text).text();
		}

	}

}
