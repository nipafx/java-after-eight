package org.codefx.java_after_eight.post;

import org.codefx.java_after_eight.post.Slug;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlugTests {

	@Test
	void emptyText_exception() {
		assertThatThrownBy(() -> Slug.from("")).isInstanceOf(IllegalArgumentException.class);
	}

}
