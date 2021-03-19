---
title: "JDeps Maven Plugin 0.2 Released"
tags: [java-9, jdeps, tools, project-jigsaw]
date: 2015-12-07
slug: jdeps-maven-plugin-0-2
description: "With v0.2 the JDeps Maven Plugin allows the creation of flexible exceptions from build-breaking for a self-paced preparation for Java 9 and Project Jigsaw."
intro: "With the second release the JDeps Maven Plugin allows the creation of flexible exemptions from build-breaking. This enables a self-paced migration away from dependencies of JDK-internal APIs that will be unavailable in Java 9."
searchKeywords: "JDeps Maven Plugin"
featuredImage: jdeps-mvn-motor
repo: jdeps-maven-plugin
---

It only took me six months after [the initial release](jdeps-maven-plugin-0-1) but last week I finally published [version 0.2](https://github.com/nipafx/JDeps-Maven-Plugin/releases/tag/v0.2) of my *JDeps Maven Plugin*, now lovingly nicknamed *JDeps Mvn*.

Since Apache released theirs recently this begs the question "Why even bother?" Well, because mine actually understands [*jdeps*](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jdeps.html) and can do some stuff the official can't.
This makes it much more useful for large projects.

Before we go on, let me point you to [that old post again](jdeps-maven-plugin-0-1).
You might want to read it if you need some background on how Java 9 may break your code and how *jdeps* can help.
Many links lead to the [documentation on GitHub](https://github.com/nipafx/JDeps-Maven-Plugin/wiki), of which the [walkthrough](https://github.com/nipafx/JDeps-Maven-Plugin/wiki/Walkthrough) comes highly recommended.

## What's So Special?

When including *jdpes* in a large project's build, two things are prohibitive:

-   Having to clean up all problematic dependencies before the build will pass.
-   Having to manually create unflexible exemptions from build-breaking.

*JDeps Mvn* helps with both:

-   Smartly interacting rules allow a detailed and flexible configuration.
-   Initial rules can be automatically created from identified dependencies.

If no rule matches, the plugin will apply a [default value](https://github.com/nipafx/JDeps-Maven-Plugin/wiki/Configuration#default-severity), which will usually be configured to FAIL.
This way the rules can be used to create exemptions and have the build break on all unexpected dependencies.

<contentimage slug="JDeps-Maven-Plugin-v0.2" options="sidebar"></contentimage>

### Introducing Rules

[Rules](https://github.com/nipafx/JDeps-Maven-Plugin/wiki/Concepts#rules) are of the form `dependent -> dependee: severity`, where both the dependent and the dependee are fully qualified class or package names.

The most basic rules are used to define severities for class-to-class dependencies:

```shell
org.food.fruits.Mango -> sun.misc.BASE64Decoder: INFORM
org.food.fruits.Mango -> sun.misc.BASE64Encoder: INFORM
org.food.fruits.Banana -> sun.misc.Unsafe: INFORM
```

So you will get get a log message on INFO level, when `Mango` uses either `BASE64Decoder` or `BASE64Encoder` or when `Banana` uses `Unsafe`.

But it is also possible to create broader rules.
For example this will set all uses of `Unsafe` in `org.food.fruits` to INFORM:

```shell
org.food.fruits -> sun.misc.Unsafe: INFORM
```

It is up to you how the plugin will interpret [package relations](https://github.com/nipafx/JDeps-Maven-Plugin/wiki/Configuration#package-inclusion).
By default it does it like the JVM, where the concept of "subpackages" does not exist and `org.food` and `org.food.fruits` are unrelated.
But you can also configure a hierarchical mode where packages are interpreted like folders and `org.food` contains `org.food.fruits`.

So you have this package where the use of `sun.misc` is generally ok except for the one subpackage where everything but `Unsafe` could already be removed and must not creep back?
No problem:

```xml
org.food -> sun.misc: WARN
org.food.fruit -> sun.misc: FAIL
org.food.fruit -> sun.misc.Unsafe: WARN
```

The interaction of conflicting rules is [well-defined](https://github.com/nipafx/JDeps-Maven-Plugin/wiki/Concepts#resolution) and follows general intuition.
In short: Find the best match on the left side that still matches on the right.
If there are several such matches, pick the one with the most specific right side.

### Creating Rules

You can of course go through your code base or the output of a *jdeps* run and write rules to exempt the existing dependencies from breaking your build.
Or you can have *JDeps Mvn* [do that for you](https://github.com/nipafx/JDeps-Maven-Plugin/wiki/Walkthrough#godlike-clap):

```xml
<configuration>
	<defaultSeverity>WARN</defaultSeverity>
	<outputRulesForViolations>true</outputRulesForViolations>
	<outputRuleFormat>ARROW</outputRuleFormat>
	<outputFilePath>path/to/dependency_rules.xml</outputFilePath>
</configuration>
```

With this configuration *JDeps Mvn* will write a rule for each dependency it finds.
So if `Mango` uses `BASE64Decoder` and `BASE64Encoder` and `Banana` uses `Unsafe`, the result will be:

```xml
<arrowDependencyRules>
	<arrowRules>
		org.food.fruits.Mango -> sun.misc.BASE64Decoder: WARN
		org.food.fruits.Mango -> sun.misc.BASE64Encoder: WARN
		org.food.fruits.Banana -> sun.misc.Unsafe: WARN
	</arrowRules>
</arrowDependencyRules>
```

*JDeps Mvn* will always append to the file, so if you have a multi-module build and use an absolute path you can gather rules for all of them in one file.

You can now edit this block as you like and then move it into your pom.
(As long as `outputRulesForViolations` is set to `true` *JDeps Mvn* will never break the build so make sure to turn it off when it's no longer needed.)

Changing the default severity to FAIL will then make sure the build breaks except for the defined exemptions.
Your config might then look like this:

```xml
<configuration>
	<defaultSeverity>FAIL</defaultSeverity>
	<arrowDependencyRules>
		<arrowRules>
			org.food.fruits.Mango -> sun.misc.BASE64Decoder: WARN
			org.food.fruits.Mango -> sun.misc.BASE64Encoder: WARN
			org.food.fruits.Banana -> sun.misc.Unsafe: WARN
		</arrowRules>
	</arrowDependencyRules>
</configuration>
```

## What Else Is There?

Nothing much feature-wise.
As I said, there is [proper documentation](https://github.com/nipafx/JDeps-Maven-Plugin/wiki), so if you start using *JDeps Mvn* in anger, make sure to check it out.
If you do, I'd be happy to hear your feedback!
