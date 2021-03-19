---
title: "JUnit Pioneer 1.0"
tags: [junit-5, testing]
date: 2020-10-07
slug: junit-pioneer-1-0-0
description: "Yesterday we released JUnit Pioneer 1.0 🥳 - here's a quick rundown of its features"
searchKeywords: "junit 5 extension"
featuredImage: junit-pioneer-spacecraft
repo: junit-pioneer
---

Let's try to write as few tests as possible while using [each extension](https://junit-pioneer.org/docs/):

```java
@DefaultLocale(language = "vi", country = "VN")
@DefaultTimeZone("Asia/Ho_Chi_Minh")
@ClearSystemProperty(key = "A")
@ClearSystemProperty(key = "B")
@SetSystemProperty(key = "C", value = "3")
@ClearEnvironmentVariable(key = "1")
@ClearEnvironmentVariable(key = "2")
@SetEnvironmentVariable(key = "3", value = "C")
class AllInOne {

	@CartesianProductTest(name = "word: {0} / number: {1}")
	@CartesianValueSource(strings = { "foo", "bar"})
	@CartesianValueSource(ints = { 0, 1, 3})
	@DisableIfDisplayName(matches = "word: bar / number: 3")
	void combination(String word, int number) {
		// ...
	}

	@ParameterizedTest
	@DoubleRangeSource(from = -1.0, to = -10, step = -0.1)
	@ReportEntry(
		key = "failed", value="for number {0}", when = ON_FAILURE)
	void steppingDown(double number) {
		// ...
	}

	@RetryingTest(3)
	@StdIo({"Hello", "World"})
	void failsOnlyOnFirstInvocation(StdOut out) {
		// ...
	}

}
```

There you go, all ten [Jupiter](junit-5-architecture-jupiter) extensions in just three tests 😁 and all but one annotation come from Pioneer.
Let me quickly go through each of them before telling you a bit more about the project.

## Jupiter Extensions

