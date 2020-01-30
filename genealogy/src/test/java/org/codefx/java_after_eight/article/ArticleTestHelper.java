package org.codefx.java_after_eight.article;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArticleTestHelper {

	public static Article createWithSlug(String slug) {
		Slug properSlug = when(mock(Slug.class).value()).thenReturn("a").getMock();
		return when(mock(Article.class).slug()).thenReturn(properSlug).getMock();
	}

}
