package org.codefx.java_after_eight.article;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextParserTests {

	public interface QuotationTests {

		String parseCreateExtract(String text);

		@Test
		default void createFromStringWithoutQuotationMarks_noChange() {
			String text = "A cool blog post";
			String expected = text;

			String actual = parseCreateExtract(text);

			assertThat(actual).isEqualTo(expected);
		}

		@Test
		default void createFromStringWithQuotationMarks_quotationMarksRemoved() {
			String text = "\"A cool blog post\"";
			String expected = "A cool blog post";

			String actual = parseCreateExtract(text);

			assertThat(actual).isEqualTo(expected);
		}

		@Test
		default void createFromStringWithInnerQuotationMarks_onlyOuterQuotationMarksRemoved() {
			String text = "\"\"A cool blog post\" he said\"";
			String expected = "\"A cool blog post\" he said";

			String actual = parseCreateExtract(text);

			assertThat(actual).isEqualTo(expected);
		}

	}

}
