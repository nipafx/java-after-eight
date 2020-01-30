package org.codefx.java_after_eight.article;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlugTests {

	@Test
	void emptyText_exception() {
		assertThatThrownBy(() -> Slug.from("")).isInstanceOf(IllegalArgumentException.class);
	}

}
