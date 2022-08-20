package org.codefx.java_after_eight.genealogist;

import org.codefx.java_after_eight.post.Post;

import java.util.Collection;

/**
 * Used as a service to create {@link Genealogist}s - must have a public parameterless constructor.
 *
 * <p>Intended use:
 * {@snippet file="GenealogistServiceDemo.java" region="creation"}
 */
public interface GenealogistService {

	Genealogist procure(Collection<Post> posts);

}
