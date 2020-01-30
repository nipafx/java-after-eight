package org.codefx.java_after_eight.article;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TagTests {

	@Test
	void emptyElementArray_emptyTag() {
		String tagsText = "[ ]";
		String[] expectedTags = { };

		Set<Tag> tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void singleElementArray_singleTag() {
		String tagsText = "[$TAG]";
		String[] expectedTags = { "$TAG" };

		Set<Tag> tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArray_multipleTags() {
		String tagsText = "[$TAG,$TOG,$TUG]";
		String[] expectedTags = { "$TAG", "$TOG", "$TUG" };

		Set<Tag> tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArrayWithSpaces_multipleTagsWithoutSpaces() {
		String tagsText = "[$TAG ,  $TOG , $TUG  ]";
		String[] expectedTags = { "$TAG", "$TOG", "$TUG" };

		Set<Tag> tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArrayWithJustSpacesTag_emptyTagIsIgnored() {
		String tagsText = "[$TAG ,  , $TUG  ]";
		String[] expectedTags = { "$TAG", "$TUG" };

		Set<Tag> tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArrayWithEmptyTag_emptyTagIsIgnored() {
		String tagsText = "[$TAG ,, $TUG  ]";
		String[] expectedTags = { "$TAG", "$TUG" };

		Set<Tag> tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArrayDuplicateTags_duplicateTagIsIgnored() {
		String tagsText = "[$TAG, $TAG]";
		String[] expectedTags = { "$TAG" };

		Set<Tag> tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

}
