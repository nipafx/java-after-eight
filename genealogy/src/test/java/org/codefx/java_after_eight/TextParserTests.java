package org.codefx.java_after_eight;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TextParserTests {

	public interface QuotationTests {

		String parseCreateExtract(String text);

		@Test
		default void createFromStringWithoutQuotationMarks_noChange() {
			var text = "A cool blog post";
			var expected = text;

			var actual = parseCreateExtract(text);

			assertThat(actual).isEqualTo(expected);
		}

		@Test
		default void createFromStringWithQuotationMarks_quotationMarksRemoved() {
			var text = "\"A cool blog post\"";
			var expected = "A cool blog post";

			var actual = parseCreateExtract(text);

			assertThat(actual).isEqualTo(expected);
		}

		@Test
		default void createFromStringWithInnerQuotationMarks_onlyOuterQuotationMarksRemoved() {
			var text = "\"\"A cool blog post\" he said\"";
			var expected = "\"A cool blog post\" he said";

			var actual = parseCreateExtract(text);

			assertThat(actual).isEqualTo(expected);
		}

	}

}
