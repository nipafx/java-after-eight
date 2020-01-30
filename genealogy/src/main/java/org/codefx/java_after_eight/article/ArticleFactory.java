package org.codefx.java_after_eight.article;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
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

	public static Article createArticle(Path file) throws UncheckedIOException {
		try {
			List<String> eagerLines = Files.readAllLines(file);
			List<String> frontMatter = extractFrontMatter(eagerLines);
			Content content = () -> {
				List<String> lazyLines = Files.lines(file).collect(toList());
				return extractContent(lazyLines).stream();
			};
			return createArticle(frontMatter, content);
		} catch (IOException ex) {
			throw new UncheckedIOException("Creating article failed: " + file, ex);
		} catch (RuntimeException ex) {
			throw new RuntimeException("Creating article failed: " + file, ex);
		}
	}

	public static Article createArticle(List<String> fileLines) {
		List<String> frontMatter = extractFrontMatter(fileLines);
		Content content = () -> extractContent(fileLines).stream();
		return createArticle(frontMatter, content);
	}

	private static List<String> extractFrontMatter(List<String> markdownFile) {
		// REFACTOR 9: Stream::dropWhile, Stream::takeWhile
		List<String> frontMatter = new ArrayList<>();
		boolean frontMatterStarted = false;
		for (String line : markdownFile) {
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

	private static List<String> extractContent(List<String> markdownFile) {
		// REFACTOR 9: Stream::dropWhile, Stream::takeWhile (also drop leading empty lines)
		List<String> content = new ArrayList<>();
		boolean frontMatterStarted = false;
		boolean contentStarted = false;
		for (String line : markdownFile) {
			if (line.trim().equals(FRONT_MATTER_SEPARATOR)) {
				if (frontMatterStarted)
					contentStarted = true;
				else
					frontMatterStarted = true;
			} else if (contentStarted)
				content.add(line);
		}
		return content;
	}

	public static Article createArticle(List<String> frontMatter, Content content) {
		// REFACTOR 14: use a record instead of abusing Map.Entry
		Map<String, String> entries = frontMatter.stream()
				.map(ArticleFactory::keyValuePairFrom)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		return new Article(
				Title.from(entries.get(TITLE)),
				Tag.from(entries.get(TAGS)),
				LocalDate.parse(entries.get(DATE)),
				Description.from(entries.get(DESCRIPTION)),
				Slug.from(entries.get(SLUG)),
				content);
	}

	private static Map.Entry<String, String> keyValuePairFrom(String line) {
		String[] pair = line.split(":");
		if (pair.length < 2)
			throw new IllegalArgumentException("Line doesn't seem to be a key/value pair (no colon): " + line);
		String key = pair[0].trim().toLowerCase();
		String value = Stream.of(pair).skip(1).collect(joining(":")).trim();
		Map.Entry<String, String> keyValuePair = new AbstractMap.SimpleImmutableEntry<>(key, value);
		if (keyValuePair.getKey().isEmpty())
			throw new IllegalArgumentException("Line \"" + line + "\" has no key.");
		return keyValuePair;
	}

}
