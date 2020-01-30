package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.article.Article;

import java.util.Collection;

/**
 * Used as a service to create {@link Genealogist}s - must have a public parameterless constructor.
 */
public interface GenealogistService {

	Genealogist procure(Collection<Article> articles);

}
