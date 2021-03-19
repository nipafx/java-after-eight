---
title: "Planning Your Java 9 Update"
tags: [java-9, jdeps, migration]
date: 2017-08-14
slug: planning-your-java-9-update
description: "A Java 9 update is not always trivial; quite the opposite, migrating to Java 9 can be challenging. Here's how to gather and categorize problems."
intro: "Time to put your Java 9 knowledge into practice and plan your applications migration. Here's how to get an overview of what needs to be done."
searchKeywords: "Java 9 update"
featuredImage: java-9-migration-planning
---

You've read [the module system tutorial](java-module-system-tutorial) and [the Java 9 migration guide](java-9-migration-guide) and now you're ready to start your Java 9 update?
Great!
Here are some tips on how to get a sense of what's awaiting you.

## Looking For Trouble

There are two obvious steps to take to gather the first data points for how challenging your update will be:

-   Run your regular build entirely on Java 9, ideally in a way that lets you gather all errors instead of stopping at the first.
-   If you're developing an application, build it as you do normally (meaning, not yet on Java 9) and then run it on Java 9.
Consider using `--illegal-access=debug` or `deny` to get more information on illegal access.

Carefully analyze the output, take note of new warnings and errors and try to link them to what you know about [migration challenges](java-9-migration-guide).
Also look out for warnings or errors due to removed command line options.

It is a good idea to apply some quick fixes like [adding exports](java-9-migration-guide#illegal-access-to-internal-apis) or [Java EE modules](java-9-migration-guide#dependencies-on-java-ee-modules).
This allows you to see the tougher problems that may be hiding behind benign ones.
In this phase, no fix is too dirty or too hacky - anything that gets the build to throw a new error is a victory.
If you get too many compile errors, you could compile with Java 8 and just run the tests on Java 9.

Then [run JDeps](jdeps-tutorial-analyze-java-project-dependencies) on your project *and your dependencies*.
Analyze dependencies on [JDK-internal APIs](java-9-migration-guide#illegal-access-to-internal-apis) and take not of any [Java EE modules](java-9-migration-guide#dependencies-on-java-ee-modules).
Also look for [split packages between platform modules and application JARs](java-9-migration-guide#split-packages).
A good way to get started are the following two JDeps calls, where all your project's dependencies are in the `libs` folder:

```shell
jdeps --jdk-internals -R --class-path 'libs/*' project.jar
jdeps -s -R --class-path 'libs/*' project.jar
```

Finally, search your code base for calls to `AccessibleObject::setAccessible`, [casts to `URLClassLoader`](java-9-migration-guide#casting-to-urlclassloader), [parsing of `java.version` system properties](java-9-migration-guide#new-version-strings), or [handcrafting resource URLs](java-9-migration-guide#rummaging-around-in-runtime-images).
Put everything you found on one big list - now it's time to analyze it.

## How Bad Is It?

The problems you've found should fall into the two categories "I've seen it in before" and "What the fuck is going on?".
For the former, split it up further into "Has at least a temporary fix" and "Is a hard problem." Particularly hard problems are removed APIs and package splits between platform modules and JARs that do not implement an endorsed standard or a standalone technology.

It's very important not to confuse prevalence with importance.
You might get about a thousand errors because a Java EE module is missing, but fixing that is trivial.
You're in big trouble, though, if your core feature depends on that one cast of the application class loader to `URLClassLoader`.
Or you might have a critical dependency on a removed API but because you've designed your system well, it just causes a few compile errors in one subproject.

A good approach is to ask yourself for each specific problem that you don't know a solution for off the top of your head: "How bad would it be if I cut out the troublesome code and everything that depends on it?" How much would that hurt your project?

In that vein, would it be possible to temporarily deactivate the troublesome code?
Tests can be ignored, features toggled with flags.
Get a sense for the how feasible it is to delay a fix and run the build or the application without it.

When you're all done you should have a list of issues in these three categories:

-   a known problem with an easy fix
-   a known, hard problem
-   an unknown problem, needs investigation

For problems in the last two categories, you should know how dangerous they are for your project and how easy you could get by without fixing them right now.

## On Estimating Numbers

Chances are, somebody wants you to make an estimate that involves some hard numbers - maybe in hours, maybe in cash.
That's tough in general, but here it is particularly problematic.

A [Java 9 migration](java-9-migration-guide) makes you face the music of decisions long past.
Your project might be tightly coupled to an outdated version of that Web framework you wanted to update for years, or it might a have accrued a lot of technical debt around that unmaintained library.
And unfortunately both stop working on Java 9.
What you have to do now is pay back some technical debt and everybody knows that the fees and interest can be hard to estimate.
Finally, just like a good boss battle, the critical problem, the one that costs you the most to fix, could very well be hidden behind a few other troublemakers, so you can't see it until you're in too deep.

I'm not saying these scenarios are *likely* just that they're *possible*, so be careful with guessing how long it might take you to migrate to Java 9.

## Jumping Into Action

The next step is to actually start working on the issues you collected.
I recommend to not do that in isolation, but to set your build tool and continuous integration up to build your project with Java 9 and then start solving them one by one.
More on that in another post.
