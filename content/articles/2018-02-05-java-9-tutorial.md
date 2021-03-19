---
title: "Code-First Java 9 Tutorial"
tags: [java-9, migration, streams, optional, collections, j_ms]
date: 2018-02-05
slug: java-9-tutorial
description: "Showing code for the most important Java 9 features: private interface methods, stream and optional APIs, collection factories, reactive streams, stack walking, multi-release JARs, redirected platform logging, unified logging, the module system, and more. If you're new to Java 9, start here."
intro: "So, Java 9 came out last year... What now? Where to get started? If that's what you're asking yourself, then you've come to the right place! This Java 9 tutorial is a condensation of all you need to know to find your way around the new release, to get you ready to explore it in more depth."
searchKeywords: "java 9"
featuredImage: java-9
repo: java-x-demo
---

So, Java 9 came out last year... What now?
Where to get started?
If that's what you're asking yourself, then you've come to the right place!
This Java 9 tutorial is a condensation of all you need to know to find your way around the new release, to get you ready to explore it in more depth.
Most topics begin with a block of code, so you can see right away how it works.

We start with [setup](#getting-started-with-java-9) (including [tool support](#tool-support) and [migration challenges](#migration-challenges)) before coming to Java 9's upsides: [language changes](#language-changes) (e.g. [private interface methods](#private-interface-methods)), [new and improved APIs (e.g.](#new-and-improved-apis)[collection factory methods](#collection-factories) and improvements to [streams](#stream-api) and [optionals](#optional-api)), [changes to the JVM](#jvm-changes) (e.g. [multi-release JARs](#multi-release-jars)), and finally the new release's flagship feature, [the module system](#module-system).
There will be plenty of links for you to explore these topics further.

## Getting Started With Java 9

You can [download JDK 9 from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/jdk9-downloads-3848520.html).
Personally, I prefer to download ZIPs and just unpack them instead of using JDK 9 as my default JVM, but that might be a left-over from using the early-access build.
Nowadays you could give it a try.

### Tool Support

For the best integration into your favorite IDE you should use its most current version as Java 9 support is constantly improved.
If the cutting edge isn't for you, you should at least be on [Intellij IDEA 2017.2](https://blog.jetbrains.com/idea/2017/07/support-for-java-9-in-intellij-idea-2017-2/) or [Eclipse Oxygen.1a](https://jaxenter.com/eclipse-oxygen-1a-java-9-junit-5-138113.html) (before that version, Eclipse needed Java 9 support plugins - they are obsolete now).

Similarly, use a current version of your build tool.
In the case of Maven this should at least be 3.5.0 of the application itself and 3.7.0 of the compiler plugin.
For Gradle, [use at least 4.2.1](https://blog.gradle.org/java-9-support-update).

⇝ [Six tips for running Maven on Java 9](maven-on-java-9).

### Migration Challenges

While modularization remains fully optional, migrating to Java 9, i.e.
simply building and executing a project on the new release, may require a few changes.
The entire JDK has been modularized and together with some other internal changes this causes [migration challenges when compiling and running code on Java 9](java-9-migration-guide).
They can usually be fixed in the short-term with [the new command line options](five-command-line-options-hack-java-module-system), so you can take your time to properly resolve them.

Here are the seven most common challenges you might encounter:

-   illegal access to internal APIs
-   dependencies on Java EE modules
-   split packages
-   casting to `URLClassLoader`
-   rummaging around in runtime images
-   boot class path
-   new version strings

⇝ Read my [post on migration challenges](java-9-migration-guide) to learn how to overcome them.

## Language Changes

Java 8 revolutionized how we write code - Java 9 does not even get close.
But it *does* improve a few details and people looking for clean and warning-free code will appreciate them.

### Private Interface Methods

```java
public interface InJava8 {

	default boolean evenSum(int... numbers) {
		return sum(numbers) % 2 == 0;
	}

	default boolean oddSum(int... numbers) {
		return sum(numbers) % 2 == 1;
	}

	// before Java 9, this had to be `default`
	// and hence public
	private int sum(int[] numbers) {
		return IntStream.of(numbers).sum();
	}

}
```

As you can see, private interface methods are just that, the possibility to add `private` methods to interfaces.
They are exactly like other private methods:

-   can not be `abstract`, i.e.
must contain a body
-   can not be overriden
-   can only be called in the same source file

Their only use case is to share code between [default methods](java-default-methods-guide) without requiring you to add another default method to the interface's API.

### Try With Effectively Final Resources

```java
void doSomethingWith(Connection connection)
		throws Exception {
	// before Java 9, this had to be:
	// try (Connection c = connection)
	try(connection) {
		connection.doSomething();
	}
}
```

If `connection` is [effectively final](https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.12.4), you can write `try (connection)` instead of the laborious `try (Connection c = connection)` that you had to use before Java 9.
Finally!

### Diamond Operator

```java
<T> Box<T> createBox(T content) {
	// before Java 9, we had to put `T` there
	return new Box<>(content) {
		// useless anonymous class
	};
}
```

The diamond operator can now be applied to anonymous classes.
In some cases the compiler might derive a type that the Java type system can not express (didn't know those existed; they are called *non-denotable types*), in which case you get a compile error (this was the reason why they were not allowed in the first place).
Here's an example:

```java
Box<?> createCrazyBox(Object content) {
	List<?> innerList = Arrays.asList(content);
	// compile error
	return new Box<>(innerList) {
		// useless anonymous class
	};
}
```

### Private Safe Varargs And Less Deprecation Warning

```java
import java.io.LineNumberInputStream;

@Deprecated
public class DeprecatedImportsAndSafeVarargs<T> {

	LineNumberInputStream stream;

	@SafeVarargs
	private void compareToNext(T... args) {
		// [...]
	}

}
```

On Java 8, the `import` directive would cause a warning because `java.io.LineNumberInputStream` is deprecated and the `@SafeVarargs` annotations would cause a compile error because it was not applicable to non-final methods.
From Java 9 on, imports no longer cause deprecation warnings and `@SafeVarargs` can be applied to private methods (final or not).

## New And Improved APIs

The lack of cohesion of the new and improved APIs might make it seem that nothing much happened, but that's far from the truth!
Much work went into them - they just don't have a well-marketable label like "Streams and Lambdas".

### Stream API

```java
public Stream<LogMessage> fromWarningToError() {
	return messages.stream()
		.dropWhile(message -> message.lessThan(WARNING))
		// this actually excludes the error
		.takeWhile(message -> message.atLeast(ERROR));
}
```

The [stream API saw good improvements](java-9-stream), of which a single example can only show a little.
The changes are:

-   `Stream::ofNullable` creates a stream of either zero or one element, depending on whether the parameter passed to the method was `null` or not.
-   `Stream::iterate` create a stream much like a `for` loop.
-   `Stream::dropWhile` takes a predicate and removes elements from the stream's beginning until the predicate fails for the first time - from then on, the stream remains the same and no more elements are tested against the predicate (unlike `filter` would do).
-   `Stream::takeWhile` takes a predicate and returns elements from the stream's beginning until the predicate fails for the first time - there the stream ends and no more elements are tested against the predicate (unlike `filter` would do).

⇝ [More on stream improvements](java-9-stream).

### Optional API

```java
public interface Search {

	Optional<Customer> inMemory(String id);
	Optional<Customer> onDisk(String id);
	Optional<Customer> remotely(String id);

	default void logLogin(String id, Logger logger) {
		inMemory(id)
			.or(() -> onDisk(id))
			.or(() -> remotely(id));
			.ifPresentOrElse(
				logger::customerLogin,
				() -> logger.unknownLogin(id));
	}

}
```

The [`Optional` API was improved as well](java-9-optional) - too much for a single example.
The changes are:

-   `Optional::stream` creates a stream of either zero or one element, depending on the optional is empty or not - great to replace `.filter(Optional::isPresent).map(Optional::get)` stream pipelines with `.flatMap(Optional::stream)`.
-   `Optional::or` takes a supplier of another `Optional` and when empty, returns the instance supplied by it; otherwise returns itself.
-   `Optional::ifPresentOrElse` extends `Optional::isPresent` to take an additional parameter, a `Runnable`, that is called if the `Optional` is empty.

⇝ [More on `Optional` improvements](java-9-optional).

### Collection Factories

```java
List<String> list = List.of("a", "b", "c");
Set<String> set = Set.of("a", "b", "c");
Map<String, Integer> mapImmediate = Map.of(
	"one", 1,
	"two", 2,
	"three", 3);
Map<String, Integer> mapEntries = Map.ofEntries(
	entry("one", 1),
	entry("two", 2),
	entry("three", 3));
```

The new [collection factory methods](http://openjdk.java.net/jeps/269) `List::of`, `Set::of`, `Map::of` return collections that:

-   are immutable (unlike e.g. `Array::asList`, where elements can be replaced) but do not express that in the type system - calling e.g. `List::add` causes an `UnsupportedOperationException`
-   roundly reject `null` as elements/keys/values (unlike `ArrayList`, `HashSet`, and `HashMap`, but like `ConcurrentHashMap`)
-   for `Set` and `Map`, randomize iteration order between JDK runs

⇝ [Here's a good introduction to collection factory methods.](http://iteratrlearning.com/java9/2016/11/09/java9-collection-factory-methods.html)

### Reactive Streams

I'm gonna break with the code-first approach here, because for reactive streams there is too much code involved - have a look at [the demo](https://github.com/nipafx/demo-java-x/tree/master/src/main/java/org/codefx/demo/java9/api/reactive_streams).

[Reactive streams](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.html) require three basic types:

-   `Publisher` produces items to consume and can be subscribed to.
-   `Subscriber` subscribes to publisher and offers methods `onNext` (for new items to consume), `onError` (to inform if publisher encountered an error), `onComplete` (if publisher is done).
-   `Subscription` is the connection between publisher and subscriber and can be used to `request` items or `cancel` the subscription

The programmatic flow is as follows:

-   Creation and subscription:
	-   create `Publisher pub` and `Subscriber sub`
	-   call `pub.subscribe(sub)`
	-   `pub` creates `Subscription script` and calls `sub.onSubscription(script)`
	-   `sub` stores `script`
-   Streaming:
	-   `sub` calls `script.request(10)`
	-   `pub` calls `sub.onNext(element)` (max 10x)
-   Canceling:
	-   `sub` may call `sub.OnError(err)` or `sub.onComplete()`
	-   `sub` may call `script.cancel()`

There are no reactive APIs in JDK 9.
For now, it only contains these interfaces ([in `java.util.concurrent.Flow`](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.html)) to offer reactive libraries like RxJava that implement those interfaces a common integration point in the JDK.
In the future, JDK APIs might make use of them themselves.

⇝ [Here's a good introduction to the flow API.](http://www.baeldung.com/java-9-reactive-streams)

### Stack-Walking

```java
private static Class<?> getCallingClass() {
	return StackWalker
		.getInstance(RETAIN_CLASS_REFERENCE)
		.walk(frames -> frames
			.map(StackFrame::getDeclaringClass)
			.filter(declaringClass -> declaringClass != Utils.class)
			.findFirst()
			.orElseThrow(IllegalStateException::new);
}
```

[The new stack-walking API](https://www.sitepoint.com/deep-dive-into-java-9s-stack-walking-api/) makes it easier to walk the Java call stack and considerably improves performance of partial walks (e.g. when only to determine the immediate caller like logging frameworks do) and walks that require cheaper information (i.e.
no source code information like line number).

The trick is to first get a `StackWalker` instance and then hand a `Function<Stream<StackFrame>, T>` (plus wild cards) to `walk`, so when the walker hands you a stream of frames, you do your thing and compute your `T` (in the case above finding the `Class` that called into `Utils`), which `walk` will then return.

Why doesn't `walk` simply return a `Stream<StackFrame>`?
Because the stream is lazy (that's the whole point of the new API) and you could get weird results when evaluating it at some random future time.
Hence `walk` forces you to evaluate the frames *within* its call.

⇝ [Deep dive into stack-walking API.](https://www.sitepoint.com/deep-dive-into-java-9s-stack-walking-api/)

### OS Processes

```java
public static void main(String[] args) throws Exception {
	// tree -i /home/nipa | grep pdf
	ProcessBuilder ls = new ProcessBuilder()
		.command("tree", "-i")
		.directory(Paths.get("/home/nipa").toFile());
	ProcessBuilder grepPdf = new ProcessBuilder()
		.command("grep", "pdf")
		.redirectOutput(Redirect.INHERIT);
	List<Process> lsThenGrep = ProcessBuilder
		// new in Java 9
		.startPipeline(asList(ls, grepPdf));

	System.out.println("Started processes...");

	CompletableFuture[] lsThenGrepFutures = lsThenGrep.stream()
		// onExit returns a CompletableFuture<Process>
		.map(Process::onExit)
		.map(processFuture -> processFuture.thenAccept(
			process -> System.out.println(
				"Process " + process.getPid() + " finished.")))
		.toArray(CompletableFuture[]::new);
	// wait until all processes are finished
	CompletableFuture
			.allOf(lsThenGrepFutures)
			.join();

	System.out.println("Processes done");
}
```

[The process API](https://docs.oracle.com/javase/9/docs/api/java/lang/Process.html) got a few new methods to create process pipelines as well as new methods [on `Process`](https://docs.oracle.com/javase/9/docs/api/java/lang/Process.html) ...

-   `boolean supportsNormalTermination()`
-   `long pid()`
-   `CompletableFuture<Process> onExit()`
-   `Stream<ProcessHandle> children()`
-   `Stream<ProcessHandle> descendants()`
-   `ProcessHandle toHandle()`

... and [a new type `ProcessHandle`](https://docs.oracle.com/javase/9/docs/api/java/lang/ProcessHandle.html) with some interesting static factory methods:

-   `Stream<ProcessHandle> allProcesses()`
-   `Optional<ProcessHandle> of(long pid)`
-   `ProcessHandle current()`

Looks like all you need to build a simple task manager with Java.
😊

### Version API

```java
Version version = Runtime.version();
System.out.println(
	version.major()
		+ "." + version.minor()
		+ "." + version.security());
```

Java 9 changed the version scheme (and [Java 10 changes it again](http://openjdk.java.net/jeps/322)), which made all that prodding of system properties and parsing their values all the more error-prone.
Java 9 finally [resolves that with `Runtime.Version`](https://docs.oracle.com/javase/9/docs/api/java/lang/Runtime.Version.html), which gives you safe access to Java 9's (and 10+'s) version information with methods like `major` and `minor` (which have been [renamed for Java 10+](http://openjdk.java.net/jeps/322#API) to `feature` and `interim`).

### Further Changed APIs

-   multi-resolution images ([JEP 251](http://openjdk.java.net/jeps/251))
-   native desktop integration ([JEP 272](http://openjdk.java.net/jeps/272))
-   deserialization filter ([JEP 290](http://openjdk.java.net/jeps/290))
-   experimental HTTP/2 support ([JEP 110](http://openjdk.java.net/jeps/110), **!
[Fully supported in Java 11](java-http-2-api-tutorial) !**), DTLS ([JEP 219](http://openjdk.java.net/jeps/219)), TLS ALPN and OCSP stapling ([JEP 244](http://openjdk.java.net/jeps/244))
-   OASIS XML Catalogs 1.1 ([JEP 268](http://openjdk.java.net/jeps/268)), Xerces 2.11.0 ([JEP 255](http://openjdk.java.net/jeps/255))

## JVM Changes

Not only the language and API was improved, though.
The JVM got some new features as well.
Naturally, its a little tougher to show them with code-first, but I'll do my best.

### Multi-Release JARs

Say you have a class `Main` and another one `Version`.
`Version` is special because you need it to run different code on Java 8 and 9.
With [multi-release JARs](http://openjdk.java.net/jeps/238) you can do that as follows:

-   write `Main` for Java 8 and compile it into the folder `classes-8`
-   create two implementations of `Version` with the same fully-qualified name and the same public API; one targets Java 8, the other Java 9
-   compile them into two different folders `classes-8` and `classes-9`

With Java 9's `jar` you can do this:

```shell
jar
	--create --file mr.jar
	-C classes-8 .
	--release 9 -C classes-9 .
```

Without the last line in that command, it's the typical way to package a bunch of classes into a JAR that would look like this:

```shell
└ org
	└ codefx ... (moar folders)
		├ Main.class
		└ Version.class
```

With the last line the JAR looks like this, though:

```shell
└ org
	└ codefx ... (moar folders)
		├ Main.class
		└ Version.class
└ META-INF
	└ versions
		└ 9
			└ org
				└ codefx ... (moar folders)
					└ Version.class
```

JVMs before 8 ignore the `META-INF/versions` folder, but Java 9 will first look there when loading classes.
That means running the JAR on Java 9 will execute a different `Version` class than when running on Java 8.

With multi-release JARs you can create artifacts that execute different code, depending on the JVM version they run on.
This allows your library to use the best API *on each JVM version*, for example the throwable-creating (for stack information) and property-parsing (for version information) on Java 8 and earlier and the `StackWalking` and `Runtime.Version` APIs on Java 9.

⇝ Read my [detailed guide to multi-release JARs](multi-release-jars-multiple-java-versions).

### Redirected Platform Logging

No code this time, because you're unlikely to write any.
This is the job of your favorite logging framework's maintainers, so they can get their project ready to be the backend for all JDK log messages (*not* JVM logging).
Because from Java 9 on, the JDK will send its log messages through a set of interfaces (`System.LoggerFinder`, `System.Logger`) for which logging frameworks can provide implementations.

This feature works well with multi-release JARs, which allows the framework to work fine on older Java versions, while benefiting from the additional functionality if run on Java 9.

### Unified Logging

```shell
$ java -Xlog:gc*=debug -version

> [0.006s][info][gc,heap] Heap region size: 1M
> [0.006s][debug][gc,heap] Minimum heap 8388608  Initial heap 262144000
	Maximum heap 4192206848
# truncated about two dozen message
> [0.072s][info ][gc,heap,exit         ] Heap
# truncated a few messages showing final GC statistics
```

This time it's about JVM logging.
Thanks to a unified infrastructure ([JEP 158](http://openjdk.java.net/jeps/158), [JEP 271](http://openjdk.java.net/jeps/271)), log messages from most (in the future, all) JVM subsystems can be configured with the same command line flag.

Internally, it works similarly to common logging frameworks, with messages getting a a level, a message, a time stamp, tags, etc.
What's a little unusual is the configuration with `-Xlog`.

⇝ [In-depth guide to unified logging](java-unified-logging-xlog).

### JVM Performance Improvements

As usual, the JVM got once again faster in Java 9.
Here's the list of the performance-related changes:

-   compact strings reduce average heap size by 10% to 15% ([JEP 254](http://openjdk.java.net/jeps/254))
-   improved ("indified") string concatenation significantly reduces overhead when putting strings together ([JEP 280](http://openjdk.java.net/jeps/280))
-   Java 9 is aware of cgroup memory limits, which makes it play nicer with Docker et al (this was backported to Java 8)
-   something with interned strings and class data sharing ([JEP 250](http://openjdk.java.net/jeps/250))
-   contended locks reduce the performance overhead caused by internal bookkeeping ([JEP 143](http://openjdk.java.net/jeps/143))
-   security manager performance hit was reduced ([JEP 232](http://openjdk.java.net/jeps/232))
-   Java 2D rendering got better with the Marlin renderer ([JEP 265](http://openjdk.java.net/jeps/265))

⇝ There's a [great talk by Aleksey Shipilëv](https://www.youtube.com/watch?v=wIyeOaitmWM) about the challenges and impact of implementing compact strings and indified string concatenation.

### Further JVM changes

There are many more changes I can't go into detail on.
For something approaching completeness, I will list them instead.

-   new version strings ([JEP 223](http://openjdk.java.net/jeps/223))
-   GNU-style command line options ([JEP 293](http://openjdk.java.net/jeps/293))
-   command line flag validation ([JEP 245](http://openjdk.java.net/jeps/245))
-   reserved stack areas ([JEP 270](http://openjdk.java.net/jeps/270))

## Module System

The [Java Platform Module System (JPMS)](java-module-system-tutorial) is undoubtedly Java 9's major feature.
It posits that artifacts should no longer be plain JARs but JARs that describe a module, *modular JARs*, so to speak, and that they should be represented at runtime as modules.

<pullquote>The JPMS posits that artifacts should be represented at runtime as modules</pullquote>

A JAR is made modular by adding a *module descriptor*, `module-info.class`, which gets compiled from a *module declaration*, `module-info.java`:

```java
module com.example.project {
	requires org.library;
	requires io.framework;
	exports com.example.project.pack;
}
```

As you can see a modules has a name, expresses dependencies, and defines some exports.
The module system has many features, but its two cornerstones are:

-   making sure all required modules are presented when an application gets compiled or launched (called *reliable configuration*)
-   preventing access to all classes except the public ones in those exported packages (*string encapsulation*)

This allows compiler and runtime to fail faster when dependencies are missing or code does things it's not supposed to and will make Java applications, particularly large ones, more stable.
Other interesting features are more refined imports and exports (e.g. [optional dependencies](java-modules-optional-dependencies)), services, or the possibility to create runtime images with `jlink` with exactly the modules your application needs.

By aligning the JVM's conception (which sees all code on the class path as a big ball of mud) with ours (which usually sees trees of dependencies with artifacts that have names, dependencies, and APIs) an jarring conceptual dissonance is mended.

To process modules the module system introduces a concept paralleling the class path: the *module path*.
It expects modular JARs and represents artifacts it finds as modules.

The class path won't go anywhere, though, and remains a completely appropriate way to build and run projects.
This and a few specific mechanisms (mostly *unnamed module* and *automatic modules*) allow the Java ecosystem to modularize almost independently from one another without forcing any project to either go modular or stay plain against its maintainers will.

For a thorough introduction to the module system:

-   ⇝ read the [Code-First Java 9 Module System Tutorial](java-module-system-tutorial)
-   ⇝ get my book [The Java Module System](https://www.manning.com/books/the-java-module-system?a_aid=nipa&a_bid=869915cb) (Manning)

## Reflection

And that's it.
Phew...
