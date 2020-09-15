package org.codefx.java_after_eight.genealogists.repo;

import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.GenealogistService;
import org.codefx.java_after_eight.post.Post;

import java.util.Collection;

public class RepoGenealogistService implements GenealogistService {

	@Override
	public Genealogist procure(Collection<Post> posts) {
		return new RepoGenealogist();
	}

}
