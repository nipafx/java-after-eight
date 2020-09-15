package org.codefx.java_after_eight.post;

import java.time.LocalDate;
import java.util.Set;

public interface Post {

	Title title();

	Set<Tag> tags();

	LocalDate date();

	Description description();

	Slug slug();

}
