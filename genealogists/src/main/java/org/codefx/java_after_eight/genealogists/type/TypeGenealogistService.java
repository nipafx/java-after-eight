package org.codefx.java_after_eight.genealogists.type;

import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.GenealogistService;
import org.codefx.java_after_eight.post.Post;

import java.util.Collection;

public class TypeGenealogistService implements GenealogistService {

	@Override
	public Genealogist procure(Collection<Post> posts) {
		return new TypeGenealogist();
	}

}
