---
title: "JavaFX Sources in Eclipse"
tags: [tools, javafx]
date: 2014-11-13
slug: javafx-sources-in-eclipse
description: "A quick step by step guide how to use the JavaFX sources in Eclipse by attaching them to the current JDK."
searchKeywords: "JavaFX sources in eclipse"
featuredImage: javafx-with-attached-source
---

Just a quickie about how to tell Eclipse to attach the JavaFX sources for an improved development experience.

## Java Sources

When you're using [Eclipse](http://eclipse.org/) with a JDK as your system library, the IDE will helpfully display [Javadoc](https://en.wikipedia.org/wiki/Javadoc) when you hover over members of official Java classes and let you jump into the code whenever you want to.
This is extremely helpful to get to know the classes and take a look at their inner workings.

This is possible because the JDK comes with its own sources and Eclipse knows about them.
It attaches those sources to the compiled classes from the JDK and thus provides you those benefits.

## JavaFX Sources

For reasons unknown to me, Eclipse does not automatically do this for the JavaFX classes (from the package `javafx` ), though.
But because like the rest of the JRE, the FX sources are also bundled with the JDK, few things are easier than to attach them.

In Eclipse:

1. open preferences (*Window* \~&gt; *Preferences*)
2. edit the used JRE (*Java* \~&gt; *Installed JREs* \~&gt; Select JDK on the right \~&gt; *Edit*)
3. start to attach FX sources (select **jfxrt.jar** \~&gt; *Source Attachement)*
4. configure the sources (select *External location* \~&gt; *External File*)
5. select the sources bundled with the JDK

	(go to JDK root folder, e.g. C:/Program Files/Java/jdk1.8.0\_20

	    \~&gt; select **javafx-src.zip**)

6. almost done, just OK/Finish your way back...

To verify that this worked, open the *Open Type* dialog (by default with *Ctrl-Shift-T*) and check that you can open types from JavaFX like `javafx.application.Application`, `javafx.application.Platform` or `javafx.beans.property.Property`.

You should also be able to see tooltips like this one:

## Reflection

You have seen a step by step guide how to attach the JavaFX sources in Eclipse so it shows the Javadoc and lets you jump into the source code.

Last step: Have even more fun with JavaFX!
