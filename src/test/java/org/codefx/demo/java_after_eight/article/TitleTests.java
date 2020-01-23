package org.codefx.demo.java_after_eight.article;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TitleTests {

	@Test
	void createFromStringWithoutQuotationMarks_noChange() {
		String text = "A cool blog post";
		String expected = text;

		Title title = Title.from(text);

		assertThat(title.text()).isEqualTo(expected);
	}

	@Test
	void createFromStringWithQuotationMarks_quotationMarksRemoved() {
		String text = "\"A cool blog post\"";
		String expected = "A cool blog post";

		Title title = Title.from(text);

		assertThat(title.text()).isEqualTo(expected);
	}

	@Test
	void createFromStringWithInnerQuotationMarks_onlyOuterQuotationMarksRemoved() {
		String text = "\"\"A cool blog post\" he said\"";
		String expected = "\"A cool blog post\" he said";

		Title title = Title.from(text);

		assertThat(title.text()).isEqualTo(expected);
	}

}
