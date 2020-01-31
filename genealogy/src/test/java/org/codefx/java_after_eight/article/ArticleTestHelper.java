package org.codefx.java_after_eight.article;

import java.time.LocalDate;
import java.util.stream.Stream;

public class ArticleTestHelper {

	public static Article createWithSlug(String slug) {
		return new Article(
				Title.from("Title"),
				Tag.from("[Tag]"),
				LocalDate.now(),
				Description.from("description"),
				Slug.from(slug),
				() -> Stream.of(""));
	}

}
