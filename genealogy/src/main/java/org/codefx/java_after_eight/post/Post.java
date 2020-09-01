package org.codefx.java_after_eight.post;

import java.time.LocalDate;
import java.util.stream.Stream;

public interface Post {

	Title title();

	Stream<Tag> tags();

	LocalDate date();

	Description description();

	Slug slug();

}
