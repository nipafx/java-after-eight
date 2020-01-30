package org.codefx.java_after_eight;

import org.codefx.java_after_eight.article.Article;
import org.codefx.java_after_eight.article.ArticleFactory;
import org.codefx.java_after_eight.genealogy.Genealogist;
import org.codefx.java_after_eight.genealogy.GenealogistService;
import org.codefx.java_after_eight.genealogy.Genealogy;
import org.codefx.java_after_eight.genealogy.Relation;
import org.codefx.java_after_eight.genealogy.Weights;
import org.codefx.java_after_eight.recommendation.Recommendation;
import org.codefx.java_after_eight.recommendation.Recommender;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static java.lang.Math.round;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class Main {

	// text similarities: https://medium.com/@adriensieg/text-similarities-da019229c894
	// Stanford CoreNLP: https://github.com/stanfordnlp/CoreNLP

	public static void main(String[] args) throws IOException {
		Path articleFolder = getArticleFolder(args);
		Genealogy genealogy = createGenealogy(articleFolder);
		Recommender recommender = new Recommender();

		Stream<Relation> relations = genealogy.inferRelations();
		Stream<Recommendation> recommendations = recommender.recommend(relations, 3);
		String recommendationsAsJson = recommendationsToJson(recommendations);

		if (args.length > 1)
			Files.write(Paths.get(args[1]), Arrays.asList(recommendationsAsJson.split("\n")));
		else
			System.out.println(recommendationsAsJson);
	}

	private static Path getArticleFolder(String[] args) {
		if (args.length == 0)
			throw new IllegalArgumentException("Please specific input path as first parameter.");

		Path articleFolder = Paths.get(args[0]);
		if (!Files.exists(articleFolder))
			throw new IllegalArgumentException("Path doesn't exist: " + articleFolder);
		if (!Files.isDirectory(articleFolder))
			throw new IllegalArgumentException("Path is no directory: " + articleFolder);
		return articleFolder;
	}

	private static Genealogy createGenealogy(Path articleFolder) throws IOException {
		List<Article> articles = Files.list(articleFolder)
				.filter(Files::isRegularFile)
				.filter(file -> file.toString().endsWith(".md"))
				.map(ArticleFactory::createArticle)
				.collect(toList());
		Collection<Genealogist> genealogists = getGenealogists(articles);
		return new Genealogy(articles, genealogists, Weights.allEqual());
	}

	private static Collection<Genealogist> getGenealogists(Collection<Article> articles) {
		List<Genealogist> genealogists = new ArrayList<>();
		ServiceLoader
				.load(GenealogistService.class)
				.forEach(service -> genealogists.add(service.procure(articles)));
		if (genealogists.isEmpty())
			throw new IllegalArgumentException("No genealogists found.");
		return genealogists;
	}

	private static String recommendationsToJson(Stream<Recommendation> recommendations) {
		String frame = "[\n$RECOMMENDATIONS\n]";
		String recommendation = "" +
				"\t{" +
				"\n\t\t\"title\": \"$TITLE\",\n" +
				"\t\t\"recommendations\": [\n" +
				"$RECOMMENDED_ARTICLES\n" +
				"\t\t]\n" +
				"\t}";
		String recommendedArticle = "" +
				"\t\t\t{ \"title\": \"$TITLE\" }";

		String recs = recommendations
				.map(rec -> {
					String articles = rec
							.recommendedArticles()
							.map(recArt -> recArt.title().text())
							.map(recTitle -> recommendedArticle.replace("$TITLE", recTitle))
							.collect(joining(",\n"));
					return recommendation
							.replace("$TITLE", rec.article().title().text())
							.replace("$RECOMMENDED_ARTICLES", articles);
				})
				.collect(joining(",\n"));
		return frame.replace("$RECOMMENDATIONS", recs);
	}

}
