---
title: "JUnit 5 Conditions: `@Enabled`, `@Disabled`, Customized"
tags: [junit-5, libraries, testing]
date: 2018-08-05
slug: junit-5-disabled-conditions
description: "A detailed look at JUnit 5's `@Disabled`, `@DisabledOnOs`, `@DisabledOnJre`, etc. and how to create custom conditions to flexibly disable test methods."
intro: "A detailed look at `@Disabled`, its conditional counterparts, and how to  create custom conditions that allow us to flexibly disable test methods."
searchKeywords: "conditions"
featuredImage: junit-5-conditions
repo: junit-5-demo
---

It is time to combine two topics that we've been exploring in the past: [conditions like `@DisabledOnOs` and `@DisabledOnJre`](junit-5-basics#disabling-tests) on the one hand and [extending JUnit 5 with custom behavior](junit-5-extension-model) on the other hand.
In that last post I left you with the promise to look at conditions, which allow us to define flexible criteria for (de)activating tests, just like the built-in `@Disabled...`/`@Enabled...` annotations.

I'll start by giving you a run-down of [Jupiter's](junit-5-architecture-jupiter#splitting-junit-5) conditions before having a quick look at their implementation to prepare you for the last part: creating custom conditions.
Conditions are a great way to get started with extensions because they are so easy to write and make a test suite so much more readable than checking the same condition in the test methods.

## Conditions In JUnit 5

Besides `@Disabled`, which unconditionally disables either a single test method or an entire test class, depending on where it is applied, Jupiter comes with four conditions.
They evaluate the operating system, the Java version, a system property, or an environment variable:

`@DisabledOnOs` / `@EnabledOnOs`:
Given either a single or multiple values of [the `OS` enum](https://junit.org/junit5/docs/current/api/org/junit/jupiter/api/condition/OS.html), tests can be disabled on selected operating systems.

`@DisabledOnJre` / `@EnabledOnJre`:
Given either a single or multiple values of [the `JRE` enum](https://junit.org/junit5/docs/current/api/org/junit/jupiter/api/condition/JRE.html), tests can be disabled when the suite runs on selected Java versions.

`@DisabledIfSystemProperty` / `@EnabledIfSystemProperty`:
These conditions have two attributes, `named` and `matches`.
The first names a specific system property (surprise!) and the second is a regular expression that is matched against the property's value.
If it matches, the test ist disabled or not (depending on which annotation you use).

`@DisabledIfEnvironmentVariable` / `@EnabledIfEnvironmentVariable`:
Work exactly like `@DisabledIfSystemProperty` and `@EnabledIfSystemProperty`, but check environment variables, not system properties.

These conditions always come in two variants, `@Disabled...` and `@Enabled...`, but it is important to understand that *enabled on X* really just means *disabled on everything but X*.
That becomes relevant when you use several conditions *of different kinds*: As soon as one of them disables a test, the test does not run, no matter what other `@Enabled...` conditions might have to say about that:

<pullquote>`@Enabled...(X)` doesn't "enable on X", rather it "disables on everything but X"</pullquote>

```java
@Test
@EnabledOnOs({ LINUX, SOLARIS }) // disabled on all but Linux, Solaris
@DisabledOnJre(JAVA_8)           // disabled on Java 8
@EnabledIfSystemProperty(        // disabled on all but 64bit OS
	named = "os.arch", matches = ".*64.*")
@EnabledIfEnvironmentVariable(   // disabled unless `ENV` is `ci`
	named = "ENV", matches = "ci")
void test() {
	// doesn't run on Linux with Java 10 because `@EnabledOnOs` doesn't
	// really _enable_ the test as much as _not disable_ it
}
```

You should only ever use one condition *of the same kind* because JUnit only evaluates the first it finds - all others are silently ignored.

### Disabling All Conditions

You will sometimes want to run disabled tests to find out whether they indeed break under the avoided circumstances.
Don't worry, you don't have to remove all `@Disabled...` annotations.
Instead [configure JUnit](https://junit.org/junit5/docs/current/user-guide/#running-tests-config-params) with the parameter `junit.jupiter.conditions.deactivate`.

The given value is interpreted as a [glob pattern](https://en.wikipedia.org/wiki/Glob_(programming)) against which the class name of each `ExecutionCondition` implementation (see below) is compared.
If they match, the condition will be ignored and the test hence be activated.
With `*` you can deactivate all conditions and hence run all tests.

One way to pass this parameter is as a system property with `shell§-Djunit.jupiter.conditions.deactivate=*`.

## Extension Points For Conditions

Remember what we said about [extension points](junit-5-extension-model#extension-points)?
No?
In short: There's a bunch of them and each relates to a specific interface.
Implementations of these interfaces can be handed to JUnit (for example, with with the `@ExtendWith` annotation) and it calls them at the appropriate time.

For conditions, there is [the interface `ExecutionCondition`](https://junit.org/junit5/docs/current/api/org/junit/jupiter/api/extension/ExecutionCondition.html):

```java
public interface ExecutionCondition extends Extension {

	/**
	 * Evaluate this condition for the supplied ExtensionContext.
	 *
	 * An enabled result indicates that the container or test should
	 * be executed; whereas, a disabled result indicates that the
	 * container or test should not be executed.
	 */
	ConditionEvaluationResult evaluate(ExtensionContext context);

}
```

And that's already pretty much it.
Any condition has to implement that interfaces and execute the required checks in its `evaluate` implementation.

## `@Disabled`

The easiest condition is one that is not even evaluated: We simply always disable the test if the annotation is present.
That's how `@Disabled` works:

```java
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
// Jupiter registers this extension itself,
// so @Disabled doesn't have to do it
// @ExtendWith(@DisabledCondition.class)
public @interface Disabled {

	String value() default "";

}
```

And the matching extension:

```java
class DisabledCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED =
		ConditionEvaluationResult.enabled("@Disabled is not present");

	/**
	 * Containers/tests are disabled if @Disabled is present
	 * on the test class or method.
	 */
	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(
			ExtensionContext context) {
		Optional<AnnotatedElement> element = context.getElement();
		Optional<Disabled> disabled = findAnnotation(
			element, Disabled.class);
		if (disabled.isPresent()) {
			String reason = disabled
				.map(Disabled::value)
				.filter(StringUtils::isNotBlank)
				.orElseGet(() -> element.get() + " is @Disabled");
			return ConditionEvaluationResult.disabled(reason);
		}
		return ENABLED;
	}

}
```

Easy as pie, right?
If it wouldn't exist already, our implementation of `@Disabled` would be almost exactly the same - the only difference is that we would have to put `@ExtendWith(@DisabledCondition.class)` on `@Disabled`.

Now let's look at something slightly less trivial.

## `@DisabledOnOs` And Other Conditions

The only difference between `@Disabled` and `@DisabledOnOs` (et al.) is that the latter check something else besides the presence of an annotation to determine whether a test is disabled:

```java
@Override
public ConditionEvaluationResult evaluateExecutionCondition(
		ExtensionContext context) {
	Optional<DisabledOnOs> optional = findAnnotation(
		context.getElement(), DisabledOnOs.class);
	if (optional.isPresent()) {
		OS[] operatingSystems = optional.get().value();
		// [... check that OS[] is not empty... ]
		return Arrays
			.stream(operatingSystems)
			.anyMatch(OS::isCurrentOs)
				? DISABLED_ON_CURRENT_OS
				: ENABLED_ON_CURRENT_OS;
	}
	return ENABLED_BY_DEFAULT;
}
```

## A Custom Condition: `@EnabledIfReachable`

For our own condition, lets check whether a URL is reachable within a specified time frame.
Once again, we start with the annotation:

```java
@Target({ METHOD, TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@ExtendWith(EnabledIfReachableCondition.class)
public @interface EnabledIfReachable {

	String url();

	int timeoutMillis();

}
```

We want to allow enabling individual tests as well as an entire class and, being good JUnit 5 citizens, we prepare our annotation for [composability via meta-annotations](junit-5-extension-model#custom-annotations), so we use the targets `METHOD`, `TYPE`, and `ANNOTATION_TYPE`.
Because we're not part of Jupiter core, we actually have to extend our annotation with the condition implementation.
As attributes we define the URL and the timeout in milliseconds.

Now, on to the condition:

```java
class EnabledIfReachableCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED_BY_DEFAULT =
			ConditionEvaluationResult.enabled(
				"@EnabledIfReachable is not present");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(
			ExtensionContext context) {
		AnnotatedElement element = context
			.getElement()
			.orElseThrow(IllegalStateException::new);
		return findAnnotation(element, EnabledIfReachable.class)
			.map(annotation -> disableIfUnreachable(annotation, element))
			.orElse(ENABLED_BY_DEFAULT);
	}

	private ConditionEvaluationResult disableIfUnreachable(
			EnabledIfReachable annotation, AnnotatedElement element) {
		String url = annotation.url();
		int timeoutMillis = annotation.timeoutMillis();
		boolean reachable = pingUrl(url, timeoutMillis);
		if (reachable)
			return enabled(format(
				"%s is enabled because %s is reachable",
				element, url));
		else
			return disabled(format(
				"%s is disabled because %s could not be reached in %dms",
				element, url, timeoutMillis));
	}

}
```

Given what we saw earlier, this is all pretty straightforward.
To check actual reachability in `pingUrl`, I used [an implementation from StackOverflow](https://stackoverflow.com/a/3584332/2525313), but the details don't really matter.

And that's already it.
Here's how `@EnabledIfReachableCondition` looks in action:

```java
class EnabledIfReachableTests {

	@Test
	@EnabledIfReachable(
		url = "http://example.org/",
		timeoutMillis = 1000)
	void reachableUrl_enabled() { }

	@Test
	@EnabledIfReachable(
		url = "http://org.example/",
		timeoutMillis = 1000)
	void unreachableUrl_disabled() { }

}
```

## Summary

Now you know how to implement conditions in JUnit Jupiter:

-   create the desired annotation and `@ExtendWith` your condition implementation
-   implement `ExecutionCondition`
-   check whether your annotation is present
-   perform the actual checks and return the result

This way, your custom condition is just as usable as the built-in `@DisabledOnOs`, `@DisabledOnJre`, `@DisabledIfSystemProperty`, `@DisabledIfEnvironmentVariable`, and their `@Enabled...` counterparts.

To deactivate conditions and run all tests, pass the following system property: `shell§-Djunit.jupiter.conditions.deactivate=*`.

For more fun with ~~[flags](https://www.youtube.com/watch?v=_e8PGPrPlwA)~~ conditions and other extension points, check the next posts in this series!
