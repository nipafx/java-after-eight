package org.codefx.java_after_eight.post;

import java.time.LocalDate;
import java.util.Set;

public sealed interface Post permits Article, Talk, Video {

	Title title();

	Set<Tag> tags();

	LocalDate date();

	Description description();

	Slug slug();

}
