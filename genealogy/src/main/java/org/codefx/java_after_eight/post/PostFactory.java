package org.codefx.java_after_eight.post;

import org.codefx.java_after_eight.Utils;

import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

public final class PostFactory {

	public static final String TITLE = "title";
	public static final String TAGS = "tags";
	public static final String DATE = "date";
	public static final String DESCRIPTION = "description";
	public static final String SLUG = "slug";
	public static final String VIDEO_URL = "video";

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
		Content content = () -> extractContent(fileLines).stream();
		return new RawPost(frontMatter, content);
	}

	static RawFrontMatter extractFrontMatter(List<String> fileLines) {
		List<String> frontMatterLines = readFrontMatter(fileLines);
		Map<String, String> frontMatter = frontMatterLines.stream()
				.map(PostFactory::keyValuePairFrom)
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		return new RawFrontMatter(frontMatter);
	}

	private static List<String> readFrontMatter(List<String> markdownFile) {
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

	private static Map.Entry<String, String> keyValuePairFrom(String line) {
		String[] pair = line.split(":", 2);
		if (pair.length < 2)
			throw new IllegalArgumentException("Line doesn't seem to be a key/value pair (no colon): " + line);
		String key = pair[0].trim().toLowerCase();
		if (key.isEmpty())
			throw new IllegalArgumentException("Line \"" + line + "\" has no key.");

		String value = pair[1].trim();
		return new AbstractMap.SimpleImmutableEntry<>(key, value);
	}

	private static List<String> extractContent(List<String> markdownFile) {
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

	public static class RawPost {

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

	public static class RawFrontMatter {

		private final Map<String, String> lines;

		RawFrontMatter(Map<String, String> lines) {
			this.lines = lines;
		}

		public Optional<String> valueOf(String key) {
			return Optional.ofNullable(lines.get(key));
		}

		public String requiredValueOf(String key) {
			return valueOf(key).orElseThrow(
					() -> new IllegalArgumentException("Required key '" + key + "' not present in front matter."));
		}

	}

}
