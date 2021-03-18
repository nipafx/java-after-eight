package org.codefx.java_after_eight.genealogists.tags;

import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.RelationType;
import org.codefx.java_after_eight.genealogist.TypedRelation;
import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.post.Tag;

import java.util.Set;

import static java.lang.Math.round;
import static java.util.stream.Collectors.toSet;

public class TagGenealogist implements Genealogist {

	private static final RelationType TYPE = new RelationType("tag");

	@Override
	public TypedRelation infer(Post post1, Post post2) {
		Set<Tag> post2Tags = post2.tags().collect(toSet());
		long numberOfSharedTags = post1
				.tags()
				.filter(post2Tags::contains)
				.count();
		long numberOfPost1Tags = post1.tags().count();
		long score = round((100.0 * 2 * numberOfSharedTags) / (numberOfPost1Tags + post2Tags.size()));
		return new TypedRelation(post1, post2, TYPE, score);
	}

}
