package org.codefx.java_after_eight.genealogists.repo;

import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.RelationType;
import org.codefx.java_after_eight.genealogist.TypedRelation;
import org.codefx.java_after_eight.post.Article;
import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.post.Repository;
import org.codefx.java_after_eight.post.Video;

import java.util.Objects;
import java.util.Optional;

public class RepoGenealogist implements Genealogist {

	private static final RelationType TYPE = new RelationType("repo");

	@Override
	public TypedRelation infer(Post post1, Post post2) {
		long score = determineScore(post1, post2);
		return new TypedRelation(post1, post2, TYPE, score);
	}

	private long determineScore(Post post1, Post post2) {
		var repo1 = getRepository(post1);
		var repo2 = getRepository(post2);

		if (repo1.isPresent() != repo2.isPresent())
			return 0;
		// at this point, either both are empty or both are non-empty
		if (repo1.isEmpty())
			return 20;
		return Objects.equals(repo1, repo2) ? 100 : 50;
	}

	private Optional<Repository> getRepository(Post post) {
		return switch (post) {
			case Article(var t, var ts, var d, var desc, var s, var repository, var c) -> repository;
			case Video(var t, var ts, var d, var desc, var s, var v, var repository) -> repository;
			default -> Optional.empty();
		};
	}

}
