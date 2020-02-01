import org.codefx.java_after_eight.genealogist.GenealogistService;

module org.codefx.java_after_eight.genealogists {
	requires org.codefx.java_after_eight.genealogy;

	provides GenealogistService
		with org.codefx.java_after_eight.genealogists.tags.TagGenealogistService;
}