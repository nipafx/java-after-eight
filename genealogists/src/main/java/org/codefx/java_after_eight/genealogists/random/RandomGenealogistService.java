package org.codefx.java_after_eight.genealogists.random;

import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.GenealogistService;
import org.codefx.java_after_eight.post.Post;

import java.util.Collection;
import java.util.random.RandomGenerator;

public class RandomGenealogistService implements GenealogistService {

	@Override
	public Genealogist procure(Collection<Post> posts) {
		var random = RandomGenerator.getDefault();
		// for something fancier:
//		var random = RandomGeneratorFactory.all()
//				.filter(RandomGeneratorFactory::isJumpable)
//				.filter(factory -> factory.stateBits() > 64)
//				.map(RandomGeneratorFactory::create)
//				.findAny()
//				.orElseThrow();
		return new RandomGenealogist(random);
	}

}