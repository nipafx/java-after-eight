package org.codefx.demo.java_after_eight.article;

import org.junit.jupiter.api.Nested;

class TitleTests {

	@Nested
	class QuotationTests implements TextParserTests.QuotationTests {

		@Override
		public String parseCreateExtract(String text) {
			return Title.from(text).text();
		}

	}
}