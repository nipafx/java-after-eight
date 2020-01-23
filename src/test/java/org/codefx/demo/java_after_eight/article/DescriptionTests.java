package org.codefx.demo.java_after_eight.article;

import org.junit.jupiter.api.Nested;

class DescriptionTests {

	private final TextParserTests.QuotationTests quotationTests = text -> Description.from(text).text();

	@Nested
	class QuotationTests implements TextParserTests.QuotationTests {

		@Override
		public String parseCreateExtract(String text) {
			return Description.from(text).text();
		}

	}

}
