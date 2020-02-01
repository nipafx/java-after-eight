package org.codefx.java_after_eight;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

public final class Utils {

	private Utils() {
		// private constructor to prevent accidental instantiation of utility class
	}

	public static String removeOuterQuotationMarks(String string) {
		return string.replaceAll("^\"|\"$", "");
	}

	public static Stream<Path> uncheckedFilesList(Path dir) {
		try {
			return Files.list(dir);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	public static <T> void uncheckedFilesWrite(Path path, String content) {
		try {
			Files.write(path, List.of(content), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	public static List<String> uncheckedFilesReadAllLines(Path file) {
		try {
			return Files.readAllLines(file, StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	public static Stream<String> uncheckedFilesLines(Path file) {
		try {
			return Files.lines(file, StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public static <ELEMENT> Stream<ELEMENT> concat(Stream<? extends ELEMENT>... streams) {
		return Stream.of(streams).flatMap(s -> s);
	}

	public static <ELEMENT> Collector<ELEMENT, ?, Optional<ELEMENT>> collectEqualElement() {
		return collectEqualElement(Objects::equals);
	}

	public static <ELEMENT> Collector<ELEMENT, ?, Optional<ELEMENT>> collectEqualElement(
			BiPredicate<ELEMENT, ELEMENT> equals) {
		return Collector.of(
				AtomicReference::new,
				(AtomicReference<ELEMENT> left, ELEMENT right) -> {
					if (left.get() != null && !equals.test(left.get(), right))
						throw new IllegalArgumentException(
								"Unequal elements in stream: %s vs %s".formatted(left.get(), right));
					left.set(right);
				},
				(AtomicReference<ELEMENT> left, AtomicReference<ELEMENT> right) -> {
					if (left.get() != null && right.get() != null && !equals.test(left.get(), right.get()))
						throw new IllegalArgumentException(
								"Unequal elements in stream: %s vs %s".formatted(left.get(), right.get()));
					return left.get() != null ? left : right;
				},
				reference -> Optional.ofNullable(reference.get())
		);
	}

}
