---
title: "JavaOne 2015: Prepare For JDK 9"
tags: [java-next, impulse, java-9, community, project-jigsaw]
date: 2015-11-05
slug: javaone-2015-prepare-for-jdk-9
description: "JavaOne 2015 saw a series of talks by the Project Jigsaw team about modularity in Java 9. This one explains how to prepare for it."
searchKeywords: "JavaOne"
featuredImage: javaone-project-jigsaw-prepare-sf
---

<snippet markdown="java-one-2015-intro"></snippet>

Let's start with preparations for JDK 9!

- **Content**: What to expect when moving from JDK 8 to JDK 9
- **Speaker**: Alan Bateman
- **Links**: [Video](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=2h53m55s) and [Slides](http://openjdk.java.net/projects/jigsaw/j1/prepare-for-jdk9-j1-2015.pdf)

<contentvideo slug="javaone-2015-jigsaw"></contentvideo>

## Background

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=2h56m04s)
Alan Bateman begins the talk by giving some background information.

### JDK 9 And Project Jigsaw Goals

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=2h56m15s)
A quick recap of Jigsaw's goals.
For more details, see [my post about them](motivation-goals-project-jigsaw#goals-of-project-jigsaw).

### Modularity Landscape

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=2h56m54s)
A short overview over the multitude of [Java Specification Requests](https://en.wikipedia.org/wiki/Java_Community_Process) (JSRs) and [JDK Enhancement Proposals](https://en.wikipedia.org/wiki/JDK_Enhancement_Proposal) (JEPs) that cover Project Jigsaw's efforts.

### Compatibility

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=2h59m28s)
Bateman categorizes the kinds of APIs exposed by the JDK:

-   Supported and intended for external use:
	-   JCP standard: `java.*`, `javax.*`
	-   JDK-specific API: some `com.sun.*`, some `jdk.*`
-   Not intended for external use: `sun.*`, rest `com.sun.*`, rest `jdk.*`

He points out that if an application uses only supported APIs and works on Java N, it should also work on Java N+1.
Java 9 will make use of this and change/remove APIs that have been internal or deprecated in Java 8.

He then goes into managing (in)compatibilities and mentions a post by Joseph Darcy, [Kinds of Compatibility: Source, Binary, and Behavioral](https://blogs.oracle.com/darcy/entry/kinds_of_compatibility), that he recommends to read.
It sheds some light on the different aspects of compatibility and hence, by extension, the complexity of evolving Java.

## Incompatible Changes In JDK 9

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h07m02s)
The bulk of this talk covers the different incompatibilities Java 9 will incur.
This is largely covered by my post about [how Java 9 may break your code](how-java-9-and-project-jigsaw-may-break-your-code).

### Encapsulating JDK-Internal APIs

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h07m37s)
Bateman starts by presenting some data on uses of internal APIs.
Details can be found on [slide 16](http://openjdk.java.net/projects/jigsaw/j1/prepare-for-jdk9-j1-2015.pdf#page=16) but the gist is that only a couple of APIs are frequently used.

APIs that are not used in the wild or are only used for convenience are non-critical.
By default, these will be encapsulated in Java 9.
Those in actual use for which it would be hard or impossible to create implementations outside of the JDK are deemed critical.
If alternatives exist, they will also be encapsulated.

The critical APIs without alternative will be deprecated in Java 9, with the plan to remove them in 10.
[JEP 260](http://openjdk.java.net/jeps/260) proposes the following APIs for this:

-   `sun.misc.Unsafe`
-   `sun.misc.{Signal,SignalHandler}`
-   `sun.misc.Cleaner`
-   `sun.reflect.Reflection::getCallerClass`
-   `sun.reflect.ReflectionFactory`

If you miss something on the list, contact the Jigsaw team and argue your case (and bring data to support it).

He [then](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h16m06s) goes into how [*jdeps*](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jdeps.html) can be used to find the uses of internal APIs.
This part also contains some examples of what will happen if problematic code is run on JDK 9 (start [here](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h20m03s)) and how to solve such issues (start [here](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h24m14s)).

### Removing API

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h26m36s)
This is quick.
The following six methods will not be present in Java 9:

-   `java.util.logging.LogManager::addPropertyChangeListener`
-   `java.util.logging.LogManager::removePropertyChangeListener`
-   `java.util.jar.Pack200.Packer::addPropertyChangeListener`
-   `java.util.jar.Pack200.Packer::removePropertyChangeListener`
-   `java.util.jar.Pack200.Unpacker::addPropertyChangeListener`
-   `java.util.jar.Pack200.Unpacker::removePropertyChangeListener`

### Change Of JDK/JRE Binary Structure

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h28m06s)
By [merging JDK and JRE](how-java-9-and-project-jigsaw-may-break-your-code) into a common structure, several existing practices will stop working.

