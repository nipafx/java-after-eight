---
title: "Evolving Java With `––enable–preview` aka Preview Language Features"
tags: [java-next]
date: 2019-07-11
slug: enable-preview-language-features
description: "Use `--enable-preview` (plus `--source` or `--release` during compilation) to experiment with Java's preview features"
searchKeywords: "enable-preview"
featuredImage: preview-features
---

Since [Java 9](tag:java-9), a new major Java version is released every six months.
This has a profound impact on the entire ecosystem, not least of which is the faster turnaround time for new language features and APIs.
With just six months between major versions, the JDK team can release a feature, collect feedback, refine, and eventually finalize it.

Wait, *refine*, *finalize*?
Aren't Java features set in stone, never to be changed after their introduction?
Yes, they are, which is why new Java syntax, JVM features, and APIs that are still suspect to change are called *preview language features* (for the syntax), *preview JVM features* (for the JVM), or *incubator modules* (for APIs).
They are released for experimentation, but safe-guarded against accidental production use, so we don't bet too much code on them.
Once finalized, they're just as set in stone as every other feature.

<pullquote>They are released for *experimentation* and can change, so don't bet too much code on them.</pullquote>

Let's discuss how that works and how you can experiment with them.
This post looks at using `--enable-preview` to unlock preview language features, a concept introduced by [JEP 12](https://openjdk.java.net/jeps/12).
The same mostly applies to preview JVM features.
I will cover incubator modules in a future post.

## Unlocking Preview Features with `––enable–preview`

Each (programming) language has a syntax that defines which expressions are legal - like is this sentence not (yes, that was on purpose) - and Java's syntax evolves constantly.
Since 2017 we've got [private interface methods](java-9-tutorial#private-interface-methods), [`var`](java-10-var-type-inference), [switch expressions](java-13-switch-expressions), [text blocks](java-13-text-blocks), and a few smaller changes.
Up to and including Java 10 these features were set in stone as soon as they first appeared in a public release, but Java 11 changed that.

Since [Java 11](tag:java-11), major releases can contain syntax changes that are hidden behind the command-line flag `--enable-preview` - so-called *preview language features*.
[Java 12](java-12-guide)'s switch expressions were the first such feature and when [experimenting with it](https://github.com/nipafx/demo-java-x#language-changes) you need to add two command line flags during compilation and one when launching:

```shell
# compile
javac
	--enable-preview # activate preview features
	--release 12 # release that defines the feature
	# other flags like --class-path, -d, etc.
	# list of source files
# run
java
	--enable-preview # activate preview features
	# other flags like --class-path, etc.
	# main class
```

A few notes before we continue:

-   as you can see, preview features are not enabled individually, but with a blanket switch
-   I'll come to `--release` when discussing safeguards, but you need to know now that preview feature, compiler, and JVM all have to be from the same Java version
-   if you're just experimenting with a single source file, make your life easier and [execute it directly with `java`](scripting-java-shebang), which works great with `--enable-preview`
-   to use preview features in jshell, launch it with `--enable-preview`
-   the Javadoc binary also has that flag, but I don't know why

This will get you started, but there are more details to consider:

-   How do you activate preview features in your IDE and build tool?
-   Why even do this previewing and how stable can we expect previews to be?
-   Won't code experimenting with these features get out into the wild and wreak havoc?
-   What about relating APIs?

Let's go through these one by one.

## Enabling Previews In Tools

Build tools and IDEs can be configured to work with preview features, but the support is not always ideal.

### Maven

You need to activate preview features during compilation and test execution.
There's no single switch to do that and neither do the compiler plugin, Surefire, or Failsafe have a dedicated flag for that - instead you have to add command line arguments:

```xml
<plugin>
	<artifactId>maven-compiler-plugin</artifactId>
	<configuration>
		<release>13</release>
		<compilerArgs>
			--enable-preview
		</compilerArgs>
	</configuration>
</plugin>
<plugin>
	<artifactId>maven-surefire-plugin</artifactId>
	<configuration>
		<argLine>--enable-preview</argLine>
	</configuration>
</plugin>
<plugin>
	<artifactId>maven-failsafe-plugin</artifactId>
	<configuration>
		<argLine>--enable-preview</argLine>
	</configuration>
</plugin>
```

### Gradle

Like Maven, Gradle doesn't have explicit support for preview features and it needs to be configured manually:

```groovy
compileJava {
	options.compilerArgs += ["--enable-preview"]
}
test {
	jvmArgs '--enable-preview'
}
```

### IntelliJ IDEA

Go into the *Project Settings* to *Project* and have a look at the drop-down box for *Project language level*.
There you can choose between, for example, *13* and *13 (Preview)*.
Alternatively you can write code that uses a preview feature and thus leads to a compile error and then let the quick fix take you to the settings.
Note that these are the *Module Settings*, though - if you have multiple modules, this will only enable preview features for one of them.

The annoying thing is that IntelliJ's habitual reimports of Maven/Gradle projects doesn't square well with this manual configuration.
Indeed, as the note in the module settings warns you, it will override the configuration on reimport with the build information, ignoring the preview flags you may have added there, leaving you with compile errors until you go in and reconfigure preview features.
Annoying as hell!
There's [an issue for that](https://youtrack.jetbrains.com/issue/IDEA-212618) - please consider upvoting it.

### Eclipse

Open the *Project Properties* and go to *Java Compiler*.
The panel on the right-hand side has a checkbox *Enable preview features*.
Alternatively just use a preview feature, observe the error, and let Eclipse's quick fix guide you.

Since I don't use Eclipse, I only kicked the tires.
I didn't spend enough time with it to be annoyed by pesky details, but I'm not aware of any problems.

## But... Why?!

Why even go through the trouble, though?
The answer is straightforward: Making a mistake when designing language features or APIs comes at a high cost.
Whether it's an actual error, less-than-optimal usability, or "just" an architectural limitation for future improvements, Java fares better if they can be avoided.

Doing the best they can to get a feature just right, and *then* letting it simmer for another six months, gathering additional input from broader exposure than early access builds offer, should allow the JDK team to keep the language's quality high while it undergoes more and more changes.
That said, it is not mandatory for new language features to go through a preview phase.

## Previews, Not Beta Versions

A crucial aspect to note about preview features is that they aren't beta versions or banana software, intended to ripen at the customer.
They are released in a state that, in the past, would have been the final version (or at least very, very close to it).
That means the JDK team has invested time and energy to figure out what they consider to be the best trade-offs for the ecosystem and have released a version that they have good reason to believe will not change.

A good example for this are multiline strings in Java 12.
The feature was merged into Java's mainline in September 2018.
People started experimenting with it in the early-access builds and gave feedback, often negative, and in December the team realized that [the feature will likely undergo considerable rework](http://mail.openjdk.java.net/pipermail/jdk-dev/2018-December/002402.html).
But instead of releasing a variant that they already knew was dead on arrival, the feature was [unmerged](https://bugs.openjdk.java.net/browse/JDK-8215681) in early January 2019, last minute before feature freeze.
When Java 12 was released two months later, it didn't contain multiline strings.
(Instead they've been reworked and introduced in Java 13 as [text blocks](java-13-text-blocks) - as a preview feature, of course.)

So previews are relatively stable.
That doesn't mean you should bet a lot of code on them, but I'd consider it reasonable, even for commercial code bases, to write a little bit of code using them.
After all, experimenting on the real thing is always more informative than on laboratory or green-field projects.
But I would keep the impact small, no more than I can rework in, say, about a day if the feature is overhauled considerably or even pulled.

<pullquote>Keep the impact small; no more code than you can rework in a day.</pullquote>

## Safeguards Against Accidental Proliferation

Imagine everybody started experimenting with preview features (or incubator modules, for that matter) and then spreading that code and the artifacts around.
When a feature changes, they become outdated after just a few months and maintaining such dependencies would become a nightmare.
Don't worry, though, there are a number of safeguards preventing exactly that.
Well, from happening *accidentally* at least.

### Compiler Warnings

When compiling with `--enable-preview` the compiler warns you about preview features:

```shell
$ javac --enable-preview --release 12 # other flags

# correct compilation, but warnings:
> Note: Some input files use a preview language feature.
> Note: Recompile with -Xlint:preview for details.
```

This warning can't be disabled.

### Locking In The Source Version

When compiling source code, the compiler will by default assume that the source's version is the same as its own.
For example, when compiling with `javac` 11, it will accept all language features up to Java 11.
You can override this by setting `--source` or the newer `--release` to another, older version.
(Introduced in Java 9, `--release` combines `--source`, `--target`, and `-bootclasspath`; I will only discuss it, but the same applies to `--source`.) To continue the example, you can use this to compile code that must only use features from Java 8 with javac 11.

While `--release` is optional for "regular" compilation, it's mandatory if you use `--enable-preview`.
Otherwise you get an error like this:

```shell
# without --release
$ javac --enable-preview # other flags

> error: --enable-preview must be used with either -source or --release
```

This forces you to make explicit the version you're drawing preview features from.
And as we'll see in a bit, this always has to be the compiler's own version.

### Forced To `––enable–preview` At Run Time

There are some features that have no run-time component (`var` comes to mind) but even then you have to add `--enable-preview` to `java`.
Otherwise you will see a message like this:

```shell
# without --enable-preview
$ java # other flags

> java.lang.UnsupportedClassVersionError:
>   Preview features are not enabled for
>   org/codefx/demo/java12/lang/switch_/Switch (class file
>   version 56.65535). Try running with '--enable-preview'
```

(We'll get to those funny numbers in a minute.)

This not only happens for class files whose source code uses a preview feature, but for *all* class files that were compiled with the `--enable-preview` flag.

As the message says, the solution is to also apply `--enable-preview` at run time.
This prevents over-eager library and framework developers from sneaking artifacts that may be hard to maintain due to their reliance on volatile features into their users dependencies.
They can still try, but users will notice and can decide whether to take on the risk.

### Same Version For Feature, Compiler, And JVM

When experimenting with preview features, you need a compiler and a JVM from the same major version that introduced that feature.
For example, you can't use 13's compiler to compile code using 12's switch expressions (with `break` instead of `yield`).
If you try, you get this error:

```shell
# javac 13
$ javac --enable-preview --release 12 # other flags

> error: invalid source release 12 with --enable-preview
>   (preview language features are only supported for release 13)
```

Likewise, here's what happens when you compile that Java 12 code with 12's compiler and try to run it with 13:

```shell
#  java 13 on 12 bytecode
$ java --enable-preview # other flags

> java.lang.UnsupportedClassVersionError:
>   org/codefx/demo/java12/lang/switch_/Switch (class file
>   version 56.65535) was compiled with preview features
>   that are unsupported. This version of the Java Runtime
>   only recognizes preview features for class file version
>   57.65535
```

This prevents outdated preview features from staying in a code base.
As soon as you update to Java's next major version, you have to increment `--release` and do *something* about the preview:

-   if the feature was finalized without changes, simply drop `--enable-preview`
-   if the feature was finalized with changes, update your code and drop `--enable-preview`
-   if the feature was changed and is still in preview, update your code

As you can see, preview features force you to make sure that the code is always up-to-date with the Java version you're working with.

### Safeguards Under The Hood

The implementation for all of this is pretty straight forward.
First some background: When the compiler creates a class file, it embeds a *bytecode version*, a numerical representation of what you set `--target` (and thus `--release`) to.
(Newer bytecode versions are `(44 + javaVersion).0`, so for example `56.0` is Java 12 bytecode.) When the JVM loads a class, it checks whether it supports the given version and if it doesn't you get a message like this:

```java
java.lang.UnsupportedClassVersionError:
	Unsupported major.minor version 56.0
```

Enter preview features: If they are activated, the compiler sets the minor version to `65535` and the JVM will pick up on it to implement the behavior described above.
You can see that in the error messages, for example `(class file version 56.65535)`, which is using Java 12 preview features.

## APIs Related To Preview Features

Some language features like `var` and switch expressions stand on their own, but others require or at least benefit from supporting APIs.
For-each loops, for example, need the `Iterable` interface whereas lambdas don't *require* streams (and vice versa), but they work really well together.
What happens to APIs that relate to preview features?

The JEP categorizes them:

-   *Essential APIs* are those that are needed for the preview feature to be at all usable.
From the first release on, they will be marked with `@Deprecated(forRemoval=true)` and their documentation highlights that they only exist because of the feature, that they may change with it, and that they should only be used in conjunction with it.
If the feature is finalized, the deprecation and the preview-y part of the documentation are removed.
If the feature is removed, the API will be removed as well.
-   *Reflective APIs* are those that expose preview features via reflection, method handles, compiler API, etc.
Their annotations, documentation, and lifecycle are the same as for essential APIs.
-   *Convenient APIs* are those that relate to a preview feature, but aren't essential for it and can be used without it.
They are neither annotated nor documented in any specific way and their lifecycle is not bound to the feature.

There is a possible but no mandatory relation between preview features and incubator modules - I will discuss that in the post on incubator modules.

## Reflection

Since version 11, Java frequently previews new language features to expose them to a greater audience and thus collect more feedback before finalizing them.
During the preview, these features need to be unlocked with the `--enable-preview` flag - build tools and IDEs can be configured accordingly.
There are safeguards in place that prevent code relying on these more volatile features to sneak out of the lab:

-   during compilation `--enable-preview` needs to be paired with `--source` or `--release` for compiler's own version
-   only a JVM of the same version will execute such code and it also needs the `--enable-preview` flag, even if the feature has no run-time component

Now, go forth and experiment!
