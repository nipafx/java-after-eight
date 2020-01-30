package org.codefx.java_after_eight.article;

import java.io.IOException;
import java.util.stream.Stream;

@FunctionalInterface
public interface Content {

	Stream<String> get() throws IOException;

}
