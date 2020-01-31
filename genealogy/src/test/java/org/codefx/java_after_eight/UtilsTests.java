package org.codefx.java_after_eight;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.codefx.java_after_eight.Utils.collectEqualElement;

class UtilsTests {

	@Nested
	class QuotationTests implements TextParserTests.QuotationTests {

		@Override
		public String parseCreateExtract(String text) {
			return Utils.removeOuterQuotationMarks(text);
		}

	}

	@Nested
	class CollectEqualElement {

		@Test
		void emptyStream_emptyOptional() {
			Optional<Object> element = Stream
					.of()
					.collect(collectEqualElement());

			assertThat(element).isEmpty();
		}

		@Test
		void singleElementStream_optionalWithThatElement() {
			Optional<String> element = Stream
					.of("element")
					.collect(collectEqualElement());

			assertThat(element).contains("element");
		}

		@Test
		void equalElementStream_optionalWithThatElement() {
			Optional<String> element = Stream
					.of("element", "element", "element")
					.collect(collectEqualElement());

			assertThat(element).contains("element");
		}

		@Test
		void nonEqualElementStream_throwsException() {
			Stream<String> stream = Stream.of("element", "other element");

			assertThatThrownBy(() -> stream.collect(collectEqualElement()))
					.isInstanceOf(IllegalArgumentException.class);
		}

	}

}
