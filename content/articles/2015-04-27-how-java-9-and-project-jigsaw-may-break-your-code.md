---
title: "How Java 9 And Project Jigsaw May Break Your Code"
tags: [java-next, java-9, project-jigsaw]
date: 2015-04-27
slug: how-java-9-and-project-jigsaw-may-break-your-code
description: "With Java 9 comes Project Jigsaw - a modularization of the JDK - which will break existing code. An overview over the planned changes lets you see whether yours is affected."
searchKeywords: "Project Jigsaw"
featuredImage: java-9-jigsaw-breaks-code
---

Java 9 looms on the horizon and it will come with a completed [Project Jigsaw](http://openjdk.java.net/projects/jigsaw/).
I didn't pay much attention to it until I learned from [a recent discussion on the OpenJFX mailing list](http://mail.openjdk.java.net/pipermail/openjfx-dev/2015-April/017017.html) that it may break existing code.
This is very unusual for Java so it piqued my interest.

I went reading the project's [JEP](http://en.wikipedia.org/wiki/JDK_Enhancement_Proposal)s and some related articles and came to the conclusion that, yes, this will break existing code.
It depends on your project whether you will be affected but you might be and it might hurt.

<admonition type="note">

Since I wrote this post [Project Jigsaw](tag:project-jigsaw) resulted in the [Java Platform Module System](tag:j_ms) and a lot of details changed.
I explain all of them in my [Java 9 migration guide](java-9-migration-guide), so you should read that one instead.

</admonition>

## Project Jigsaw

I might write a more detailed description of Project Jigsaw at some point, but for now I'll be lazy and simply quote:

> The primary goals of this Project are to:
>
> -   Make the Java SE Platform, and the JDK, more easily scalable down to small computing devices;
> -   Improve the security and maintainability of Java SE Platform Implementations in general, and the JDK in particular;
> -   Enable improved application performance; and
> -   Make it easier for developers to construct and maintain libraries and large applications, for both the Java SE and EE Platforms.
>
> To achieve these goals we propose to design and implement a standard module system for the Java SE Platform and to apply that system to the Platform itself, and to the JDK.
> The module system should be powerful enough to modularize the JDK and other large legacy code bases, yet still be approachable by all developers.
>
> [Jigsaw Project Site - Feb 11 2015](http://openjdk.java.net/projects/jigsaw/)

If you want to know more about the project, check out [its site](http://openjdk.java.net/projects/jigsaw/) and especially the [list of goals and requirements](http://openjdk.java.net/projects/jigsaw/goals-reqs/) (current version is [draft 3](http://openjdk.java.net/projects/jigsaw/goals-reqs/03) from July 2014).

The main thing to take away here is the module system.
From version 9 on Java code can be (and the JRE/JDK will be) organized in modules *instead of* JAR files.

## Breaking Code

This sounds like an internal refactoring so why would it break existing code?
Well, it doesn't do that necessarily and compatibility is even one of the project's central requirements (as usual for Java):

> An application that uses only standard Java SE APIs, and possibly also JDK-specific APIs, must work the same way \[...\] as it does today.
>
> [Project Jigsaw: Goals & Requirements - DRAFT 3](http://openjdk.java.net/projects/jigsaw/goals-reqs/03#preserve-compatibility)

The important part is the qualification "only standard APIs".
There are plenty of ways to create applications which for some critical detail rely on unspecified or deprecated properties like non-standard APIs, undocumented folder structures and internal organizations of JAR files.

So let's see the potentially breaking changes.
For more details, make sure to check the project's site, especially [JEP 220](http://openjdk.java.net/jeps/220), which contains a more precise description of most of what follows.

### Internal APIs Become Unavailable

<admonition type="note">

For the current take on internal APIs, [go here](java-9-migration-guide#illegal-access-to-internal-apis).

</admonition>

With JAR files any public class is visible anywhere in the JVM.
This severely limits the ability of JDK-implementations to keep internal APIs private.
Instead many are accessible and they are often used for a variety of reasons (e.g. to improve performance or work around \[former\] bugs in the Java runtime; [the Java FAQ explains why that may be a bad idea](http://www.oracle.com/technetwork/java/faq-sun-packages-142232.html)).

This changes with modules.
Every module will be able to explicitly declare which types are made available as part of its API.
The JDK will use this feature to properly encapsulate all internal APIs which will hence become unavailable.

This may turn out to be the biggest source of incompatibilities with Java 9.
It surely is the least subtle one as it causes compile errors.

To prepare for Java 9 you could check your code for dependencies upon internal APIs.
Everything you find must be replaced one way or another.
Some workarounds might have become unnecessary.
Other classes might find their way into the public API.
To find out whether this is the case, you will have to research and maybe resort to asking this on the [OpenJDK mailing list](http://mail.openjdk.java.net/mailman/listinfo) for the functionality you are interested in.

#### Internal APIs

So what are internal APIs?
Definitely everything that lives in a `sun.*`-package.
I could not confirm whether everything in `com.sun.*` is private as well - surely some parts are but maybe not all of them?

<admonition type="update" hint="5th of May, 2015">

This got cleared up in [a comment by Stuart Marks](how-java-9-and-project-jigsaw-may-break-your-code)<!-- comment-1994660530 --> as follows:

> Unfortunately, com.sun is a mixture of internal and publicly supported ("exported") APIs.
> An annotation @jdk.Exported distinguishes the latter from internal APIs.
> Note also that com.sun.\* packages are only part of the Oracle (formerly Sun) JDK, and they are not part of Java SE.
>
> So if starts with `com.sun.*`, it won't exist on any non-Oracle JDK.
> And if it belongs to one of those packages and is not annotated with `@jdk.Exported`, it will be inaccessible from Java 9 on.

</admonition>

<admonition type="update" hint="early 2017">

Due to the fact that much mission critical code depends on internal APIs for which no alternative exist, a specific module, namely [*jdk.unsupported*](http://cr.openjdk.java.net/~mr/jigsaw/ea/module-summary.html#jdk.unsupported), was created that exports the packages `sun.misc` (yes that means the infamous `sun.misc.Unsafe` is still available), `sun.reflect`, and `com.sun.nio.file`.
The module will be available while the JDK team works on supported APIs to replace it, and once that happens it will eventually be removed.

</admonition>

Two examples, which might prove especially problematic, are `sun.misc.Unsafe` and everything in `com.sun.javafx.*`.
Apparently the former is used in quite a number of projects for mission and performance critical code.
From personal experience I can say that the latter is a crucial ingredient to properly building JavaFX controls (e.g. all of [ControlsFX](http://controlsfx.org/) depends on these packages).
It is also needed to work around a number of bugs.

Both of these special cases are considered for being turned into public API (see for [Unsafe](http://mail.openjdk.java.net/pipermail/discuss/2013-October/003162.html) and for [JavaFX](javafx-project-jigsaw-jep-253) - although some people would rather see [Unsafe die in a fire](http://mail.openjdk.java.net/pipermail/openjfx-dev/2015-April/017028.html "Private APIs not usable in Java 9?
- OpenJFX mailing list")).

#### Tool Support

Fortunately you don't have to find these dependencies by hand.
Since Java 8 the JDK contains the Java Dependency Analysis Tool *jdeps* ([introduction with some internal packages](https://wiki.openjdk.java.net/display/JDK8/Java+Dependency+Analysis+Tool), official documentation for [windows](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/jdeps.html) and [unix](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jdeps.html)), which can list all packages upon which a project depends.

If you run it with the parameter *-jdkinternals*, it will output all internal APIs your project uses - exactly the ones which you will have to deal with before Java 9 rolls around.

Update (15th of May, 2015)
:   *JDeps does not yet recognize all packages which will be unavailable in Java 9.
This affects at least those which belong to JavaFX as can be seen in [JDK-8077349](https://bugs.openjdk.java.net/browse/JDK-8077349).
I could not find other issues regarding missing functionality (using [this search](https://bugs.openjdk.java.net/issues/?jql=status%20in%20%28Open%2C%20%22In%20Progress%22%2C%20Reopened%29%20AND%20text%20~%20%22jdeps%22)).*

<!-- -->

Update (11th of May, 2015)
:   *I created a Maven plugin which uses JDeps to discover problematic dependencies and breaks the build if it finds any.
See [the release post](jdeps-maven-plugin-0-1) for details.*

### Merge Of JDK And JRE

<admonition type="note">

For the current take on runtime image directory layout, internal JARs, and resource URLs, go [here](java-9-migration-guide#rummaging-around-in-runtime-images).

</admonition>

The main goal of Project Jigsaw is the modularization of the Java Platform to allow the flexible creation of runtime images.
As such the JDK and JRE loose their distinct character and become just two possible points in a spectrum of module combinations.

This implies that both artifacts will have the same structure.
This includes the folder structure and any code which relies on it (e.g. by utilizing the fact that a JDK folder contains a subfolder *jre*) will stop working correctly.

### Internal JARs Become Unavailable

Internal JARs like *lib/rt.jar* and *lib/tools.jar* will no longer be accessible.
Their content will be stored in implementation-specific files with a deliberately unspecified and possibly changing format.

Code which assumes the existence of these files, will stop working correctly.
This might also lead to some transitional pains in IDEs or similar tools as they heavily rely on these files.

### New URL Schema For Runtime Image Content

Some APIs return URLs to class and resource files in the runtime (e.g. [`ClassLoader.getSystemResource`](https://docs.oracle.com/javase/8/docs/api/java/lang/ClassLoader.html#getSystemResource-java.lang.String-)).
Before Java 9 these are *jar URLs* and they have the following form:

    *jar:file:&lt;path-to-jar&gt;!&lt;path-to-file-in-jar&gt;*

Project Jigsaw will use modules as a container for code files and the individual JARs will no longer be available.
This requires a new format so such APIs will instead return *jrt URLs*:

    *jrt:/&lt;module-name&gt;/&lt;path-to-file-in-module&gt;*

Code that uses the instances returned by such APIs to access the file (e.g. with [`URL.getContent`](https://docs.oracle.com/javase/8/docs/api/java/net/URL.html#getContent--)) will continue to work as today.
But if it depends on the *structure* of jar URLs (e.g. by constructing them manually or parsing them), it will fail.

### Removal Of The Endorsed Standards Override Mechanism

Some parts of the Java API are considered *Standalone Technologies* and created outside of the Java Community Process (e.g. [JAXB](https://jaxb.java.net/)).
It might be desirable to update them independently of the JDK or use alternative implementations.
The [endorsed standards override mechanism](http://docs.oracle.com/javase/8/docs/technotes/guides/standards/index.html) allows to install alternative versions of these standards into a JDK.

This mechanism is deprecated in Java 8 and will be removed in Java 9.
Its replacement are [upgradeable modules](http://openjdk.java.net/projects/jigsaw/goals-reqs/03#upgradeable-modules).

If you've never heard about this, you're probably not using it.
Otherwise you might want to verify whether the implementation you are using will be made into an upgradeable module.

### Removal Of The Extension Mechanism

With the [extension mechanism](http://docs.oracle.com/javase/tutorial/ext/) custom APIs can be made available to all applications running on the JDK without having to name them on the class path.

This mechanism is deprecated in Java 8 and will be removed in Java 9.
Some features which are useful on their own will be retained.

If you've never heard about this, you're probably not using it.
Otherwise you might want to check JEP 220 for details.

<admonition type="note">

There are a few more things, that can go wrong - check my [Java 9 migration guide](java-9-migration-guide).

</admonition>

## Preparations For Java 9

Together these changes impose a risk for any large project's transition to Java 9.
One way to assess and reduce it could be an "update spike": Use *jdeps* to identify dependencies on internal APIs.
After fixing these, invest some time to build and run your project with one of the [Java 9 early access builds](https://jdk9.java.net/download/).
Thoroughly test relevant parts of the system to get a picture of possible problems.

Information gathered this way can be returned to the project, e.g. by posting it on the [Jigsaw-Dev mailing list](http://mail.openjdk.java.net/mailman/listinfo/jigsaw-dev).
To quote the (almost) final words of JEP 220:

> It is impossible to determine the full impact of these changes in the abstract.
> We must therefore rely upon extensive internal and—especially—external testing.
> [...] If some of these changes prove to be insurmountable hurdles for developers, deployers, or end users then we will investigate ways to mitigate their impact.

## Reflection & Lookout

We have seen that Project Jigsaw will modularize the Java runtime.
Internal APIs (packages `sun.*` and maybe `com.sun.*`) will be made unavailable and the internal structure of the JRE/JDK will change, which includes folders and JARs.
Following their deprecation in Java 8, the endorsed standards override mechanism and the extension mechanism will be removed in Java 9.

If you want to help your friends and followers to prepare for Java 9, make sure to share this post.

So far we focused on the problematic aspects of Project Jigsaw.
But that should not divert from the exciting and - I think - very positive nature of the planned changes.
After reading the documents, I am impressed with the scope and potential of this upcoming Java release.
While it is likely not as groundbreaking for individual developers as Java 8, it is even more so for everyone involved in building and deploying - especially of large monolithic projects.

As such, I will surely write about Project Jigsaw again - and then with a focus on the good sides.
Stay tuned if you want to read about it.
