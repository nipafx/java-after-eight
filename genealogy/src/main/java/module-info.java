import org.codefx.java_after_eight.genealogist.GenealogistService;

module org.codefx.java_after_eight.genealogy {
	requires jdk.incubator.concurrent;

	exports org.codefx.java_after_eight.post;
	exports org.codefx.java_after_eight.genealogist;

	uses GenealogistService;
}