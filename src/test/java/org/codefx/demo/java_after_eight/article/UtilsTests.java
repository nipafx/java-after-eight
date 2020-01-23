package org.codefx.demo.java_after_eight.article;

import org.junit.jupiter.api.Nested;

import static org.codefx.demo.java_after_eight.article.Utils.removeOuterQuotationMarks;

class UtilsTests {

	@Nested
	class QuotationTests implements TextParserTests.QuotationTests {

		@Override
		public String parseCreateExtract(String text) {
			return removeOuterQuotationMarks(text);
		}

	}

}
