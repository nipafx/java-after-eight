---
title: "Java 9 Resources - Talks, Articles, Repos, Blogs, Books And Courses"
tags: [java-9]
date: 2017-05-02
slug: java-9-resources-talks-articles-blogs-books-courses
canonicalUrl: https://www.sitepoint.com/java-9-resources
description: "Java 9 draws and the number of posts and talks about it skyrocketed in the recent months. Here's a list of recommended talks and articles but also further resources where new, high-quality content will pop up."
featuredImage: java-9-resources
---

You can tell that Java 9 draws near because the number of posts and talks about it skyrocketed in the recent months.
I want to recommend existing talks and articles where you can learn about Java 9 but also further resources, where new, high-quality content will pop up.

## Talks

If you're the kind of person who likes to watch talks, there are quite a few I can recommend.
For a great high-level overview and conceptual intro to the module system, watch [*Java 9: Make Way for Modules!*](https://www.youtube.com/watch?v=Bj_nUbIhJg8) (Mark Reinhold; 40 min).
Going deeper into the module system, the JDK team has an entire series of talks:

-   [*Introduction to Modular Development*](https://www.youtube.com/watch?v=eALw4P_0O4k) (Alan Bateman; 55 min)
-   [*Advanced Modular Development*](https://www.youtube.com/watch?v=WJHjKMIrbD0) (Mark Reinhold, Alan Bateman; 60 min)
-   [*Project Jigsaw: Under The Hood*](https://www.youtube.com/watch?v=fxB9cVNcyZo) (Mark Reinhold; 60 min)

As a follow-up, there was an [*Ask the Architect* session at JFokus](https://www.youtube.com/watch?v=sO1fumd8e4o) where Mark Reinhold answers all kinds of questions, among them some about Java 9 (transitive dependencies, version conflicts, state of JavaFX, ahead-of-time compilation; 23 min).

With Java 9 coming closer, people started presenting on non-modularity features that Java 9 has to offer.
Simon Ritter races through [55 new features in JDK 9](https://www.youtube.com/watch?v=CMMzG8I23lY) (50 min) and [this is me](https://www.youtube.com/watch?v=vOdFuvIyN0E) talking a little bit about the module system before going into the new language features, a few new APIs (collection factories, reactive streams aka Flow API, stack-walking, multi-release JARs) and performance (50 min).
If you want to dive deeper, there is a [talk by Aleksey Shipilëv](https://www.youtube.com/watch?v=wIyeOaitmWM) on compact strings and indified string concatenation, which I highly recommend (60 min).
[Monica Beckwith explains about G1](https://vimeo.com/181948157) but be warned, you better have your GC expertise down before giving this a try (55 min).

There are also a number of great talks that are much more practical.
To learn about how Maven deals with with Java 9, [watch Robert Scholte](https://www.youtube.com/watch?v=Wef9p4ykNMM) talk about Unicode encoding, version strings, cross compilation, multi-release JARS, and then of course Jigsaw with its impact on how Maven works but also what it has to offer (50 min).
Don't miss live-coding queen [Trisha Gee working on a Java 9 project](https://www.youtube.com/watch?v=96vce1qd0QY) with IntelliJ, where she demonstrates various features of both the JVM and the IDE (30 min).
If you're interested to see what a migration to Java 9 modules might look like, [watch Rabea Gransberger](https://www.youtube.com/watch?v=hUZb4iOaizg) live-refactor a small demo project (15 min).
Of course there is no way to talk about live-coding without mentioning Venkat Subramaniam, who shows off modules and JShell [in a mammoth 150 minute live session](https://www.youtube.com/watch?v=8XmYT89fBKg).

For shorter bits there are a couple of interviews the Voxxed folks recorded:

-   [Mark Reinhold talks](https://www.youtube.com/watch?v=R83xS0bNHTM) about strong encapsulation, open modules, compatibility, and `sun.misc.Unsafe` (10 min).
-   [Venkat Subramaniam talks](https://www.youtube.com/watch?v=OjJBau4ZNyA) about compatibility, the Flow API, and wishes for future Java versions (13 min).
-   [I talk](https://www.youtube.com/watch?v=bZu6MGefHU0) about migration and command line escapes (not yet [the new `--permit-illegal-access`](http://mail.openjdk.java.net/pipermail/jigsaw-dev/2017-March/011763.html), though) (7 min)

## Repositories

Of course people started writing code and there are a few interesting repositories that demonstrate Java 9 features:

-   [CodeFX/java-9](https://github.com/nipafx/demo-java-x): A repository demonstrating all kinds of Java 9 features
-   [AdoptOpenJDK/jdk9-jigsaw](https://github.com/AdoptOpenJDK/jdk9-jigsaw): Examples of some of the module system features
-   [CodeFX/jigsaw-advent-calendar](https://github.com/nipafx/demo-jigsaw-advent-calendar): A simple demo application demonstrating JPMS features
-   [CodeFX/jpms-monitor](https://github.com/nipafx/demo-jpms-monitor): The JPMS demo application I use in my book

## Articles

There are countless articles about Java 9, so there is simply no way to do everyone justice.
Here's my try of listing the more important ones.

Overviews:

-   [The Ultimate Guide](https://www.sitepoint.com/ultimate-guide-to-java-9/)
-   [Programming with Modularity and Project Jigsaw](https://www.infoq.com/articles/Latest-Project-Jigsaw-Usage-Tutorial)

New features:

-   [Additions To Stream API](java-9-stream/), [New Stream Collectors](http://www.baeldung.com/java9-stream-collectors)
-   [Process API: The Shape of Things to Come](http://iteratrlearning.com/java/2017/03/12/java9-process-api.html)
-   [Java Time (JSR-310) enhancements](http://blog.joda.org/2017/02/java-time-jsr-310-enhancements-java-9.html)
-   [Concurrency Updates](https://www.voxxed.com/blog/2016/10/java-9-series-concurrency-updates/)
-   [Additions To Optional](java-9-optional/)
-   [Deep Dive into Stack-Walking API](https://www.sitepoint.com/deep-dive-into-java-9s-stack-walking-api/)
-   [Convenience Factory Methods for Collections](http://www.baeldung.com/java-9-collections-factory-methods)

Under the hood:

-   [Applying `@Deprecated` Enhancements](http://marxsoftware.blogspot.de/2016/08/applying-jdk-9-deprecated-enhancements.html)
-   [Using `sun.misc.Unsafe`](http://gregluck.com/blog/archives/2017/03/using-sun-misc-unsafe-in-java-9/)
-   [Variable Handles](https://www.voxxed.com/blog/2016/11/java-9-series-variable-handles/)
-   [Reflection vs Encapsulation – Stand Off in the Java Module System](https://www.sitepoint.com/reflection-vs-encapsulation-in-the-java-module-system/)

JVM features:

-   [Generating Multi-Release JARs with Maven](http://word-bits.flurg.com/multrelease-jars/), [Building Multi-Release JARs with Maven](http://in.relation.to/2017/02/13/building-multi-release-jars-with-maven/)
-   [Adjust Memory Limits if Running with Docker](https://www.infoq.com/news/2017/02/java-memory-limit-container)
-   [JShell - Getting Started and Examples](http://jakubdziworski.github.io/java/2016/07/31/jshell-getting-started-examples.html)

For a longer list of posts, check out [Baeldung's Java 9 site](http://www.baeldung.com/java-9).

I want to end whit list with a teaser: One particular interesting part about Java 9 modularity is whether build tools will generate module declarations.
Next Monday Robert Scholte, chairman of the Apache Maven project and principal developer of Maven's Java 9 compatibility and features, will tell us whether Maven can do that for you.
Spoiler: It doesn't look good.

## Blogs

There are a few blogs and sites that regularly publish about Java 9.
Most of them have the decency to tag those posts, so you don't have to go searching.

Company blogs/sites:

-   [Oracle](https://blogs.oracle.com/java/) (no Java 9 tag)
-   [SitePoint](http://sitepoint.com/tag/java-9/) (surprise!)
-   [Voxxed](https://www.voxxed.com/blog/tag/java-9/) (including a [nice series](https://www.voxxed.com/blog/tag/java-9-series/))

Personal blogs:

-   [Baeldung](http://www.baeldung.com/tag/java-9/) (Eugen Baeldung)
-   [Iteratr Learning](http://iteratrlearning.com/articles) (no Java 9 tag; Raoul-Gabriel Urma and Richard Warbuton)
-   [CodeFX](tag:java-9) (mine)
-   [Joda](http://blog.joda.org/search/label/java9) (Stephen Colebourne)

## Books and Courses

If you want to go really deep and prepare yourself for actually using the new stuff, you might want to go for a book or online course.
These are the ones I know of:

-   [*Java 9 Modularity: First Look*](https://www.pluralsight.com/courses/java-9-modularity-first-look) (Sander Mak with Pluralsight)
-   [*Java 9 Modularity*](http://shop.oreilly.com/product/0636920049494.do) (Sander Mak and Paul Bakker with O'Reilly)
-   [*Java 9 Modularity - Project Jigsaw and Scalable Java Applications*](http://www.apress.com/de/book/9781484227121) (Alexandru Jecan with Apress)
-   [*Mastering Java 9*](https://www.packtpub.com/application-development/mastering-java-9) (Martin Toshev with Packt)
-   [*Modular Programming in Java 9*](https://www.packtpub.com/application-development/modular-programming-java-9) (Koushik Kothagal with Packt)
-   [*Java 9 with JShell*](https://www.packtpub.com/application-development/java-9-jshell) (Gastón C.
Hillar with Packt)
-   [*Java 9 Module System*](https://www.manning.com/books/the-java-9-module-system?a_aid=nipa&a_bid=869915cb) (me with Manning)

And because this is my blog I will take the freedom to make sure you don't overlook that the last book on that list is by me, which of course makes it the best one.
😜 Early access is open, so you can get it now!
