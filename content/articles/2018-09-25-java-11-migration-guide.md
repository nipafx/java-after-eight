---
title: "All You Need To Know For Migrating To Java 11"
tags: [java-11, migration]
date: 2018-09-25
slug: java-11-migration-guide
description: "Migrating from Java 8 to Java 11? This has got you covered: licensing, long-term support, preparations, version requirements, migration challenges, and more."
intro: "Migrating from Java 8 to Java 11? Then this post has got you covered. It discusses licensing, long-term support, update preparations, version requirements, common migration challenges, and more."
searchKeywords: "Java 11 migration"
featuredImage: java-11-migration
repo: java-9-migration
---

[Java 11](http://jdk.java.net/11/) is released today!
Formally it marks the end of a monumental shift in the Java ecosystem.
With the challenges of migrating from Java 8 onto a modular and flexible JDK, with the six-month release cycle, the new licensing and long-term support models, we've entered a new era!
Now the code bases have to follow and many projects will move from Java 8 directly to Java 11.
If that describes yours, you've come to the right place - this migration guide tells you everything you need to know when moving from Java 8 to Java 11.

(**Small aside**: If you're interested in Java 11 features, check out my posts on [the new HTTP/2 client](java-http-2-api-tutorial) and [its reactive use](java-reactive-http-2-requests-responses), [scripting with Java](scripting-java-shebang), and [the eleven hidden gems in Java 11](java-11-gems).)

We'll start with a super-quick tour through the new release cadence, licensing, and support before discussing how to prepare a migration (TL;DR: update all the things!) and finally, how to overcome the four most common hurdles (if you already [migrated to Java 9](java-9-migration-guide), you can skip most of that).
Note that we're talking [*migration*](tag:migration), not *modularization* (that's not required and should be a separate step), so we won't be creating any modules.

## On Releases, JDKs, And Licenses

This may seem like boring stuff, but the six-month release cycle, the commercialization of Oracle's JDK, and the open question of long-term support for OpenJDK probably has more impact on your project than the technical challenges of moving to Java 11.
So let's discuss this, but be quick about it - more details in the links.

⇝ [Java Next](https://vimeo.com/289852355) (talk at JavaZone 2018)

### New Release Cadence

This is the most well-known change, so I'll keep it short:

-   new major release every six months (March and September)
-   two minor updates for each (one and four months later)

⇝ [Radical new plans for Java - CodeFX Weekly \#34](https://medium.com/codefx-weekly/radical-new-plans-for-java-5f237ab05b0)

### OpenJDK Is The New Default

Before September 2018, Oracle's JDK (and before that Sun's JDK) was richer in features and perceived to be more stable and performant (although that was mostly an illusion) - it was hence the default choice for most of the ecosystem.
OpenJDK was much less widely used but that will change with Java 11.

Oracle worked hard to make Oracle JDK 11 and OpenJDK 11 almost identical from a technical point of view - so much so that the most important difference is the license file they ship with.
Oracle further pushes developers towards OpenJDK by making their branded JDK commercial, meaning you can't use it in production without paying Oracle from day one after its release (you can use it for development and testing).

<pullquote>Oracle JDK is fully commercial</pullquote>

As a consequence, [OpenJDK](http://jdk.java.net/11/) will become the new default - with full feature set, prime performance, and a free license (GPL+CE) it's a great choice.
On the next rung come, side by side, Oracle and other vendors with their OpenJDK variants for which they sell long-term support.

⇝ [Time to look beyond Oracle's JDK](https://blog.joda.org/2018/09/time-to-look-beyond-oracles-jdk.html)

⇝ [Oracle JDK Releases for Java 11 and Later](https://blogs.oracle.com/java-platform-group/oracle-jdk-releases-for-java-11-and-later)

### Long-Term Support

Oracle ships OpenJDK builds at [jdk.java.net](http://jdk.java.net) and, as mentioned, publishes two updates for each major version.
So what happens after six months if you want to stay on a specific major version while still receiving updates with security and bug fixes?
Two choices:

-   pay someone for commercial support
-   hope for free support of OpenJDK

As to commercial support, there are various vendors that have you covered for the specific versions they mark as long-term support (the focus seems to be on 11/17/23/...):

-   [Oracle](https://www.oracle.com/java/java-se-subscription.html)
-   [IBM](https://developer.ibm.com/javasdk/support/lifecycle/)
-   [RedHat](https://access.redhat.com/articles/1299013)
-   [Azul](https://www.azul.com/products/azul_support_roadmap/)

Regarding OpenJDK, there are [very promising discussions](http://mail.openjdk.java.net/pipermail/jdk-dev/2018-August/001823.html) on the mailing list that suggest that there will be at least four years of public updates to the same versions.
Most likely, each LTS version will get a steward that manages the updates and it looks like it may be Red Hat for Java 11.
That covers the sources, but where can we get the final binaries from?
[AdoptOpenJDK](http://adoptopenjdk.net/) is gearing up to continuously build the various OpenJDK versions for all kinds of platforms.

<pullquote>There will likely be free LTS for OpenJDK</pullquote>

Put together, we'd get free OpenJDK LTS, organized by companies that are well-known in the Java community and continuously built by AdoptOpenJDK.
That would be awesome!

⇝ [No Free Java LTS Version? - CodeFX Weekly \#56](https://medium.com/codefx-weekly/no-free-java-lts-version-b850192745fb)

⇝ [Java is still available at zero-cost](https://blog.joda.org/2018/08/java-is-still-available-at-zero-cost.html)

⇝ [Java is still free](https://docs.google.com/document/d/1nFGazvrCvHMZJgFstlbzoHjpAVwv5DEdnaBr_5pKuHo)

### Update Nov 2018: Amazon Corretto

Out of left field, a new player entered the game!
Amazon now offers [Amazon Corretto](https://aws.amazon.com/corretto/), a GPL+CE-licensed OpenJDK build with free long-term support.
Here are the key information:

-   based on OpenJDK, plus security/performance/stability/bug fixes implemented by Amazon
-   support for Linux, macOS, Windows
-   free to use, [GPL+CE](https://openjdk.java.net/legal/gplv2+ce.html) licensed
-   long-term support for Java 8 until at least 2023
-   long-term support for Java 11 starting in 2019 until at least 2024
-   quarterly updates with possible intermittent urgent fixes

Regarding contributions to OpenJDK, [the FAQ](https://aws.amazon.com/corretto/faqs/) says, that "Amazon started contributing to OpenJDK in 2017 and \[...\] plan\[s\] to increase contributions in both number and complexity." I guess/hope that means that Amazon will work to upstream their fixes into OpenJDK, so they become available for everybody.
If not, the sources are [available on GitHub](https://github.com/corretto).

If I didn't miss something, this implies that Amazon will merge Oracle's fixes from the main development line into the freely accessible Corretto 11 code base.
Even if they don't then work to include those merges in the OpenJDK 11 repo, others should be able to do that fairly easily.
This makes a free, long-term supported, community-driven OpenJDK 11 all the more likely.
Very cool!

## Preparing Your Migration

Here you are: With your favorite vendor's JDK installed (and maybe some LTS in the back pocket), you want your Java 8 project to work on Java 11.
I know you're ready to roll but before we go in, we need to discuss how to best approach and prepare the migration.

### Short Or Long-Lived?

When starting to migrate from Java 8 to Java 11 (or later), the first thing you have to answer is whether you can and want to do this in one fell swoop or over a longer period of time.
If the project causes little trouble and you are raising your minimum requirements, then go for a quick migration where you use the new version for the entire build process, including the target for compilation.
All that's left is to fix any problems that may pop up.

If you don't want to raise the minimum version requirement or the project is too large to migrate in a single sitting or a short-lived branch, I recommend the following approach:

<pullquote>Don't create a long-lived migration branch</pullquote>

-   Don't create a long-lived branch for the migration - instead do it on the regular development branch.
This way, you're not facing merge conflicts and can be sure that your colleagues' changes are always also built on Java 11.
-   Configure your continuous integration server to run the build once in its common configuration and once on Java 11.
-   Learn about your build tool's support for configuration specific to individual Java versions.
(In Maven, that would be [profiles](maven-on-java-9#configuring-the-build-for-java-8-and-java-9).) This way you can keep the old build running while adding required configuration for the new version.
-   Try to keep the version-specific configuration to a minimum.
For example, prefer updating dependencies over adding command line flags (more on that below).

This way you can take all the time you need to guarantee that your project works on Java 8 as well as on Java 11 (or later).
Whether you want to keep building on both (or more) versions or flip the switch once you're done depends on the project's minimum Java requirement.
When you're eventually leaving Java 8 behind for good, don't forget to merge the version-specific bits into the default configuration to reduce complexity.

⇝ [Planning Your Java 9 Update](planning-your-java-9-update) (fully applies to Java 11)

⇝ [Maven on Java 9 - Six Things You Need To Know](maven-on-java-9) (fully applies to Java 11)

### Update All The Things

The first rule of moving to Java 11 is ~~you do not talk ...~~ to update all the things.
Your IDE, your build tool, its plugins, and, most importantly, your dependencies.
You don't *have* to do all of these updates in advance, but if you can, you absolutely should - it will very likely get you past some hurdles you can then stay blissfully unaware of.

Here are the recommended minimum versions for a few tools:

-   **IntelliJ IDEA**: [2018.2](https://blog.jetbrains.com/idea/2018/06/java-11-in-intellij-idea-2018-2/)
-   **Eclipse**: Photon 4.9RC2 with [Java 11 plugin](https://marketplace.eclipse.org/content/java-11-support-eclipse-photon-49)
-   **Maven**: 3.5.0
	-   **compiler plugin**: 3.8.0
	-   **surefire** and **failsafe**: 2.22.0
-   **Gradle**: [5.0](https://docs.gradle.org/5.0/release-notes.html#java-11-runtime-support)

Some dependencies that you should keep an eye on (and versions that are known to work on Java 11):

-   Anything that operates on bytecode like **ASM** (7.0), **Byte Buddy** (1.9.0), **cglib** (3.2.8), or **Javassist** (3.23.1-GA).
Since Java 9, the bytecode level is increased every six months, so you will have to update libraries like these pretty regularly.
-   Anything that uses something that operates on bytecode like **Spring** (5.1), **Hibernate** (unknown), **Mockito** (2.20.0), and many, many other projects.

The second bullet is not very helpful in its generality, but it's the unfortunate truth: Many powerful projects work with bytecode under the hood.
It helps to develop an eye for identifying problems related to that.
Some (obvious?) tips:

-   stack traces ending in bytecode manipulation libraries
-   errors or warnings complaining about the bytecode level
-   errors or warnings mumbling about "unknown (constant pool) entries"

If you don't update in advance, it should still be the first action you take when encountering a problem with any specific tool or dependency.
Either way, you may occasionally encounter problems even though your dependencies are up to date.
In that case, have a look at the precise artifact causing the problem - chances are it's a *transitive dependency*, in which case you should look into updating it separately.

With an older version of Hibernate, for example, it was necessary to update Javassist to work on Java 11:

```xml
<dependency>
	<groupId>org.hibernate</groupId>
	<artifactId>hibernate-core</artifactId>
	<!-- LOOK OUT: YOU SHOULD USE A NEWER VERSION! -->
	<version>5.2.12.Final</version>
</dependency>
<dependency>
	<!-- update Hibernate dependency on Javassist
			from 3.20.0 to 3.23.1 for Java 11 compatibility -->
	<groupId>org.javassist</groupId>
	<artifactId>javassist</artifactId>
	<version>3.23.1-GA</version>
</dependency>
```

Likewise, with the outdated version 3.7.0 of the Maven compiler plugin, its ASM dependency needed updating:

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-compiler-plugin</artifactId>
	<!-- LOOK OUT: YOU SHOULD USE 3.8.0! -->
	<version>3.7.0</version>
	<configuration>
		<release>${java.version}</release>
	</configuration>
	<dependencies>
		<dependency>
			<!-- update compiler plugin dependency on ASM
					for Java 11 compatibility -->
			<groupId>org.ow2.asm</groupId>
			<artifactId>asm</artifactId>
			<version>6.2</version>
		</dependency>
	</dependencies>
</plugin>
```

Unfortunately, not all projects are well-maintained or were even discontinued, in which case you need to look for alternatives.
Examples are FindBugs (use [SpotBugs](https://spotbugs.github.io/) instead), Log4j 1 (use [Log4J 2](https://logging.apache.org/log4j/2.x/)), and Cobertura (use [JaCoCo](https://github.com/jacoco/jacoco)).

<pullquote>Only dive into the problem if updating is impossible or doesn't help</pullquote>

Only if the problem lies in your own code or such updates/replacements don't help or aren't possible, does it make sense to dive into the actual problem.

### A Word On The Module System

I'm sure you've heard about [the Java Platform Module System (JPMS)](tag:j_ms) that was introduced in Java 9.
Since it's causing most of the compatibility challenges you're going to face during a migration from Java 8, it definitely helps a lot to understand its basics - for example, by reading [this fine module system tutorial](java-module-system-tutorial) (*cough*) or [my book](https://www.manning.com/books/the-java-module-system?a_aid=nipa&a_bid=869915cb) (*cough cough*).
But keep in mind that you are not required to create modules to have your code run on Java 9 or later!

The class path is here to stay and if your code or its dependencies don't do anything forbidden (more on that later), you can expect it to Just Work™ on Java 9, 10, or 11 exactly as it did on 8 - modules are no requirement and so this post does not address *modularization*, just *migration*.

<pullquote>You don't need modules to run on Java 9+</pullquote>

## Migrating From Java 8 To Java 11

We're done preparing, time to go!
Let's see which problems you may expect on Java 11 and how to fix them.

### Removal Of Java EE Modules

There used to be a lot of code in Java SE that was actually related to Java EE.
It ended up in six modules that were deprecated for removal in Java 9 and removed from Java 11.
Here are the removed technologies and packages:

-   the JavaBeans Activation Framework (JAF) in `javax.activation`
-   CORBA in the packages `javax.activity`, `javax.rmi`, `javax.rmi.CORBA`, and `org.omg.*`
-   the Java Transaction API (JTA) in the package `javax.transaction`
-   JAXB in the packages `javax.xml.bind.*`
-   JAX-WS in the packages `javax.jws`, `javax.jws.soap`, `javax.xml.soap`, and `javax.xml.ws.*`
-   Commons Annotation in the package `javax.annotation`

#### Symptoms

Here's a compile error for a class using `JAXBException` from the *java.xml.bind* module:

```shell
error: package javax.xml.bind does not exist
import javax.xml.bind.JAXBException;
					 ^
```

If you get it past the compiler but forget to massage the run time, you'll get a `NoClassDefFoundError`:

```shell
Exception in thread "main" java.lang.NoClassDefFoundError: javax/xml/bind/JAXBException
	at monitor.Main.main(Main.java:27)
Caused by: java.lang.ClassNotFoundException: javax.xml.bind.JAXBException
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:582)
	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:185)
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:496)
	... 1 more
```

#### Fixes

Add third-party dependencies that contain the classes you need.
The easiest way to do that is to stick to the reference implementations (given as Maven coordinates without version - use the most current ones):

-   JAF: with [*com.sun.activation:javax.activation*](https://search.maven.org/search?q=g:com.sun.activation%20AND%20a:javax.activation&core=gav)
-   CORBA: there is currently no artifact for this
-   JTA: [*javax.transaction:javax.transaction-api*](https://search.maven.org/search?q=g:javax.transaction%20AND%20a:javax.transaction-api&core=gav)
-   JAXB: [*com.sun.xml.bind:jaxb-impl*](https://search.maven.org/search?q=g:com.sun.xml.bind%20AND%20a:jaxb-impl&core=gav)
-   JAX-WS: [*com.sun.xml.ws:jaxws-ri*](https://search.maven.org/search?q=g:com.sun.xml.ws%20AND%20a:jaxws-ri&core=gav)
-   Commons Annotation: [*javax.annotation:javax.annotation-api*](https://search.maven.org/search?q=g:javax.annotation%20AND%20a:javax.annotation-api&core=gav)

For more details, sources, and other recommendations, see [this StackOverflow answer](https://stackoverflow.com/a/48204154/2525313).

### Illegal Access To Internal APIs

One of the module system's biggest selling points is strong encapsulation.
It makes sure non-public classes as well as classes from non-exported packages are inaccessible from outside the module.
First and foremost, this of course applies to the platform modules shipped with the JDK, where only `java.*` and `javax.*` packages are fully supported.
Most `com.sun.*` and `sun.*` packages, on the other hand, are internal and hence inaccessible by default.

While the Java 11 compiler behaves exactly as you would expect and prevents illegal access, the same is not true for the run time.
To offer a modicum of backwards compatibility it eases migration and improves the chances of applications built on Java 8 to run on Java 11 by granting access to internal classes.
If reflection is used for the access, a warning is emitted.

#### Symptoms

During compilation against Java 11 you may see compile errors similar to the following:

```shell
error: package com.sun.imageio.plugins.jpeg is not visible
import com.sun.imageio.plugins.jpeg.JPEG;
					          ^
  (package com.sun.imageio.plugins.jpeg is declared
  in module java.desktop, which does not export it)
```

Warnings emitted for reflection look as follows:

```shell
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by j9ms.internal.JPEG
	(file:...) to field com.sun.imageio.plugins.jpeg.JPEG.TEM
WARNING: Please consider reporting this
	to the maintainers of j9ms.internal.JPEG
WARNING: Use --illegal-access=warn to enable warnings
	of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
# here's the reflective access to the static field com.sun.imageio.plugins.jpeg.JPEG.TEM
```

#### Fixes

The most obvious and sustainable fix for dependencies on internal APIs is to get rid of them.
Replace them with maintained APIs and you paid back some high-risk technical debt.

If that can't be done for whatever reason, the next best thing is to acknowledge the dependencies and inform the module system that you need to access it.
To that end you can use two command line options:

-   The option `§--add-exports module/package=$readingmodule` exports `§$package` of *$module* to *$readingmodule*.
Code in *$readingmodule* can hence access all public types in `§$package` but other modules can not.
When setting *$readingmodule* to `§ALL-UNNAMED`, all code from the class path can access that package.
During a migration to Java 11, you will always use that placeholder (you will have to change it when you modularize).
The option is available for the `java` and `javac` commands.
-   This covers access to public members of public types but reflection can do more than that: With the generous use of `setAccessible(true)` it allows interaction with non-public classes, fields, constructors, and methods (sometimes called *deep reflection*), which even in exported packages are still encapsulated.
The `java` option `§--add-opens` uses the same syntax as `§--add-exports` and opens the package to deep reflection, meaning all of its types and their members are accessible regardless of their visibility modifiers.

You obviously need `§--add-exports` to appease the compiler but using `§--add-exports` and `§--add-opens` for the run time has advantages as well:

1. the run time's permissive behavior will change in future Java releases, so you have to do that work at some point anyway
2. `§--add-opens` makes the warnings for illegal reflective access go away
3. as I will show in a minute, you can make sure no new dependencies crop up by making the run time actually enforce strong encapsulation

⇝ [Five Command Line Options To Hack The Java Module System](five-command-line-options-hack-java-module-system)

#### Going Further

Compiling against Java 11 helps hunting down dependencies on internal APIs in the project's code base.
But the libraries and frameworks your project uses are just as likely to make trouble.

JDeps is the perfect tool to find compile dependencies on JDK-internal APIs in your project *and* your dependencies.
If you're not familiar with it, I've written [a tutorial](jdeps-tutorial-analyze-java-project-dependencies) that gets you started.
Here's how to use it for the task at hand:

```shell
jdeps --jdk-internals -R --class-path 'libs/*' $project
```

Here, `§libs` is a folder containing all of your dependencies and `§$project` your project's JAR.
Analyzing the output is beyond this article's scope but it's not that hard - you'll manage.

Finding reflective access is a little tougher.
The run time's default behavior is to warn you once for the first illegal access to a package, which is insufficient.
Fortunately, there's the `§--illegal-access=$value` option, where `§$value` can be:

-   `§permit`: Access to all JDK-internal APIs is permitted to code on the class path.
For reflective access, a single warning is issued for the *first* access to each package.
(Default in Java 9, but [will be removed in a future release](http://mail.openjdk.java.net/pipermail/jigsaw-dev/2017-June/012841.html).)
-   `§warn`: Behaves like `§permit` but a warning is issued for *each* reflective access.
-   `§debug`: Behaves like `§warn` but a stack trace is included in each warning.
-   `§deny`: The option for those who believe in strong encapsulation: All illegal access is forbidden by default.

Particularly `§deny` is very helpful to hunt down reflective access.
It is also a great default value to set once you've collected all required `§--add-exports` and `§--add-opens` options.
This way, no new dependencies can crop up without you noticing it.

### Removal Of Deprecated APIs and JavaFX

Since Java 9, the `@Deprecated` annotation got a Boolean attribute: `forRemoval`.
If `true`, the deprecated element is going to be removed as soon as the next major release.
That's mildly shocking - in the past `@Deprecated` just meant yellow squiggly lines.

#### Removed Classes and Methods

Here are some of the more common classes and methods that were removed between Java 8 and 11:

-   `sun.misc.Base64` (use `java.util.Base64`)
-   `com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel`
	(use `javax.swing.plaf.nimbus.NimbusLookAndFeel`)
-   on `java.util.LogManager`, `java.util.jar.Pack200.Packer`/`Unpacker`:
	methods `addPropertyChangeListener` and `removePropertyChangeListener`
-   on `java.lang.Runtime`: methods `getLocalizedInputStream` and `getLocalizedOutputStream`
-   various methods on `SecurityManager`

⇝ [JDK 9 Release Notes - Removed APIs, Features, and Options](https://www.oracle.com/technetwork/java/javase/9-removed-features-3745614.html)

⇝ [JDK 10 Release Notes - Removed Features and Options](https://www.oracle.com/technetwork/java/javase/10-relnote-issues-4108729.html#Removed)

#### Heeding Deprecation Warnings

To make it easier to keep up with deprecation warnings, I recommend using the command line tools `§jdeps` and `§jdeprscan`.
They work on class files and JARs and you can find them in your JDK's `§bin` folder.
The former is a multi-purpose dependency analysis tool while the latter focuses on reporting the use of deprecated APIs, highlighting those that will be removed.

⇝ [A JDeps Tutorial - Analyze Your Project’s Dependencies](jdeps-tutorial-analyze-java-project-dependencies)

⇝ [Java Platform, Standard Edition Tools Reference - jdeprscan](https://docs.oracle.com/javase/10/tools/jdeprscan.htm#JSWOR-GUID-2B7588B0-92DB-4A88-88D4-24D183660A62)

#### JavaFX

Then there's JavaFX.
It was never part of Java SE (i.e.
The Standard™) and few OpenJDK variants shipped with it.
For a while, Oracle seemed to push JavaFX and so they included it in their JDK, but that dwindled out and with Oracle aligning its JDK with OpenJDK, they no longer ship JavaFX.
In fact, from Java 11 on, you will have a hard time finding any JDK that ships with JavaFX.

Don't worry, though, the future is bright.
[OpenJFX](https://openjfx.io/), the project behind JavaFX, pulled the entire UI framework into their own artifacts that you simply add as a regular dependency.
You can download them from [Gluon](https://gluonhq.com/products/javafx/) or even [Maven Central](https://search.maven.org/search?q=g:org.openjfx%20AND%20a:javafx&core=gav).

### Casting To URL Class Loader

Java 9 and the module system improved the platform's class loading strategy, which is implemented in a new type and in Java 11 the application class loader is of that type.
That means it is not a `URLClassLoader`, anymore, so the occasional `(URLClassLoader) getClass().getClassLoader()` or `(URLClassLoader) ClassLoader.getSystemClassLoader()` sequences will no longer execute.
This is another typical example where Java 11 is backwards compatible in the strict sense (because that it's a `URLCassLoader` was never specified) but which can nonetheless cause migration challenges.

#### Symptoms

This one is very obvious.
You'll get a `ClassCastException` complaining that the new `AppClassLoader` is no `URLClassLoader`:

```shell
Exception in thread "main" java.lang.ClassCastException:
	java.base/jdk.internal.loader.ClassLoaders$AppClassLoader
	cannot be cast to java.base/java.net.URLClassLoader
		at monitor.Main.logClassPathContent(Main.java:46)
		at monitor.Main.main(Main.java:28)
```

#### Fixes

The class loader was probably cast to access methods specific to `URLClassLoader`.
If so, you might have to face some serious changes.

If you want to access the class path content, check the system property `§java.class.path` and parse it:

```java
String pathSeparator = System
	.getProperty("path.separator");
String[] classPathEntries = System
	.getProperty("java.class.path")
	.split(pathSeparator);
```

If you've used the `URLClassLoader` to dynamically load user provided code (for example as part of a plugin infrastructure) by appending to the class path, then you have to find a new way to do that as it can not be done with Java 11.
You should instead consider creating a new class loader for that.
This has the added advantage that you'll be able to get rid of the new classes as they are not loaded into the application class loader.
You should also read up on [layers](https://docs.oracle.com/javase/9/docs/api/java/lang/ModuleLayer.html) - they give you a clean abstraction for loading an entirely new module graph.

Beyond that, your chances to do a migration with only small changes are slim.
The only supported (and hence accessible) super types of the new `AppClassLoader` are [`SecureClassLoader` and `ClassLoader`](https://docs.oracle.com/javase/9/docs/api/java/security/SecureClassLoader.html) and only few methods were added here in 9.
Still, have a look, they might do what you're looking for.

## Summary

As executive summary:

-   background:
	-   new release every six months
	-   pick OpenJDK by default
	-   assume that there will be free LTS, otherwise pay a commercial vendor
-   preparations:
	-   avoid long-lived branches
	-   update all the things
	-   keep the module system in mind
-   challenges
	-   replace Java EE modules with third-party implementations
	-   if absolutely necessary, use `§--add-exports` or `§--add-opens` to gain access to internal APIs
	-   heed deprecation warnings as classes and methods will be removed
	-   add JavaFX as a regular dependency
	-   don't cast the application class loader to `URLClassLoader`

Further reading:

-   More migration details:
	-   [Planning Your Java 9 Update](planning-your-java-9-update) (fully applies to Java 11)
	-   [Java 9 Migration Guide: The Seven Most Common Challenges](java-9-migration-guide) (if you have problems not covered here)
-   On the module system:
	-   [Code-First Java Module System Tutorial](java-module-system-tutorial)
	-   [Five Command Line Options To Hack The Java Module System](five-command-line-options-hack-java-module-system)
-   Features in Java 11:
	-   [HTTP/2 client](java-http-2-api-tutorial) and [its reactive use](java-reactive-http-2-requests-responses)
	-   [Single-Source-File Execution And Scripting](scripting-java-shebang)
	-   [Eleven Hidden Gems In Java 11](java-11-gems)
	-   [Improve Launch Times On Java 10 With Application Class-Data Sharing](java-10-var-type-inference)
	-   [90 New Features (and APIs) in JDK 11](https://www.azul.com/90-new-features-and-apis-in-jdk-11/)
-   Tools on Java 9 and later:
	-   [Maven on Java 9 - Six Things You Need To Know](maven-on-java-9) (fully applies to Java 11)
	-   [A JDeps Tutorial - Analyze Your Project’s Dependencies](jdeps-tutorial-analyze-java-project-dependencies)
	-   [Java Platform, Standard Edition Tools Reference - jdeprscan](https://docs.oracle.com/javase/10/tools/jdeprscan.htm#JSWOR-GUID-2B7588B0-92DB-4A88-88D4-24D183660A62)

