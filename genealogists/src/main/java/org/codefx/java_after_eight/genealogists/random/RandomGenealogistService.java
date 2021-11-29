package org.codefx.java_after_eight.genealogists.random;

import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.GenealogistService;
import org.codefx.java_after_eight.post.Post;

import java.util.Collection;
import java.util.Random;

public class RandomGenealogistService implements GenealogistService {

	@Override
	public Genealogist procure(Collection<Post> posts) {
		Random random = new Random();
		return new RandomGenealogist(random);
	}

}