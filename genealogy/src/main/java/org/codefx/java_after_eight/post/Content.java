package org.codefx.java_after_eight.post;

import java.util.function.Supplier;
import java.util.stream.Stream;

@FunctionalInterface
public interface Content extends Supplier<Stream<String>> {

}
