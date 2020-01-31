package org.codefx.java_after_eight.article;

import org.codefx.java_after_eight.TextParserTests;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TitleTests {

	@Test
	void emptyText_exception() {
		assertThatThrownBy(() -> Title.from("")).isInstanceOf(IllegalArgumentException.class);
	}


	@Nested
	class QuotationTests implements TextParserTests.QuotationTests {

		@Override
		public String parseCreateExtract(String text) {
			return Title.from(text).text();
		}

	}

}
