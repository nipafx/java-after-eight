---
title: "Use Builders... Cautiously - Effective Java, Item 2"
# subtitle
tags: [book-club, patterns]
date: 2018-10-09
slug: effective-java-builders
videoSlug: effective-java-02
description: "Why and how to avoid the builder pattern and how to make best use of it if you can't"
searchKeywords: "Effective Java"
featuredImage: effective-java-02
repo: effective-java
---

The builder pattern is a powerful tool to ease the instantiation of complex classes.
Whether constructor parameters are too numerous, there are too many of the same type, or whether many are optional - with a builder you can make your life easier.
Although, I posit, often you can make your life even easier by directly tackling the class' or constructor's complexity.

In this video I show an example of how to simplify a class to make a builder obsolete, but also how to build more powerful builders that add more value than just simplifying constructor calls.

Links to follow up:

* [JavaZone in Oslo](https://2018.javazone.no/)
* [my opinion on `Optional`](intention-revealing-code-java-8-optional)
* [`Map.of` et al](java-9-tutorial/#collection-factories)
* [named & default parameters in Kotlin](http://www.deadcoderising.com/kotlin-how-to-use-default-parameters-in-functions-and-constructors/)
* [automaton](https://brilliant.org/wiki/finite-state-machines/)
* [partial application](https://en.wikipedia.org/wiki/Partial_application)
* [self types with generics](https://www.sitepoint.com/self-types-with-javas-generics/)
