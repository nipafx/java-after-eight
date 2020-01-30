package org.codefx.java_after_eight.article;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TagTests {

	@Test
	void emptyElementArray_emptyTag() {
		String tagsText = "[ ]";
		String[] expectedTags = { };

		List<Tag> tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void singleElementArray_singleTag() {
		String tagsText = "[$TAG]";
		String[] expectedTags = { "$TAG" };

		List<Tag> tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArray_multipleTags() {
		String tagsText = "[$TAG,$TOG,$TUG]";
		String[] expectedTags = { "$TAG", "$TOG", "$TUG" };

		List<Tag> tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArrayWithSpaces_multipleTagsWithoutSpaces() {
		String tagsText = "[$TAG ,  $TOG , $TUG  ]";
		String[] expectedTags = { "$TAG", "$TOG", "$TUG" };

		List<Tag> tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArrayWithEmptyTags_exception() {
		String tagsText = "[$TAG ,  , $TUG  ]";
		String[] expectedTags = { "$TAG", "$TUG" };

		List<Tag> tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

}