In order of appearance... (note that there's always more detail in the documentation)

### System Properties and Environment Variables

If your tests rely on specific values for system properties or environment variables or you want to verify that they correctly read them, `@ClearSystemProperty`, `@SetSystemProperty`, `@ClearEnvironmentVariable`, and `@ClearEnvironmentVariable` are there for you:

```java
@ClearSystemProperty(key = "A")
@ClearSystemProperty(key = "B")
@SetSystemProperty(key = "C", value = "3")
@ClearEnvironmentVariable(key = "1")
@ClearEnvironmentVariable(key = "2")
@SetEnvironmentVariable(key = "3", value = "C")
class AllInOne {

	// Before each test in this class starts,
	// the system properties "A" and "B" are removed
	// and "C" is set to "3".
	// Likewise, the environment variables "1" and "2"
	// are removed and "3" is set to "C".
	// After each test, they are restored.

}
```

Changing environment variables is an ugly business, though, because it requires reflection, so try to avoid it.

[⇝ Documentation on environment variables](https://junit-pioneer.org/docs/environment-variables/)  \
[⇝ Documentation on system properties](https://junit-pioneer.org/docs/system-properties/)

### Default Locales and Time Zones

`@DefaultLocale` and `@DefaultTimeZone` are straight forward - they set `Locale.getDefault()` and `Timezone.getDefault()` during the test:

```java
@DefaultLocale(language = "vi", country = "VN")
@DefaultTimeZone("Asia/Ho_Chi_Minh")
class AllInOne {

	// All tests in this class think they're in Vietnam
	// (judging by time zone and locale).

}
```

You can also define locales with IETF BCP 47 language tag strings (so _i-klingon_ and _xtg-x-cel-gaulish_ work) and both annotations work on class and method level where the one closest to the test defines the default value read by the test.

[⇝ Documentation on default locale and time zone](https://junit-pioneer.org/docs/default-locale-timezone/)

### Cartesian Product Tests

The `@CartesianProductTest` annotation lets you specify values for each parameter and then executes the test method once per combination:

```java
@CartesianProductTest(name = "word: {0} / number: {1}")
@CartesianValueSource(strings = { "foo", "bar"})
@CartesianValueSource(ints = { 0, 1, 3})
void combination(String word, int number) {
	// gets executed six times with arguments:
	//     "foo"/0, "foo"/1, "foo"/3, "bar"/0, "bar"/1, "bar"/3
}
```

For a test with a single `String` parameter you don't need `@CartesianValueSource` and if you prefer specifying value sets per factory method, you can do that too.

[⇝ Documentation in cartesian product tests](https://junit-pioneer.org/docs/cartesian-product/)

### Disable Based on DisplayName

It's not so easy to disable one out of a number of template-based tests (like the ones annotated with `@ParameterizedTest` or `@CartesianProductTest`).
`@DisableIfDisplayName` helps you with that by allowing you to specify a display name and then disabling each test that matches it:

```java
@ParameterizedTest(name = "run #{index} with [{arguments}]")
@ValueSource(strings = { "Hello", "JUnit" })
@DisableIfDisplayName(contains = "Hell")
void withValueSource(String word) {
	// the test for "Hello" is disabled
}

@CartesianProductTest(name = "word: {0} / number: {1}")
@CartesianValueSource(strings = { "foo", "bar"})
@CartesianValueSource(ints = { 0, 1, 3})
@DisableIfDisplayName(matches = "word: bar / number: 3")
void combination(String word, int number) {
	// the test for arguments "bar"/3 is disabled
}
```

With the `contains` attribute, the extension checks whether the given string is a substring of the display name.
For more complex cases you can use `matches`, which is interpreted as a regular expression.

[⇝ Documentation on disabling by display name](https://junit-pioneer.org/docs/disable-if-display-name/)

### Range Sources

Jupiter's `@ParameterizedTest` brings us to Pioneer's range sources.
With `@ByteRangeSource`, `@ShortRangeSource`, `@IntRangeSource`, `@LongRangeSource`, `@FloatRangeSource`, and `@DoubleRangeSource` you can specify a range of values for a test method with a single parameter:

```java
@ParameterizedTest
@DoubleRangeSource(from = -1.0, to = -10, step = -0.1)
void steppingDown(double number) {
	// this parameterized test gets called with
	// -1.0, -1.1, -1.2, ... -9.9
}
```

[⇝ Documentation on range sources](https://junit-pioneer.org/docs/range-sources/)

### Publishing Report Entries

Jupiter can inject a [`TestReporter`](https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/TestReporter.html) that you can use to publish additional data about the current test run, which tools can then consume and display:

```java
@Test
void reportSingleValue(TestReporter testReporter) {
	testReporter.publishEntry("a status message");
	// ...
}
```

With Pioneer's `@ReportEntry`, you can do that declaratively:

```java
@Test
@ReportEntry("a status message")
void reportSingleValue() {
	// ...
}
```

As shown further above, you can optionally specify a key and under which condition (success, failure, aborted) a message is published.

[⇝ Documentation on reporting test entries](https://junit-pioneer.org/docs/report-entries/)

### Retrying Failing Tests

This is the extension the world has been waiting for: retrying tests until they pass!

```java
@RetryingTest(3)
void flakyTest() {
	// this test gets executed up to three times and
	// is only marked as failed if all executions fail
}
```

Ok, it's not quite that bad: It's repeated until it passes, but at most the specified number of times.
Each execution but the last is marked as aborted - the last execution is either successful or failed.

[⇝ Documentation on retrying tests](https://junit-pioneer.org/docs/retrying-test/)

### Standard Input and Output

If code under test needs to read from `System.in` or write to `System.out`, `@StdIo` has you covered:
It can replace `System.in` so the code can read the input you provide (without blocking of course) and it can wrap `System.out` to capture the output that the code created:

```java
@Test
@StdIo({"Hello", "World"})
void failsOnlyOnFirstInvocation(StdOut out) {
	// `System.in` reads the two specified Strings;
	// to verify what was written to `System.out`,
	// the test can check `out.capturedLines()`
}
```

You can also have `StdIn` injected and not all combinations of annotation attributes and parameters are valid.

[⇝ Documentation on standard input and output](https://junit-pioneer.org/docs/standard-input-output/)


## Thread Safety

All of the extensions are thread-safe, so you can use them in a fully parallelized test suite (in fact, that's how we run our tests).
That said, many change global state, so for them thread-safety really means preventing parallel execution.
So if each of your tests sets a default locale, they will effectively run sequentially.

The larger problem with this is that while each extension will force sequential execution of all tests that use it, there may still be other tests out there that rely on the same global state (maybe unwittingly) but don't use the extension.
To allow you to still execute your tests in parallel, Pioneer provides annotations that you can use to mark such tests.

Here's an example for default time zones:

```java
class TimeZoneTests {

	@Test
	@DefaultTimeZone("Asia/Ho_Chi_Minh")
	void checkHcmc() {
		// this test uses the extension
	}

	@Test
	@ReadsDefaultTimeZone
	void someTest() {
		// this test does not use the extension,
		// but reads the default time zone
		// and doesn't work if its arbitrary
		// (wait, then it should use the extension)
	}

	@Test
	@WritesDefaultTimeZone
	void anotherTest() {
		// this test does not use the extension,
		// but the code under tests writes
		// the default time zone
	}

}
```

While only one of the three tests actively uses the extension, the other two interact with the underlying global state because the code under test reads or writes it.
Without the additional annotations `@ReadsDefaultTimeZone` and `@WritesDefaultTimeZone`, this is bound to fail under threading (even without `@DefaultTimeZone`).
With the annotations, Jupiter does not execute these tests in parallel.

For more details on thread-safety, check [each extensions documentation](https://junit-pioneer.org/docs/).


## Getting Started With JUnit Pioneer

<contentimage slug="junit-pioneer-v1.0" options="sidebar"></contentimage>

If you're interested in JUnit Pioneer, give it a go.

Maven:

```xml
<dependency>
    <groupId>org.junit-pioneer</groupId>
    <artifactId>junit-pioneer</artifactId>
    <version>1.0.0<version>
    <scope>test</scope>
</dependency>
```

Gradle (Kotlin-style):

```kotlin
testImplementation("org.junit-pioneer:junit-pioneer:1.0.0")
```

If you have any problems, feature requests, or your own extension to share, feel free to [open an issue](https://github.com/junit-pioneer/junit-pioneer/issues/new/choose).
You can also always reach out to me [on Twitter](https://twitter.com/nipafx).

Happy testing!
