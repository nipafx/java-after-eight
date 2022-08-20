import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.GenealogistService;
import org.codefx.java_after_eight.post.Post;

import java.util.Collection;
import java.util.List;

class Demo {

	public static void main(String[] args) {
		// @start region="creation"
		Collection<Post> posts = List.of();
		GenealogistService service = new SpecificGenealogistService();
		Genealogist genealogist = service.procure(posts);
		// @end
	}

	private static class SpecificGenealogistService implements GenealogistService {

		@Override
		public Genealogist procure(Collection<Post> posts) {
			throw new UnsupportedOperationException();
		}

	}

}