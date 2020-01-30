package org.codefx.java_after_eight.genealogists.silly;

import org.codefx.java_after_eight.article.Article;
import org.codefx.java_after_eight.genealogy.Genealogist;
import org.codefx.java_after_eight.genealogy.GenealogistService;

import java.util.Collection;

public class SillyGenealogistService implements GenealogistService {

	@Override
	public Genealogist procure(Collection<Article> articles) {
		return new SillyGenealogist();
	}

}
