package org.codefx.java_after_eight.post.factories;

import org.codefx.java_after_eight.post.Content;

class RawPost {

	private final RawFrontMatter frontMatter;
	private final Content content;

	RawPost(RawFrontMatter frontMatter, Content content) {
		this.frontMatter = frontMatter;
		this.content = content;
	}

	public RawFrontMatter frontMatter() {
		return frontMatter;
	}

	public Content content() {
		return content;
	}

}
