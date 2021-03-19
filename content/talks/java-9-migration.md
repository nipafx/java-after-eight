---
title: "To JAR Hell And Back"
# subtitle: A Live Migration to Java 11
tags: [java-9, java-11, j_ms, migration]
date: 2017-07-25
slug: talk-java-9-migration
description: "A live-coding talk where we take a typical Java 8 code base and update it to Java 9 and beyond, overcoming some common and some less common hurdles like dependencies on internal APIs and split packages"
searchKeywords: "java 9 migration"
featuredImage: java-9-migration
slides: https://slides.nipafx.dev/java-9-migration
videoSlug: java-9-migration-devoxx-be-2018
repo: java-9-migration
---

I'm sure you've heard about compatibility issues when upgrading from Java 8 to 9 and beyond, but did you try it yourself yet?
This live coding session starts with a typical Java 8 application and runs up against and eventually overcomes the common hurdles:

* build system configuration
* dependency analysis with `jdeps`
* dependencies on internal APIs and Java EE modules
* split packages

To get the most out of this talk, you should have a good understanding of the module system basics - afterwards you will know how to approach *your* application's migration to Java 9 and the module system.
