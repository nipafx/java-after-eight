package org.codefx.java_after_eight.post;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

public class PostTestHelper {

	public static Post createWithSlug(String slug) {
		return new Article(
				new Title("Title"),
				Tag.from("[Tag]"),
				LocalDate.now(),
				new Description("description"),
				new Slug(slug),
				Optional.empty(),
				() -> Stream.of(""));
	}

}
