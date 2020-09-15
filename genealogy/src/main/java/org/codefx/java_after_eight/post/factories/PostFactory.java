package org.codefx.java_after_eight.post.factories;

import org.codefx.java_after_eight.Utils;
import org.codefx.java_after_eight.post.Content;

import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

final class PostFactory {

	public static final String DATE = "date";
	public static final String DESCRIPTION = "description";
	public static final String REPOSITORY = "repo";
	public static final String SLIDES = "slides";
	public static final String SLUG = "slug";
	public static final String TAGS = "tags";
	public static final String TITLE = "title";
	public static final String VIDEO = "videoSlug";

	private static final String FRONT_MATTER_SEPARATOR = "---";

	private PostFactory() {
		// private constructor to prevent accidental instantiation of utility class
	}

	public static RawPost readPost(Path file) {
		try {
			List<String> eagerLines = Utils.uncheckedFilesReadAllLines(file);
			return readPost(eagerLines);
		} catch (RuntimeException ex) {
			throw new RuntimeException("Creating article failed: " + file, ex);
		}
	}

	public static RawPost readPost(List<String> fileLines) {
		RawFrontMatter frontMatter = extractFrontMatter(fileLines);
		Content content = () -> extractContent(fileLines);
		return new RawPost(frontMatter, content);
	}

	private static RawFrontMatter extractFrontMatter(List<String> fileLines) {
		Map<String, String> frontMatter = readFrontMatter(fileLines)
				.filter(line -> !line.startsWith("#"))
				.map(PostFactory::keyValuePairFrom)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		return new RawFrontMatter(frontMatter);
	}

	private static Stream<String> readFrontMatter(List<String> markdownFile) {
		return markdownFile.stream()
				.dropWhile(line -> !line.trim().equals(FRONT_MATTER_SEPARATOR))
				.skip(1)
				.takeWhile(line -> !line.trim().equals(FRONT_MATTER_SEPARATOR));
	}

	private static Map.Entry<String, String> keyValuePairFrom(String line) {
		String[] pair = line.split(":", 2);
		if (pair.length < 2)
			throw new IllegalArgumentException("Line doesn't seem to be a key/value pair (no colon): " + line);
		String key = pair[0].trim();
		if (key.isEmpty())
			throw new IllegalArgumentException("Line \"" + line + "\" has no key.");

		String value = pair[1].trim();
		return new AbstractMap.SimpleImmutableEntry<>(key, value);
	}

	private static Stream<String> extractContent(List<String> markdownFile) {
		return markdownFile.stream()
				.dropWhile(line -> !line.trim().equals(FRONT_MATTER_SEPARATOR))
				.skip(1)
				.dropWhile(line -> !line.trim().equals(FRONT_MATTER_SEPARATOR))
				.skip(1);
	}

}