Bateman describes some of the problems with the old run-time image directory layout and presents how the new one will look.
[Slides 29 and 30](http://openjdk.java.net/projects/jigsaw/j1/prepare-for-jdk9-j1-2015.pdf#page=29) juxtapose both layouts:

<contentimage slug="javaone-project-jigsaw-jdk-structure"></contentimage>

Since Java 7 there is an API with which tools can interact with these files regardless of the physical layout.
This also means that version N can access version N+1 files.

### Removed Mechanisms

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h34m56s)
As I [described earlier](how-java-9-and-project-jigsaw-may-break-your-code), the [endorsed standards override mechanism](http://docs.oracle.com/javase/8/docs/technotes/guides/standards/index.html) and the [extension mechanism](http://docs.oracle.com/javase/tutorial/ext/) will be removed.
They will be replaced by [upgradeable modules](http://openjdk.java.net/projects/jigsaw/spec/reqs/#upgradeable-modules).

### Other Changes

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h37m10s)
See [JEP 261](http://openjdk.java.net/jeps/261) (section Risks And Assumptions) for a full list of changes.
Bateman names a few:

-   Application and extension class loaders are no longer instances of `java.net.URLClassLoader`.
-   Command line arguments `-Xbootclasspath` and `-Xbootclasspath/p` are removed.
-   System property `sun.boot.class.path` is removed.

### Non-Jigsaw Incompatibilities in Java 9

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h40m07s)
Bateman also shorty addresses two issues that are not connected to Project Jigsaw but will show up in Java 9 and might break some code:

-   The version-string schema changes.
For details see [JEP 223](http://openjdk.java.net/jeps/223) - it also has a nice comparison of current and future version strings.
-   Underscore is no longer allowed as a one-character identifier.

## What Can You Do To Prepare For Java 9?

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h44m55s)
There are a couple of preparatory steps you can take:

-   Check code for usages of JDK-internal APIs with *jdeps*.
-   Check code that might be sensitive to the version-string schema change.
-   Check code for uses of underscore as an identifier.
-   If you develop tools, check code for a dependency on *rt.jar*, *tools.jar*, or the runtime-image layout in general.
-   Test the JDK 9 EA builds and Project Jigsaw EA builds.

Make sure to report any unexpected or overly problematic findings back to [the Jigsaw mailing list](http://mail.openjdk.java.net/mailman/listinfo/jigsaw-dev).

## Questions

There were a couple of question, of which I picked the two most interesting ones.

### How Can Libraries Target Java 8 and Java 9?

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h49m03s)
[JEP 238](http://openjdk.java.net/jeps/238) will introduce multi-release JARs, i.e.
JARs that can contain specialized code for specific Java releases.

### When Does Support For Java 8 End?

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=3h49m03s)
Nobody on stage knew the exact answer so they pointed to [the documentation of Oracle's update policy on oracle.com](http://www.oracle.com/technetwork/java/eol-135779.html).
The current answer is: Not before September 2017.
