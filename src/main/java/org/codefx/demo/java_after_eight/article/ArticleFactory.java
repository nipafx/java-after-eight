package org.codefx.demo.java_after_eight.article;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public final class ArticleFactory {

	private static final String TITLE = "title";
	private static final String TAGS = "tags";
	public static final String DATE = "date";
	private static final String DESCRIPTION = "description";
	private static final String SLUG = "slug";
	public static final String FRONT_MATTER_SEPARATOR = "---";

	private ArticleFactory() {
		// private constructor to prevent accidental instantiation of utility class
	}

	public static Article createArticle(String markdownFile) {
		// REFACTOR 11: String::lines
		Stream<String> lines = Stream.of(markdownFile.split("\n"));
		return createArticle(extractFrontMatter(lines));
	}

	private static List<String> extractFrontMatter(Stream<String> markdownFile) {
		// REFACTOR 9: Stream::dropWhile, Stream::takeWhile
		List<String> frontMatter = new ArrayList<>();
		boolean frontMatterStarted = false;
		for (String line : markdownFile.collect(toList())) {
			if (line.trim().equals(FRONT_MATTER_SEPARATOR)) {
				if (frontMatterStarted)
					return frontMatter;
				else
					frontMatterStarted = true;
			} else if (frontMatterStarted)
				frontMatter.add(line);
		}
		return frontMatter;
	}

	public static Article createArticle(List<String> frontMatter) {
		Map<String, String> entries = frontMatter.stream()
				.map(ArticleFactory::keyValuePairFrom)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		return new Article(
				Title.from(entries.get(TITLE)),
				Tag.from(entries.get(TAGS)),
				LocalDate.parse(entries.get(DATE)),
				Description.from(entries.get(DESCRIPTION)),
				Slug.from(entries.get(SLUG))
		);
	}

	private static Map.Entry<String, String> keyValuePairFrom(String line) {
		String[] pair = line.split(":");
		if (pair.length != 2)
			throw new IllegalArgumentException("Line \"" + line + "\" doesn't seem to be a key/value pair.");
		Map.Entry<String, String> keyValuePair = new AbstractMap.SimpleImmutableEntry<>(pair[0].trim().toLowerCase(), pair[1].trim());
		if (keyValuePair.getKey().isEmpty())
			throw new IllegalArgumentException("Line \"" + line + "\" has no key.");
		return keyValuePair;
	}

}
