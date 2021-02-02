package org.codefx.java_after_eight;

import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.GenealogistService;
import org.codefx.java_after_eight.genealogy.Genealogy;
import org.codefx.java_after_eight.genealogy.Weights;
import org.codefx.java_after_eight.post.Post;
import org.codefx.java_after_eight.post.factories.ArticleFactory;
import org.codefx.java_after_eight.post.factories.TalkFactory;
import org.codefx.java_after_eight.post.factories.VideoFactory;
import org.codefx.java_after_eight.recommendation.Recommendation;
import org.codefx.java_after_eight.recommendation.Recommender;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.codefx.java_after_eight.Utils.concat;

public class Main {

	public static void main(String[] args) {
		System.out.println(ProcessDetails.details());

		var config = Config.create(args).join();
		var genealogy = createGenealogy(config.articleFolder(), config.talkFolder(), config.videoFolder());
		var recommender = new Recommender();

		var relations = genealogy.inferRelations();
		var recommendations = recommender.recommend(relations, 3);
		var recommendationsAsJson = recommendationsToJson(recommendations);

		config.outputFile().ifPresentOrElse(
				outputFile -> Utils.uncheckedFilesWrite(outputFile, recommendationsAsJson),
				() -> System.out.println(recommendationsAsJson));
	}

	private static Genealogy createGenealogy(Path articleFolder, Path talkFolder, Path videoFolder) {
		List<Post> posts = concat(
				markdownFilesIn(articleFolder).<Post>map(ArticleFactory::createArticle),
				markdownFilesIn(talkFolder).map(TalkFactory::createTalk),
				markdownFilesIn(videoFolder).map(VideoFactory::createVideo)
		).toList();
		Collection<Genealogist> genealogists = getGenealogists(posts);
		return new Genealogy(posts, genealogists, Weights.allEqual());
	}

	private static Stream<Path> markdownFilesIn(Path folder) {
		return Utils.uncheckedFilesList(folder)
				.filter(Files::isRegularFile)
				.filter(file -> file.toString().endsWith(".md"));
	}

	private static Collection<Genealogist> getGenealogists(Collection<Post> posts) {
		var genealogists = ServiceLoader
				.load(GenealogistService.class).stream()
				.map(ServiceLoader.Provider::get)
				.map(service -> service.procure(posts))
				.toList();
		if (genealogists.isEmpty())
			throw new IllegalArgumentException("No genealogists found.");
		return genealogists;
	}

	private static String recommendationsToJson(Stream<Recommendation> recommendations) {
		var frame = """
				[
				$RECOMMENDATIONS
				]
				""";
		var recommendation = """
					{
						"title": "$TITLE",
						"recommendations": [
				$RECOMMENDED_POSTS
						]
					}
				""";
		var recommendedPost = """
				\t\t\t{ "title": "$TITLE" }""";

		var recs = recommendations
				.map(rec -> {
					String posts = rec
							.recommendedPosts().stream()
							.map(recArt -> recArt.title().text())
							.map(recTitle -> recommendedPost.replace("$TITLE", recTitle))
							.collect(joining(",\n"));
					return recommendation
							.replace("$TITLE", rec.post().title().text())
							.replace("$RECOMMENDED_POSTS", posts);
				})
				.collect(joining(",\n"));
		return frame.replace("$RECOMMENDATIONS", recs);
	}

}
