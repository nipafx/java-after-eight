---
title: "A JDeps Tutorial - Analyze Your Project's Dependencies"
tags: [java-basics, j_ms, jdeps, tools]
date: 2017-07-17
slug: jdeps-tutorial-analyze-java-project-dependencies
description: "JDeps is a dependency analysis tool for Java bytecode (class files and JARs). Learn how to use filters, aggregate results, and create diagrams."
intro: "JDeps is a dependency analysis tool for Java bytecode, i.e. class files and JARs. This primer teaches you how to use filters, aggregate results, and create diagrams."
searchKeywords: "jdeps"
featuredImage: jdeps-tutorial
inlineCodeLanguage: shell
---

[JDeps](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jdeps.html) is the *Java Dependency Analysis Tool*, a command line tool that processes Java bytecode, meaning `.class` files or the JARs that contain them, and analyzes the statically declared dependencies between classes.
The results can be filtered in various ways and can be aggregated to package or JAR level.
JDeps can also tell you which JDK-internal APIs your project is using and is fully aware of [the module system](tag:j_ms).
All in all it is a very useful tool to examine various forms of dependency graphs.

In this tutorial, I'll introduce you to how JDeps works - follow-up posts will show you some great use cases for it.

## Code Along

For this tutorial, I encourage you to follow along, preferably with one of *your* projects.
It will be easiest if you have a JAR of your project and next to it a folder with all its transitive dependencies.
If you're using Maven, you can achieve the latter with [the *maven-dependency-plugin*'s `copy-dependencies` goal](https://maven.apache.org/plugins/maven-dependency-plugin/examples/copying-project-dependencies.html).
With Gradle, you can use [a `Copy` task, setting `from` to `configurations.compile` or `configurations.runtime`](https://discuss.gradle.org/t/how-can-i-gather-all-my-projects-dependencies-into-a-folder/7146/4).

As my sample project I picked [Scaffold Hunter](http://scaffoldhunter.sourceforge.net/):

> Scaffold Hunter is a Java-based open source tool for the visual analysis of data sets with a focus on data from the life sciences, aiming at an intuitive access to large and complex data sets.
The tool offers a variety of views, e.g. graph, dendrogram, and plot view, as well as analysis methods, e.g. for clustering and classification.

I [downloaded](https://sourceforge.net/projects/scaffoldhunter/files/2.6.3/scaffold-hunter-2.6.3.zip/download) the 2.6.3 release ZIP and copied all dependencies into `libs`.

When showing output, I abbreviate `scaffoldhunter` (in package names) and `scaffold-hunter` (in file names) to `sh` to make it shorter.

## Getting To Know JDeps

You can find the JDeps executable `jdeps` in your JDK's `bin` folder since Java 8.
Working with it is easiest if it is available on the command line, for which you might have to perform some setup steps specific to your operating systems.
Make sure that `jdeps --version` works and shows that the Java 9 version is running.

Next step is to grab a JAR and set JDeps loose on it.
Used without further command line options it will first list the JDK modules the code depends on.
That is followed by a list of package-level dependencies, which is organized as ` ->  `.

Calling `jdeps sh-2.6.3.jar` results in the following output:

```shell
$ jdeps sh-2.6.3.jar

sh-2.6.3.jar -> java.base
sh-2.6.3.jar -> java.datatransfer
sh-2.6.3.jar -> java.desktop
sh-2.6.3.jar -> java.logging
sh-2.6.3.jar -> java.prefs
sh-2.6.3.jar -> java.sql
sh-2.6.3.jar -> java.xml
sh-2.6.3.jar -> not found
   edu.udo.sh -> com.beust.jcommander  not found
   edu.udo.sh -> edu.udo.sh.data       sh-2.6.3.jar
   edu.udo.sh -> edu.udo.sh.gui        sh-2.6.3.jar
   edu.udo.sh -> edu.udo.sh.gui.util   sh-2.6.3.jar
   edu.udo.sh -> edu.udo.sh.util       sh-2.6.3.jar
   edu.udo.sh -> java.io               java.base
   edu.udo.sh -> java.lang             java.base
   edu.udo.sh -> javax.swing           java.desktop
   edu.udo.sh -> org.slf4j             not found
[... truncated many more package dependencies ...]
```

You can see that Scaffold Hunter depends on the modules *java.base* (of course), *java.desktop* (it's a Swing application), *java.sql* (data sets are stored in SQL data bases), and a few others.
This is followed by the long list of package dependencies, which is a little too much to take in.
Note that some dependencies are marked as `not found`, which makes sense as I did not tell JDeps where to look for them.

Now it's time to configure JDeps with the various options.
You can list them with `jdeps -h`.

## Including Dependencies

An important aspect of JDeps is that it allows you to analyze your dependencies as if they were part of your code.
A first step to that goal is putting them onto the class path with `--class-path`.

That enables JDeps to follow the paths into your dependencies' JARs and rids you of the `not found` indicators.
To actually analyze the dependencies as well you need to make JDeps recurse into them with `-recursive` or `-R`.

To include Scaffold Hunter's dependencies, I execute JDeps with `--class-path 'libs/*'` and `-recursive`:

```shell
$ jdeps --class-path 'libs/*' -recursive sh-2.6.3.jar

[... truncated split package warnings ...]
[... truncated some module/JAR dependencies...]
sh-2.6.3.jar -> libs/commons-codec-1.6.jar
sh-2.6.3.jar -> libs/commons-io-2.4.jar
sh-2.6.3.jar -> libs/dom4j-1.6.1.jar
sh-2.6.3.jar -> libs/exp4j-0.1.38.jar
sh-2.6.3.jar -> libs/guava-18.0.jar
sh-2.6.3.jar -> libs/heaps-2.0.jar
sh-2.6.3.jar -> libs/hibernate-core-4.3.6.Final.jar
sh-2.6.3.jar -> java.base
sh-2.6.3.jar -> java.datatransfer
sh-2.6.3.jar -> java.desktop
sh-2.6.3.jar -> java.logging
sh-2.6.3.jar -> java.prefs
sh-2.6.3.jar -> java.sql
sh-2.6.3.jar -> java.xml
sh-2.6.3.jar -> libs/javassist-3.18.1-GA.jar
sh-2.6.3.jar -> libs/jcommander-1.35.jar
[... truncated more module/JAR dependencies...]
   edu.udo.sh -> com.beust.jcommander  jcommander-1.35.jar
   edu.udo.sh -> edu.udo.sh.data       sh-2.6.3.jar
   edu.udo.sh -> edu.udo.sh.gui        sh-2.6.3.jar
   edu.udo.sh -> edu.udo.sh.gui.util   sh-2.6.3.jar
   edu.udo.sh -> edu.udo.sh.util       sh-2.6.3.jar
   edu.udo.sh -> java.io               java.base
   edu.udo.sh -> java.lang             java.base
   edu.udo.sh -> javax.swing           java.desktop
   edu.udo.sh -> org.slf4j             slf4j-api-1.7.5.jar
[... truncated many, many more package dependencies ...]
```

In this specific case the output begins with a few split package warnings that I'm going to ignore for now.
The following module/JAR and package dependencies are like before but now all are found, so there are much more of them.
This makes the output all the more overwhelming, though, so it is high time to look into how we can make sense from so much data.

## Configuring JDeps' Output

There are various ways to configure JDeps' output.
Maybe the best option to use in a first analysis of any project is `-summary` or `-s`, which only shows dependencies between JARs and leaves out the package dependencies.
The following table lists various other ways to get different perspectives on the dependencies:

| Option | Description |
|--------|-------------|
| `--package` or `-p` | Followed by a package name it only considers dependencies *on* that package, which is a great way to see all the places where those `utils` are used. |
| `--regex` or `-e` | Followed by a regular expression it only considers dependencies *on classes* that match the regex. (Note that unless `-verbose:class` is used, output still shows packages.) |
| `-filter` or `-f` | Followed by a regular expression it *excludes* dependencies on classes that match the regex. (Note that unless `-verbose:class` is used, output still shows packages.) |
| `-filter:archive` | In many cases dependencies *within* an artifact are not that interesting. This option ignores them and only shows dependencies *across* artifacts. |
| `--api-only` | Sometimes, particularly if you’re analyzing a library, you only care about a JARs API. With this option, only types mentioned in the signatures of public and protected members of public classes are examined. |

Output on the command line is a good way to examine details and drill deeper into interesting bits.
It doesn't make for the most intuitive overview, though - diagrams are much better at that.
Fortunately, JDeps has the `--dot-output` option, which creates [`.dot` files](https://en.wikipedia.org/wiki/DOT_(graph_description_language)) for each of the individual analyses.
These files are pure text but other tools, e.g. [Graphviz](http://graphviz.org/), can then be used to create images from them.

These two commands yield the following diagram:

```shell
$ jdeps --class-path 'libs/*' -recursive --dot-output dots sh-2.6.3.jar
$ dot -Tpng -O dots/summary.dot
```

<contentimage slug="jdeps-scaffoldhunter-jars"></contentimage>

## Drilling Deeper

If you want to go into more details, `-verbose:class` will list dependencies between classes instead of aggregating them to package level.

Sometimes, listing only direct dependencies on a package or class is not enough because they might not actually be in your code but in your dependencies.
In that case `--inverse` or `-I` might help.
Given a specific package or regex to look for it tracks the dependencies back as far as they go, listing the artifacts along the way.
Unfortunately, there seems to be no straight-forward way to see the result on the level of classes instead of artifacts.

There are a few more options that might help you in your specific case - as mentioned you can list them with `jdeps -h`.

## JDeps And Modules

Just like the compiler and the JVM can operate on a higher level of abstraction thanks to [the module system](tag:j_ms), so can JDeps.
The module path can be specified with `--module-path` (note that `-p` is already reserved, so it is not a shorthand of this option) and the initial module with `--module` or `-m`.
From there, the analyses we made above can be made just the same.

Because Scaffold Hunter is not yet modularized, I'll switch to the example project I use in [my book about the Java 9 module system](), [the *Monitor* application]().
Here, I'm creating a summary analysis of the module relations:

```shell
# on `master` branch
$ jdeps --module-path mods:libs -m monitor -summary -recursive

[... truncated some module dependencies...]
monitor -> java.base
monitor -> monitor.observer
monitor -> monitor.observer.alpha
monitor -> monitor.observer.beta
monitor -> monitor.persistence
monitor -> monitor.rest
monitor -> monitor.statistics
monitor.observer -> java.base
monitor.observer.alpha -> java.base
monitor.observer.alpha -> monitor.observer
monitor.observer.beta -> java.base
monitor.observer.beta -> monitor.observer
monitor.persistence -> java.base
monitor.persistence -> monitor.statistics
monitor.rest -> java.base
monitor.rest -> monitor.statistics
monitor.rest -> spark.core
monitor.statistics -> java.base
monitor.statistics -> monitor.observer
slf4j.api -> java.base
slf4j.api -> not found
spark.core -> JDK removed internal API
spark.core -> java.base
spark.core -> javax.servlet.api
spark.core -> jetty.server
spark.core -> jetty.servlet
spark.core -> jetty.util
spark.core -> slf4j.api
spark.core -> websocket.api
spark.core -> websocket.server
spark.core -> websocket.servlet
[... truncated more module dependencies...]
```

Beyond that, there are some Java 9 and module-specific options.
With `--require <modules>` you can list all modules that require the named ones.
You can use `--jdk-internals` to analyze a project's problematic dependencies and `--generate-module-info` or `--generate-open-module` to create first drafts of module descriptors.
As mentioned in passing, JDeps will also always report all [split packages](java-9-migration-guide#split-packages) it finds.

In a future post, I will show you how to use these flags to help your project's modularization along.
But even before that, JDeps is an important tool when [migrating to Java 9](java-9-migration-guide).

## Reflection

With JDeps you can analyze your project's statically declared dependencies.
It operates on the class level but aggregates results to package and artifact levels.
With various filters you can focus on the aspects that matter most to you.
Maybe the most basic analysis is a graph of artifact dependencies across your code and third party libraries:

```shell
$ jdeps --class-path 'libs/*' -summary -recursive sh-2.6.3.jar
```

It can be used to perform some very interesting analyses, particularly on larger code bases.
I'll soon show you some examples for that.
