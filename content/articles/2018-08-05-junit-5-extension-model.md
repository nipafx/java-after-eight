---
title: "JUnit 5 Extension Model: How To Create Your Own Extensions"
tags: [architecture, junit-5, libraries, testing]
date: 2018-08-05
slug: junit-5-extension-model
description: "The JUnit 5 extension model enables detailed, flexible, and powerful additions to JUnit 5's core features. For that it provides specific extension points."
intro: "The JUnit 5 extension model enables detailed, flexible, and powerful additions to JUnit 5's core features. For that it provides specific extension points and easy composition of annotations."
searchKeywords: "JUnit 5 extension"
featuredImage: junit-5-extension-model
repo: junit-5-demo
---

We already know [quite](junit-5-setup) [a](junit-5-basics) [lot](junit-5-parameterized-tests) about [JUnit 5](tag:junit-5), the next version of Java's most ubiquitous testing framework.
Let's now examine [Jupiter's](junit-5-architecture-jupiter#splitting-junit-5) extension model, which allows third parties to extend JUnit with their own additions.
That's not only pretty cool for libraries and frameworks, but also very useful for application developers because they can adapt JUnit 5 to their projects' specific traits.

## JUnit 4 Extension Model

Let's first examine how JUnit 4 solved the problem.
It has two, partly competing extension mechanisms: runners and rules.

### Runners

[Test runners](https://github.com/junit-team/junit4/wiki/Test-runners) manage a test's life cycle: instantiation, calling setup and tear-down methods, running the test, handling exceptions, sending notification, etc.
and JUnit 4 provides an implementation that does all of that.

In 4.0 there was only one way to extend JUnit: Create a new runner and annotate your test class with `@RunWith(MyRunner.class)` so JUnit uses it instead of its own implementation.

This mechanism is pretty heavyweight and inconvenient for little extensions.
And it had a very severe limitation: There could always only be one runner per test class, which made it impossible to compose them.
So there was no way to benefit from the features of, e.g., both the [Theories](https://github.com/junit-team/junit4/wiki/theories) and the [Spring](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/context/junit4/SpringJUnit4ClassRunner.html) runners at the same time.

### Rules

To overcome these limitations, JUnit 4.7 introduced [rules](https://github.com/junit-team/junit4/wiki/Rules), which are annotated fields of the test class.
JUnit 4 wraps test methods (and other actions) into a statement and passes it to the rules.
They can then execute some code before and after executing the statement.
Additionally, test methods often call methods on rule instances during execution.

An example is the [temporary folder rule](http://junit.org/junit4/javadoc/latest/org/junit/rules/TemporaryFolder.html):

```java
public class HasTempFolderTest {

	@Rule
	public TemporaryFolder folder= new TemporaryFolder();

	@Test
	public void testUsingTempFolder() throws IOException {
		File createdFile= folder.newFile("myfile.txt");
		File createdFolder= folder.newFolder("subfolder");
		// ...
	}

}
```

Due to the `@Rule` annotation, JUnit calls `folder.apply` with a statement wrapping the method `testUsingTempFolder`.
This specific rule is written in such a way that `folder` creates a temporary folder, executes the test, and deletes the folder afterwards.
The test itself can then create files and folders in the temporary folder.

Other rules can [run the test in Swing’s Event Dispatch Thread](http://blog.schauderhaft.de/2010/08/15/use-cases-for-junit-rules/), set up and tear down a database, or [let the test time out](http://junit.org/junit4/javadoc/latest/org/junit/rules/Timeout.html) if it ran too long.

Rules were a big improvement over runners because they could be combined freely, although sometimes with unforeseen interactions.
Unfortunately, they are generally limited to executing some code before and after a test is run and can't help with extensions that can't be implemented within that frame.

### State Of Affairs

So since JUnit 4.7 there were two competing extension mechanisms, each with its own limitations but also with quite an overlap.
This makes clean extension difficult.
Additionally, composing different extensions can be problematic and will often not do what the developer hoped it would.

<pullquote>JUnit has two competing extension mechanisms, each with its own limitations.</pullquote>

## JUnit 5 Extension Model

JUnit 5 has a couple of [core principles](https://github.com/junit-team/junit5/wiki/Core-Principles) and one of them is to "prefer extension points over features".
This translated quite literally into an integral mechanism of the new version: extension points.
They are not the only but the most important mechanism to extend [JUnit Jupiter](junit-5-architecture-jupiter#splitting-junit-5).

<pullquote>Prefer extension points over features</pullquote>

(Note that what follows only applies to the Jupiter engine; other JUnit 5 engines don't share the same extension model.)

### Extension Points

JUnit Jupiter extensions can declare interest in certain junctures of the test life cycle.
When the JUnit Jupiter engine processes a test, it steps through these junctures and calls each registered extension.
In rough order of appearance, these are the extension points:

-   instance post processor
-   template invocation
-   execution condition
-   `@BeforeAll` callback
-   `@BeforeEach` callback
-   parameter resolution
-   before test execution callback
-   after test execution callback
-   exception handling
-   `@AfterEach` callback
-   `@AfterAll` callback

(Don't worry if it's not all that clear what each of them does.
We will look at some of them later.)

Each extension point corresponds to [an interface](https://github.com/junit-team/junit5/tree/master/junit-jupiter-api/src/main/java/org/junit/jupiter/api/extension) and their methods take arguments that capture the context at that specific point in the test's lifecycle.
An extension can implement any number of those interfaces and gets called by the engine at each of them with the respective arguments.
It can then do whatever it needs to implement its functionality.

### Extension Context

Another cornerstone of the extension model is [the `ExtensionContext` interface](http://junit.org/junit5/docs/current/api/org/junit/jupiter/api/extension/ExtensionContext.html), an instance of which is passed to every extension point's method.
It allows extensions to access information regarding the running test and also to interact with the Jupiter machinery.

<pullquote>The extension context gives access to test information and Jupiter's machinery</pullquote>

Let's have a look at a selection of its methods to see what it has to offer:

```java
Optional<ExtensionContext> getParent();
ExtensionContext getRoot();
```

To understand `getParent()` we need to peek under the hood of the Jupiter engine.
During execution it creates a tree of test nodes.
That's the same tree that your IDE uses to represent a Jupiter test run, where each container (for example, a test class or [a parameterized test method](junit-5-parameterized-tests)) is an inner node with children and each individual test (for example, a test method or one invocation of a parameterized test) is a leaf.

Each node is associated with one of these contexts and as the nodes have parents (for example, the node corresponding to a test method has the node corresponding to the surrounding test class is a parent), they let their extension context reference their parent's context.
The root context is the one associated with the root node.

```java
String getUniqueId();
String getDisplayName();
Set<String> getTags();
```

This block of methods makes a test's ID, human-readable name, and tags available.
The latter can be evaluated to influence the extension's behavior - for example, an extension may behave differently if applied to a test tagged with `"integration"`.

```java
Optional<AnnotatedElement> getElement();
Optional<Method> getTestMethod();
Optional<Class<?>> getTestClass();
Optional<Object> getTestInstance();
Optional<Lifecycle> getTestInstanceLifecycle();
```

Very importantly, the context gives access to the class or method it was created for.
This allows extensions to reflectively interact with it, for example to access a test instance's fields or a test method's annotations.
To support [custom annotations](#Custom-Annotations) you need to evaluate [meta-annotations](https://en.wikibooks.org/wiki/Java_Programming/Annotations/Meta-Annotations), but you don't have to do it by hand - use [the helper class `AnnotationSupport`](https://junit.org/junit5/docs/current/api/org/junit/platform/commons/support/AnnotationSupport.html) for that.

```java
Optional<String> getConfigurationParameter(String key)
void publishReportEntry(Map<String, String> map);
void publishReportEntry(String key, String value);
```

We will not discuss JUnit's [configuration parameters](https://junit.org/junit5/docs/current/user-guide/#running-tests-config-params) or reporting facilities in depth.
Rearding the latter, suffice it to say that it is a way to log messages into different sinks, like the console or XML reports, and `publishReportEntry` allows an extension to interact with it.

```java
Store getStore(Namespace namespace);
```

Finally, there is a store, which brings us to the next topic.

#### Stateless

There is an important detail to consider: The engine makes no guarantees *when* it instantiates extensions and *how long* it keeps instances around.
This has a number of reasons:

<pullquote>Extensions have to be stateless</pullquote>

-   It is not clear when and how extensions should be instantiated.
(For each test?
For each class?
For each run?)
-   Jupiter does not want to bother tracking extension instances.
-   If extensions were to communicate with one another, a mechanism for exchanging data would be required anyways.

Hence, extensions have to be stateless.
Any state they need to maintain has to be written to and loaded from the store that the extension context makes available.
A store is a namespaced, hierarchical, key-value data structure.
Let's look at each of these three properties in turn.

#### Namespaced

To access the store via the extension context, a [`Namespace`](http://junit.org/junit5/docs/current/api/org/junit/jupiter/api/extension/ExtensionContext.Namespace.html) must be provided.
The context returns a store that manages entries exclusively for that namespace.
This prevents collisions between different extensions operating on the same node, which could lead to accidental sharing and mutation of state.

Interestingly enough, this could also be used to *intentionally* access another extension's state, allowing communication and hence interaction between extensions.
That could lead to some interesting cross-library features...

#### Hierarchical

A store is created for each extension context, which means there is one store per node in the test tree: Each test container or test method has its own store.

In much the same way as extension contexts point to their parents, stores point to theirs.
To be more precise, when a node creates a store, it hands over a reference to its parent's store.
Thus, for example, the store belonging to a test method holds a reference to the store belonging to the test class that contains the method.
Upon queries (not edits!) a store first checks itself before delegating to its parent store.
This makes a node's state readable to all child nodes.

#### Key-Value

The store itself is a simplified map, where keys and values can be of any type.
Here are its most essential methods:

```java
interface Store {

	void put(Object key, Object value);

	<V> V get(Object key, Class<V> requiredType);

	<V> V remove(Object key, Class<V> requiredType);

}
```

The methods `get` and `remove` take a type token to prevent clients from littering their code with casts.
There is no magic there, the store simply does the casts internally, so if the token and the value's type don't line up, you still get a `ClassCastException`.
Overloads without type tokens exist as well as the `getOrComputeIfAbsent` shortcut.

### Registering Extensions

After creating the extension, all that is left to do is tell JUnit about it.
There are three ways to go about this:

<pullquote>There are three ways to register extensions</pullquote>

-   declaratively with `@ExtendWith`
-   programmatically with `@RegisterExtension`
-   automatically with the service loader

#### Declarative Registration

This is as easy as adding `@ExtendWith(MyExtension.class)` to the test class or method that needs the extension.
If registered with a container, an extension is also active for all tests it contains.

```java
@ExtendWith(MyExtension.class)
class SomeTests {

	// [... tests using MyExtension ...]

}
```

Actually, a slightly less verbose and more readable option exists, but for that we first have to examine the second pillar of JUnit's extension model, [custom annotations](#Custom-Annotations).
We'll do that right after discussing the other two approaches to registering extensions.

#### Programmatic Registration

Registering extensions with annotations is very smooth and requires only a minimum of effort, but it has one serious disadvantage: You can't do everything in an annotation!
Their values must be compile-time constants and that can be rather limiting.

This, for example, doesn't work because there is no way to pass an expression that needs to be evaluated to an annotation:

```java
@DisabledByFormula(
	"After Mayan b'ak'tun 13 and on Linux",
	now().isAfter(MAYAN_B_AK_TUN_13) && OS.determine() == OS.NIX))
class DisabledByFormulaTest {

	private static final LocalDateTime MAYAN_B_AK_TUN_13 =
		LocalDateTime.of(2012, 12, 21, 0, 0);

}
```

To make this work, the extension can be declared as a non-private field (preferably `static` [to have access to all extension points](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-programmatic-static-fields!)), programmatically instantiated with all the needed details, and then registered with `@RegisterExtension`:

```java
class DisabledByFormulaTest {

	private static final LocalDateTime MAYAN_B_AK_TUN_13 =
		LocalDateTime.of(2012, 12, 21, 0, 0);

	@RegisterExtension
	static final DisabledByFormula FORMULA = DisabledByFormula
		.disabledWhen(
			"After Mayan b'ak'tun 13 and on Linux",
			now().isAfter(MAYAN_B_AK_TUN_13)
				&& OS.determine() == OS.NIX);

}
```

Definitely more cumbersome, but sometimes it's the only way to go.

#### Automatic, Global Registration

If you have an extension that you think needs to be registered with all tests in a suite, don't bother adding it everywhere - that's what the registration via [service loader](https://docs.oracle.com/javase/10/docs/api/java/util/ServiceLoader.html) is there for.
Simply let your extension JAR proclaim that it provides implementations of `org.junit.jupiter.api.extension.Extension` and Jupiter picks it up.

Almost... Automatic registration is turned off by default, so you first need to [configure Jupiter](https://junit.org/junit5/docs/current/user-guide/#running-tests-config-params) to auto-detect extensions by setting `junit.jupiter.extensions.autodetection.enabled` to `true`.
While you're at it, consider requiring explicit activation for your extension with your own parameter (you can query it with the store's `getConfigurationParameter` method).
This way you can use your extension JAR without all global extensions being registered all the time.

### Custom Annotations

The JUnit Jupiter API is driven by annotations, and the engine does a little extra work when it checks for their presence: it looks for annotations not only on classes, methods and parameters but also *on other annotations*.
And it treats everything it finds as if it were immediately present on the examined element.
Annotating annotations is possible with so-called [meta-annotations](https://en.wikibooks.org/wiki/Java_Programming/Annotations/Meta-Annotations) and the cool thing is, all JUnit annotations are totally meta.

<pullquote>Composable annotations are a pillar of JUnit's extension model</pullquote>

This makes it possible to easily create and compose annotations that are fully functional within JUnit Jupiter:

```java
/**
 * We define a custom annotation that:
 * - stands in for '@Test' so the method gets executed
 * - has the tag "integration" so we can filter tests
 *   during the build
 */
@Target({ TYPE, METHOD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Test
@Tag("integration")
public @interface IntegrationTest { }
```

We can then use it like this:

```java
@IntegrationTest
void runsWithCustomAnnotation() {
	// this gets executed
	// even though `@IntegrationTest` is not defined by JUnit
}
```

Or we can create more succinct annotations for our extensions:

```java
@Target({ TYPE, METHOD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@ExtendWith(ExternalDatabaseExtension.class)
public @interface Database { }
```

Now we can use `@Database` instead of `@ExtendWith(ExternalDatabaseExtension.class)`.
And since we added `ElementType.ANNOTATION_TYPE` to the list of allowed targets, it is also a meta-annotation and we or others can compose it further.

If your extension ever checks for annotations, for example to determine whether it is active, it should also evaluate meta-annotations or its users can't create their own annotations with it.
Use [the helper class `AnnotationSupport`](https://junit.org/junit5/docs/current/api/org/junit/platform/commons/support/AnnotationSupport.html) for that (there are also [`ClassSupport`](https://junit.org/junit5/docs/current/api/org/junit/platform/commons/support/ClassSupport.html) and [`ReflectionSupport`](https://junit.org/junit5/docs/current/api/org/junit/platform/commons/support/ReflectionSupport.html) for easing other common tasks).

## An Example: Benchmarking Tests

Let's say we want to benchmark how long certain tests run.
First, we create the annotation we want to use:

```java
@Target({ TYPE, METHOD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@ExtendWith(BenchmarkExtension.class)
public @interface Benchmark { }
```

It already points to `BenchmarkExtension`, which we will implement next.
This is our plan:

-   to measure the run time of the whole test class, store the time before any test is executed
-   to measure the run time of individual test methods, store the time before a test's execution
-   after a test's execution, retrieve the test's launch time, compute, and print the resulting run time
-   after all tests are executed, retrieve the class' launch time and compute and print the resulting run time
-   only do any of this if the class or method is annotated with `@Benchmark`

The last point might not be immediately obvious.
Why would a method *not* annotated with `@Benchmark` be processed by the extension?
This stems from the fact that if an extension is registered with a class, it automatically applies to all methods therein.
So if the requirements state that we may want to benchmark the class but not necessarily all individual methods, we need to exclude them.
We do this by checking whether they are individually annotated.

Coincidentally, the first four points directly correspond to four of the extension points: *BeforeAll*, *BeforeTestExecution*, *AfterTestExecution*, *AfterAll*.
So all we have to do is to implement the four corresponding interfaces.
The implementations are pretty trivial - they just do what we stated above:

```java
public class BenchmarkExtension implements
		BeforeAllCallback, BeforeTestExecutionCallback,
		AfterTestExecutionCallback, AfterAllCallback {

	private static final Namespace NAMESPACE = Namespace
			.create("org", "codefx", "BenchmarkExtension");

	// EXTENSION POINTS

	@Override
	public void beforeAll(ExtensionContext context) {
		if (!shouldBeBenchmarked(context))
			return;

		storeNowAsLaunchTime(context, LaunchTimeKey.CLASS);
	}

	@Override
	public void beforeTestExecution(ExtensionContext context) {
		if (!shouldBeBenchmarked(context))
			return;

		storeNowAsLaunchTime(context, LaunchTimeKey.TEST);
	}

	@Override
	public void afterTestExecution(ExtensionContext context) {
		if (!shouldBeBenchmarked(context))
			return;

		long launchTime = loadLaunchTime(context, LaunchTimeKey.TEST);
		long elapsedTime = currentTimeMillis() - launchTime;
		report("Test", context, elapsedTime);
	}

	@Override
	public void afterAll(ExtensionContext context) {
		if (!shouldBeBenchmarked(context))
			return;

		long launchTime = loadLaunchTime(context, LaunchTimeKey.CLASS);
		long elapsedTime = currentTimeMillis() - launchTime;
		report("Test container", context, elapsedTime);
	}

	// HELPER

	private static boolean shouldBeBenchmarked(ExtensionContext context) {
		return context.getElement()
				.map(el -> isAnnotated(el, Benchmark.class))
				.orElse(false);
	}

	private static void storeNowAsLaunchTime(
			ExtensionContext context, LaunchTimeKey key) {
		context.getStore(NAMESPACE).put(key, currentTimeMillis());
	}

	private static long loadLaunchTime(
			ExtensionContext context, LaunchTimeKey key) {
		return context.getStore(NAMESPACE).get(key, long.class);
	}

	private static void report(
			String unit, ExtensionContext context, long elapsedTime) {
		String message = String.format(
			"%s '%s' took %d ms.",
			unit, context.getDisplayName(), elapsedTime);
		context.publishReportEntry("benchmark", message);
	}

	private enum LaunchTimeKey {
		CLASS, TEST
	}

}
```

Interesting details are:

-   `shouldBeBenchmarked` uses `AnnotationSupport.isAnnotated` to effortlessly determine whether the current element is (meta-)annotated with `@Benchmark`
-   `storeNowAsLaunchTime`/`loadLaunchTime` use the store to write and read the launch times
-   `report` uses the context to log its result instead of simply printing it to the console

You can find [the code on GitHub](https://github.com/nipafx/demo-junit-5/tree/master/src/main/java/org/codefx/demo/junit5).

## Reflection

We have seen that JUnit 4's runners and rules were not ideal to create clean, powerful, and composable extensions.
JUnit Jupiter overcomes their limitations with the more general concept of extension points, which allow extensions to specify at what points in a test's life cycle they want to intervene.

We have explored the context information available to an extension and how it must use the store to be stateless.
Then we discussed the three mechanisms to register an extension (declaratively with annotations, programmatically with fields, automatically with the service loader) and how to create custom annotations for seamless integration into Jupiter's API.

With the theory down we can see how to use the extension model's other extension points to [build custom conditions](junit-5-disabled-conditions), inject parameters, and generally do all kinds of interesting things.
