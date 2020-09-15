package org.codefx.java_after_eight.post;

import org.codefx.java_after_eight.post.article.Article;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

public class PostTestHelper {

	public static Post createWithSlug(String slug) {
		return new Article(
				Title.from("Title"),
				Tag.from("[Tag]"),
				LocalDate.now(),
				Description.from("description"),
				Slug.from(slug),
				Optional.empty(),
				() -> Stream.of(""));
	}

}
