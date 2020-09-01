package org.codefx.java_after_eight.genealogist;

import org.codefx.java_after_eight.post.Post;

public interface Genealogist {

	TypedRelation infer(Post post1, Post post2);

}
