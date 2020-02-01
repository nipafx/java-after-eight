import org.codefx.java_after_eight.genealogist.GenealogistService;

module org.codefx.java_after_eight.genealogy {
	requires java.management;

	exports org.codefx.java_after_eight.post;
	exports org.codefx.java_after_eight.genealogist;

	uses GenealogistService;
}