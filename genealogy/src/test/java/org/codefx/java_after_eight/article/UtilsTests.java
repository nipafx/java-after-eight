package org.codefx.java_after_eight.article;

import org.junit.jupiter.api.Nested;

class UtilsTests {

	@Nested
	class QuotationTests implements TextParserTests.QuotationTests {

		@Override
		public String parseCreateExtract(String text) {
			return Utils.removeOuterQuotationMarks(text);
		}

	}

}
