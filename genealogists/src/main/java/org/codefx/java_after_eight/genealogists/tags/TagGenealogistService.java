package org.codefx.java_after_eight.genealogists.tags;

import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.GenealogistService;

import java.util.Collection;

public class TagGenealogistService implements GenealogistService {

	@Override
	public Genealogist procure(Collection<Post> posts) {
		return new TagGenealogist();
	}

}
