---
title: "JavaOne 2015: Introduction to Modular Development"
tags: [java-next, impulse, java-9, community, project-jigsaw]
date: 2015-11-09
slug: javaone-2015-introduction-to-modular-development
description: "JavaOne 2015 saw a series of talks by the Project Jigsaw team about modularity in Java 9. This one introduces the basic concepts."
searchKeywords: "JavaOne"
featuredImage: javaone-project-jigsaw-introduction-sf
---

<snippet markdown="java-one-2015-intro"></snippet>

After [preparing for JDK 9](javaone-2015-prepare-for-jdk-9) let's continue with an introduction to modular development!

- **Content**: Introduction to the module system and the concept of modules
- **Speaker**: Alan Bateman
- **Links**: [Video](https://www.youtube.com/watch?v=8RhwmJlZQgs&t=4h25m19s) and [Slides](http://openjdk.java.net/projects/jigsaw/j1/intro-modular-dev-j1-2015.pdf)

<contentvideo slug="javaone-2015-jigsaw"></contentvideo>

## What Is A Module?

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=4h26m38s)
Alan Bateman starts by explaining the basic concept of modules as named, self describing collections of code and data.
This part is more than covered by [The State Of The Module System](http://openjdk.java.net/projects/jigsaw/spec/sotms/) (SOTMS):

-   [Modules](http://openjdk.java.net/projects/jigsaw/spec/sotms/#modules) and [module declarations](http://openjdk.java.net/projects/jigsaw/spec/sotms/#module-declarations)
-   [Module graphs](http://openjdk.java.net/projects/jigsaw/spec/sotms/#module-graphs)
-   [Readability](http://openjdk.java.net/projects/jigsaw/spec/sotms/#readability) and [implied readability](http://openjdk.java.net/projects/jigsaw/spec/sotms/#implied-readability)
-   [Accessibility](http://openjdk.java.net/projects/jigsaw/spec/sotms/#accessibility)

## Platform Modules

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=4h39m58s)
Platform modules are the ones that make up the JDK - [SOTMS explains them](http://openjdk.java.net/projects/jigsaw/spec/sotms/#platform-modules) as well.
Their dependency graph is shown on [Slide 19](http://openjdk.java.net/projects/jigsaw/j1/intro-modular-dev-j1-2015.pdf#page=19):

<contentimage slug="javaone-project-jigsaw-platform-modules" options="narrow"></contentimage>

A [very similar graph](https://bugs.openjdk.java.net/secure/attachment/21573/jdk-tr.png) that includes the OpenJDK-specific modules can be found in [JEP 200](http://openjdk.java.net/jeps/200).

Bateman also mentions `java -listmods`, which will list all the available platform modules.
(Note that there are [discussions on the mailing list](http://mail.openjdk.java.net/pipermail/jigsaw-dev/2015-October/005042.html) to rename the flag.)

## Command Line

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=4h42m39s)
After explaining the [module path](http://openjdk.java.net/projects/jigsaw/spec/sotms/#module-path) Bateman gives an introduction to the various new command line options.
The [Jigsaw quick-start guide](http://openjdk.java.net/projects/jigsaw/quick-start) has us covered here.

An option the guide does not mention is `java -Xdiag:resolver`, which outputs additional information regarding module dependency resolution.

## Packaging As Modular JAR

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=4h49m54s)
Modules can be packaged into so called modular JARs, which the [quick-start guide](http://openjdk.java.net/projects/jigsaw/quick-start#packaging) covers as well.

Bateman stresses that such JARs work both on the module path in Java 9 as well as on the class path in Java 8 (as long as they target 1.8).
He also quickly shows how the module path and class path can be mixed to launch a program.

## Linking

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=4h56m13s)
Linking allows to bundle some modules and all of their transitive dependencies into a run-time image.
If the initial modules are platform modules, the result will essentially be a variant of the JDK.
This is in fact how the current Jigsaw builds are being created.

This is done with the new tool *jlink* and the [quick-start guide](http://openjdk.java.net/projects/jigsaw/quick-start#linker) shows how to do it.

## Questions

There were a couple of interesting questions.

### Is There Any Solution For Optional Dependencies?

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=5h04m43s)
In earlier Jigsaw prototypes there was a notion of *optional dependencies*.
Working out the precise semantics turned out to be hard so the feature was not implemented.
Research showed that optional dependencies can typically be refactored to services that might or might not be present at runtime.

Services are covered by the [quick-start guide](http://openjdk.java.net/projects/jigsaw/quick-start#services).

### Can jlink Cross-Compile?
Can It Create A Self-Executing File?

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=5h05m39s)
"Yes" and "Not directly but other tools will be improved so that will be doable in the future".

### Can Modules Be Versioned?

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=5h07m42s)
Long story short: "Versions are hard, we don't want to replicate build tool functionality, so 'No'".
For more, listen to Mark Reinhold's full answer.

### Can jlink Use Cross-Module Optimizations?

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=5h10m22s)
Yes.

### How Does JavaDoc Handle Modules?

[PLAY](https://www.youtube.com/watch?v=8RhwmJlZQgs#t=5h14m37s)
JavaDoc will be upgraded so that it understands what modules are.
It will display them along with packages and classes.
And it will also by default not generate documentation for types in not-exported packages.
