package org.codefx.java_after_eight.post;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlugTests {

	@Test
	void emptyText_exception() {
		assertThatThrownBy(() -> new Slug("")).isInstanceOf(IllegalArgumentException.class);
	}

}
