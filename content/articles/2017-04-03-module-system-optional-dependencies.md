---
title: "Optional Dependencies with `requires static`"
tags: [java-9, j_ms]
date: 2017-04-03
slug: java-modules-optional-dependencies
description: "The Java Platform Module System allows optional dependencies with `requires static`. They are accessible at compile but can be absent at run time."
intro: "The module system allows optional dependencies with the `requires static` clause. Module required this way are accessible at compile time but can be absent at run time."
searchKeywords: "optional dependencies"
featuredImage: jpms-optional-dependency
repo: jpms-monitor
---

The [Java Platform Module System](tag:j_ms) has a strong opinion on dependencies: By default, they need to be required (to be accessible) and then they need to be present both at compile and at run time.
This does not work with optional dependencies, though, where code is written against artifacts that are not necessarily present at run time.
Fortunately, the JPMS has a `requires static` clause that can be used in these exact situations.

I will show you a couple of examples in which the default behavior's strictness leads to problems and then introduce the module system's solution to optional dependencies: `requires static`.
Coding against them is not trivial, though, so we will have a close look at that as well.

If you don't know much about the module system yet, [you should read this tutorial](java-module-system-tutorial), so you've for the basics covered.
Some examples build on [the `optional-dependencies` branch](https://github.com/nipafx/demo-jpms-monitor/tree/feature-optional-dependencies) of [a small demo application](https://github.com/nipafx/demo-jpms-monitor), called the *ServiceMonitor*.

## The Conundrum Of Unrequired Dependencies

To nail down where exactly the strictness of regular `requires` clauses leads to problems, I want to start with two examples.
While similar in some aspects there are differences that become important later when we discuss how we code against potentially missing dependencies.

### The Utility Library

Let's start with an imaginary library we're maintaining, *uber.lib*, that integrates with a handful of other libraries.
Its API offers functionality that builds on them and thus exposes their types.
We'll play this through with the example of *com.google.guava*, which in our hypothetical scenario was already turned into a Java module that *uber.lib* wants to code against.

As maintainers of *uber.lib* we assume that nobody who is not already using Guava is ever going to call the Guava portion of our library.
This makes sense in certain cases: Why would you call a method in *uber.lib* that creates a nice report for a `com.google.common.graph.Graph` instance if you don't have [such a graph](https://github.com/google/guava/wiki/GraphsExplained)?

For *uber.lib* that means that it can function perfectly without *com.google.guava*: If Guava makes it into [the module graph](http://openjdk.java.net/projects/jigsaw/spec/sotms/#resolution), clients might call into that portion of the *uber.lib* API.
If it doesn't, they won't and the library will be fine as well.
We can say that *uber.lib* never needs the dependency for its own sake.

With regular `requires` clauses, such an optional relationship can not be implemented, though.
According to the rules for [readability](http://openjdk.java.net/projects/jigsaw/spec/sotms/#readability) and [accessibility](http://openjdk.java.net/projects/jigsaw/spec/sotms/#accessibility), *uber.lib* has to require *com.google.guava* to compile against its types but this forces all clients to always have Guava on the module path when launching their application.

<pullquote>With regular dependencies optional relationships can not be implemented.</pullquote>

If *uber.lib* integrates with a handful of libraries, it would make clients depend on *all* of them even though they might never use more than one.

That's not a nice move from us.

### The Fancy Statistics Library

The second example comes from [the demo application](https://github.com/nipafx/demo-jpms-monitor), which contains a module *monitor.statistics*.
Let's assume there was some advanced statistics library containing a module *stats.fancy* that *monitor.statistics* wants to use but which could not be present on the module path for each deployment of the application.
(The reason for that is irrelevant but let's go with a license that prevents the fancy code from being used "for evil" but, evil masterminds that we are, we occasionally want to do just that.)

We would like to write code in *monitor.statistics* that uses types from the fancy module but for that to work we need to depend on it with a `requires` clause.
If we do that, though, the module system would not let the application launch if *stats.fancy* is not present.

<contentimage slug="jpms-optional-dependency-conundrum" options="bg"></contentimage>

Deadlock.
Again.

## Optional Dependencies With `requires static`

When a module needs to be compiled against types from another module but does not want to depend on it at run time, it can use a `requires static` clause.
If `foo requires static bar`, the module system behaves different at compile and run time:

-   At compile time, *bar* must be present or there will be an error.
During compilation *bar* is readable by *foo*.
-   At run time, *bar* might be absent and that will cause neither error nor warning.
If it is present, it is readable by *foo*.

We can immediately put this into action and create an optional dependency from *monitor.statistics* to *stats.fancy*:

```java
module monitor.statistics {
	requires monitor.observer;
	requires static stats.fancy;
	exports monitor.statistics;
}
```

If *stats.fancy* is missing during *compilation*, we get an error when the module declaration is compiled:

```java
monitor.statistics/src/main/java/module-info.java:3:
	error: module not found: stats.fancy
		requires static stats.fancy;
					         ^
1 error
```

At *launch time*, though, the module system does not care whether *stats.fancy* is present or not.

Similarly, the module descriptor for *uber.lib* declares all dependencies as optional:

```java
module uber.lib {
	requires static com.google.guava;
	requires static org.apache.commons.lang;
	requires static org.apache.commons.io;
	requires static io.javaslang;
	requires static com.aol.cyclops;
}
```

Now that we know how to declare optional dependencies, two questions remain to be answered:

-   Under what circumstances will it be present?
-   How can we code against an an optional dependency?

We will answer both questions next.

## Resolution Of Optional Dependencies

[Module resolution](http://openjdk.java.net/projects/jigsaw/spec/sotms/#resolution) is the process that, given an initial module and a universe of observable modules, builds a module graph by resolving `requires` clauses.
When a module is being resolved, all modules it requires must be found in the universe of observable modules.
If they are, they are added to the module graph; otherwise an error occurs.
It is important to note that modules that did not make it into the module graph during resolution are not available later during compilation or execution, either.

At compile time, module resolution handles optional dependencies just like regular dependencies.
At run time, though, `requires static` clauses are mostly ignored.
When the module system encounters one it does not try to fulfill it, meaning it does not even check whether the named module is present in the universe of observable modules.

As a consequence even if a module is present on the module path (or in the JDK for that matter), it will *not* be added to the module graph just because of an optional dependency.
It will only make it into the graph if it is also a regular dependency of some other module that is being resolved or because it was added explicitly with the command line flag `--add-modules`.

<pullquote>A module that is only an optional dependency will not be available at run time.</pullquote>

Maybe you stumbled across the phrase that optional dependencies "are *mostly* ignored".
Why mostly?
Well, one thing the module system does is if an optional dependency makes it into a graph, a readability edge is added.
This ensures that if the optional module is present, its types can be accessed straight away.


## Coding Against Optional Dependencies

Optional dependencies require a little more thought when writing code against them because this is what happens when *monitor.statistics* uses types in *stats.fancy* but the module isn't present at run time:

```java
Exception in thread "main" java.lang.NoClassDefFoundError:
	stats/fancy/FancyStats
		at monitor.statistics/monitor.statistics.Statistician
			.<init>(Statistician.java:15)
		at monitor/monitor.Main.createMonitor(Main.java:42)
		at monitor/monitor.Main.main(Main.java:22)
Caused by: java.lang.ClassNotFoundException: stats.fancy.FancyStats
		... many more
```

Oops.
We usually don't want our code to do that.

Generally speaking, when the code that is currently being executed references a type, the Java Virtual Machine checks whether it is already loaded.
If not, it tells the class loader to do that and if that fails, the result is a `NoClassDefFoundError`, which usually crashes the application or at least fails out of the chunk of logic that was being executed.

This is something [JAR hell was famous for](jar-hell#unexpressed-dependencies) and that the module system [wants to overcome](motivation-goals-project-jigsaw#reliable-configuration) by checking declared dependencies when launching an application.
But with `requires static` we opt out of that check, which means we can end up with a `NoClassDefFoundError` after all.
What can we do against that?

<pullquote>With optional dependencies we opt out of the checks that make the module system safe.</pullquote>

### Established Dependency

Before looking into solutions, though, we need to see whether we really have a problem.
In the case of *uber.lib* we expect to only use types from an optional dependency if the code calling into the library already uses them, meaning class loading already succeeded.

In other words, when *uber.lib* gets called all required dependencies must be present or the call would not have been possible.
So we don't have a problem after all and don't need to do anything.

<contentimage slug="jpms-optional-dependency-coding-established" options="bg"></contentimage>

### Internal Dependency

The general case is different, though.
It might very well be the module with the optional dependency that first tries to load classes from it, so the risk of a `NoClassDefFoundError` is very real.

<contentimage slug="jpms-optional-dependency-coding-internal" options="bg"></contentimage>

One solution for this is to make sure that all possible calls into the module with the optional dependency have to go through a checkpoint before accessing the dependency.
That checkpoint has to evaluate whether the dependency is present and send all code that arrives at it down a different execution path if it isn't.

<contentimage slug="jpms-optional-dependency-coding-checked" options="bg"></contentimage>

The module system offers a way to check whether a module is present.
I explained in [my newsletter](news) how to get there and why I use [the new stack-walking API](https://www.sitepoint.com/deep-dive-into-java-9s-stack-walking-api/), so here you'll just have to trust me when I say that this is the way to go:

```java
import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;
import java.lang.StackWalker.StackFrame;

public class ModuleUtils {

	public static boolean isModulePresent(String moduleName) {
		return StackWalker
				.getInstance(RETAIN_CLASS_REFERENCE)
				.walk(frames -> frames
					    .map(StackFrame::getDeclaringClass)
					    .filter(declaringClass ->
					            declaringClass != ModuleUtils.class)
					    .findFirst()
					    .orElse((Class) ModuleUtils.class))
				.getModule()
				.getLayer()
				.findModule(moduleName)
				.isPresent();
		// chain all the methods!
	}

}
```

(In a real application it might make sense to cache the value as to not always repeat the same check.)

Calling this method with an argument like `"stats.fancy"` will return whether that module is present.
If called with the name of a regular dependency (simple `requires` clause), the result will always be `true` because otherwise the module system would not have let the application launch.
If called with the name of an optional dependency (`requires static` clause), the result will either be `true` or `false`.

If an optional dependency is present, the module system established readability and so it is safe to go down an execution path that uses types from the module.
If it is absent, choosing such a path would lead to a `NoClassDefFoundError`, so a different one has to be found.

## Summary

Sometimes you want to write code against a dependency that might not always be present at run time.
To make the dependency's types available at compile time but not enforce its presence at launch time, the module system offers the `requires static` clause.
Note, though, that a module does not get picked up during resolution if it is only referenced this way and that special care needs to be taken to make sure code does not crash if the optional dependency is absent at run time.

To learn more about the module system check out [the JPMS tag](tag:j_ms) or [get my book *The Java 9 Module System*](https://www.manning.com/books/the-java-9-module-system?a_aid=nipa&a_bid=869915cb) (with Manning).
If you're interested in the historical perspective, check [the Project Jigsaw tag](tag:project-jigsaw).
