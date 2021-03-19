---
title: "Improve Launch Times On Java 13 With Application Class-Data Sharing"
tags: [java-10, java-12, java-13, performance]
date: 2019-09-15
slug: java-application-class-data-sharing
description: "On Java 10+, you can use application class-data sharing to reduce launch times, response time outliers, and memory footprint. By archiving class data with -Xshare:dump and loading it with -Xshare:on, the JVM's class loading workload can be reduced considerably."
searchKeywords: "application class-data sharing"
featuredImage: app-cds
repo: java-x-demo
---

Java's recent releases brought us a steady flow of new features and it's easy to miss one.
Here's a gem that's worth exploring: application class-data sharing (AppCDS).
It allows you to reduce launch times, response time outliers, and, if you run several JVMs on the same machine, memory footprint.
Here's how!

AppCDS is an extension of the [commercial class-data sharing (CDS) feature](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/class-data-sharing.html) that Oracle's JVM contains since Java 8.
Note that the feature evolved over several Java releases (10, 12, and 13).
This post targets the Java 13 variant, but I'll make sure to point out parts that aren't available on everybody's favorite LTS (that would be Java 11).
If you're interested in a little more background, check out the JEPs [310](http://openjdk.java.net/jeps/310), [341](http://openjdk.java.net/jeps/341), and [350](http://openjdk.java.net/jeps/350), which generalized CDS to AppCDS.


## Application Class-Data Sharing In A Nutshell

To execute a class' bytecode, the JVM needs to perform a few preparatory steps.
Given a class name, it looks the class up on disk, loads it, verifies the bytecode, and pulls it into an internal data structure.
That takes some time of course, which is most noticeable when the JVM launches and needs to load at least a couple of hundred, most likely thousands of classes.

The thing is, as long as the application's JARs do not change, this class-data is always the same.
The JVM executes the same steps and comes to the same result every time it runs the app.

Enter application class-data sharing!
The idea behind it is to create this data once, dump it into an archive, and then reuse that in future launches and even share it across simultaneously running JVM instances.

<pullquote>The idea behind AppCDS is to create a class-data archive once and then share it, so that the JVM need not recreate it</pullquote>

Here's the full recipe (but we'll see later that we don't need to do all of that manually):

1. create a list of classes to include in the archive (possibly with `-XX:DumpLoadedClassList`)
2. create an archive with the options `-Xshare:dump` and `-XX:SharedArchiveFile`
3. use the archive with the options `-Xshare:on` and `-XX:SharedArchiveFile`

When launching with an archive, the JVM maps the archive file into its own memory and then has most classes it needs readily available; it doesn't have to muck around with the intricate class-loading mechanism.
The memory region can even be shared between concurrently running JVM instances, which frees up memory that would otherwise be wasted on replicating the same information in each instance.

AppCDS significantly reduces the time the JVM has to spend on class-loading, which is most noticeable during launch.
It also prevents long response times in the case where a user is the first to access a feature that requires a lot of classes that weren't loaded yet.

<pullquote>AppCDS significantly reduces the time the JVM has to spend on class-loading</pullquote>

## Working With A JDK Class-Data Archive

The simplest way to get started with class-data sharing is to limit it to JDK classes, so we'll do that first.
We will then observe that a simple "Hello, World" JAR can be launched in almost half the time.

If you're on Java 12 or later, you don't need to do anything to benefit from this kind of archive.
The JDK comes with one and uses it automatically.
If you want to turn that off, launch your application with `-Xshare:off`.

<pullquote>JDK 12+ comes with a JDK-archive that is enabled by default</pullquote>

The following steps are only necessary on Java 10 and 11.

### Creating A JDK Class-Data Archive

All JDKs since 10 come with a list of classes, which `-Xshare:dump` uses by default, so we can go straight to step 2 and generate the archive:

```shell
$ java -Xshare:dump
```

You might have to execute this command with admin rights because by default the JVM creates the archive file `classes.jsa` in `${JAVA_HOME}/lib/server`.
On Linux, the resulting file is about 18 MB.

### Using A JDK Class-Data Archive

Just as `-Xshare:dump` creates the archive in a default location, `-Xshare:on` reads from that same default, so using the archive is pretty simple:

```shell
$ java -Xshare:on -jar app.jar
```

In fact, it's not even necessary to use `-Xshare:on`.
The option `-Xshare` has a default value `auto` that uses an archive if it finds one (either in the default location or in the location specified with `-XX:SharedArchiveFile`).
Setting sharing to `on` tells the JVM to exit with an error if no archive was found or it was otherwise not able to use it.
In practice `on` may be the better choice in order to fail fast, but in this post, I'll rely on the default.

We can use [unified logging with `-Xlog`](java-unified-logging-xlog) to observe CDS in action by analyzing the class loading log messages:

```shell
$ java
	-Xlog:class+load:file=cds.log
	-jar app.jar
```

The file `cds.log` then contains messages like the following:

```shell
> [0.008s][info][class,load] java.lang.Object source: shared objects file
# [...]
> [0.042s][info][class,load] org.codefx.demo.java10.jvm.acds.HelloAppCDS source:
	file:/.../app.jar
```

As you can see, `Object` was loaded from the "shared objects file", which is another term for the archive, whereas `HelloAppCDS` isn't.
A simple count of lines shows that for an app with a single class 585 classes are loaded from the archive and 3 aren't.

That implies that some JDK classes could not be loaded from the archive and that's exactly right.
There are some specific conditions under which it is not possible to archive a class' data, so the JVM won't embed it and will instead end up loading it at run time as usual.

<pullquote>There are always some classes whose data can not be archived</pullquote>

### Launch Time Measurements

A crude way to measure the performance improvement is to simply time the execution of the entire demo app while toggling `-Xshare` between `off` and `auto` (its default):

```shell
$ time java -Xshare:off -jar app.jar
> Hello, application class-data sharing!
>
> real    0m0.078s
> user    0m0.094s
> sys     0m0.012s

$ time java -jar app.jar
> Hello, application class-data sharing!
>
> real    0m0.043s
> user    0m0.053s
> sys     0m0.014s
```

The interesting bit is `real`, which gives the wall-clock time it took to run the app.
Without class-data sharing those numbers vary between 70 ms and 85 ms (need I add "on my machine"?), with sharing they land between 40 ms and 50 ms.
The exact numbers don't really matter, but you can see that there's quite some potential.

Note, though, that this is the maximum performance gain you are going to get for a JDK archive.
The more classes the application comes with, the lower becomes the share of JDK classes and hence the relative effect of loading them faster.
To scale this effect to large applications, you need to include their classes in the archive, so let's do that next.

## Working With An Application Class-Data Archive

To actually share data for application classes as opposed to just JDK classes, we obviously need an archive that includes application code.
Before Java 13, creating and using such an archive had to follow the same logic as for JDK classes.
We'll cover that first before discussing what 13 brings to the table.

### Creating A List Of Application Classes

With the pre-13 approach, we can not skip the step to come up with the list of classes to include in the archive.
There are at least two ways to create that list: You can either do it manually or ask the JVM to do it for you.
I'll tell you about the latter and once we have a file in hand, you will see how you might have generated it by hand.

To have the JVM create the list, run the application with the `-XX:DumpLoadedClassList` option:

```shell
$ java
	-XX:DumpLoadedClassList=classes.lst
	-jar app.jar
```

The JVM will then dutifully record all loaded classes.
If you want to include just the classes you need to launch, exit the app right after that.
If, on the other hand, you want to include classes for specific features, you should make sure they are used at least once.

In the end you get a file `classes.lst` that contains the slash separated name of each class that was loaded.
Here are the first few lines:

```shell
java/lang/Object
java/lang/String
java/io/Serializable
java/lang/Comparable
java/lang/CharSequence
```

As you can see, there's nothing special about this file and you could easily generate it by other means.

### Creating An Application Class-Data Archive

The second step is to actually create the archive.
We do that much like before but have to mention the list of classes with `-XX:SharedClassListFile`.
And because this archive is application-specific, we don't want it in `${JAVA_HOME}`, so we define the location with `-XX:SharedArchiveFile`:

```shell
$ java
	-Xshare:dump
	-XX:SharedClassListFile=classes.lst
	-XX:SharedArchiveFile=app-cds.jsa
	--class-path app.jar
```

Note that we do not launch the application with `-jar`.
Instead we just define the class path with all the JARs the application needs.
We will see in a minute why the path's details are of the utmost importance and also why you can't use wildcards like `lib/*` or exploded JARs like `target/classes`.

Depending on the size of the class list, this step might take a while.
When it's done, your archive, in this case `app-cds.jsa`, is ready to be used.

### Creating An Archive Dynamically When The JVM Exits

On Java 13 and later, the previous two steps can be combined and handed over to the JVM during a normal program run - this is called *dynamic* application class-data sharing.
By adding the command line option `-XX:ArchiveClassesAtExit`, the JVM will record all loaded application classes and place them into the specified archive:

<pullquote>Dynamic AppCDS creates an archive during a regular program run</pullquote>

```shell
$ java
	-XX:ArchiveClassesAtExit=app-cds.jsa
	-jar app.jar
```

If the JVM doesn't crash, it creates the class-data archive `app-cds.jsa` on shutdown.
Such a dynamically generated archive only contains application classes (you'll notice it's a few MBs smaller) because it builds on top of the default Java-class archive that the JDK contains since version 12.
As a user, that doesn't concern you, though - the JVM manages this on its own.

### Using An Application Class-Data Archive

Using the archive is pretty straightforward.
We just need to point to the archive:

```shell
$ java
	-XX:SharedArchiveFile=app-cds.jsa
	-jar app.jar
```

If we analyze the class loading log messages as before, we see that with the application class `HelloAppCDS` could be loaded from the archive:

```shell
> [0.049s][info][class,load] org.codefx.demo.java10.jvm.acds.HelloAppCDS source:
	shared objects file
```

As mentioned earlier, if the archive was generated dynamically, it links back to the Java-class archive, which means all Java classes are loaded from there.
Handcrafted archives don't link to the JDK's archive, though, so you must include all JDK classes you need in your archive or they will be loaded through the regular class-loading mechanism.

In case you're wondering, you can't use an archive to read from and write to it after the same run:

```shell
$ java
	-XX:SharedArchiveFile=app-cds.jsa
	-XX:ArchiveClassesAtExit=app-cds.jsa
	-jar app.jar
> Error occurred during initialization of VM
> Cannot have the same archive file specified for
> -XX:SharedArchiveFile and -XX:ArchiveClassesAtExit
```

### Heed The Class Path Content

What are the two biggest challenges in software development?

1. naming
2. cache invalidation
3. off-by-one errors

Cheap joke, I know, but relevant because the class-data archive is essentially a cache and so we need to ask ourselves under which circumstances it becomes stale and how that can be detected.

<pullquote>The class-data archive is a cache</pullquote>

It is obviously a problem if a JAR was replaced and now contains classes that differ from the archive's (say, someone drops an updated dependency into the app's `lib` folder).
And because the class path is always scanned linearly, even just reordering the same artifacts can change the app's run-time behavior.
In summary, the archive is no longer a correct representation of the class path, if the latter doesn't contain the same artifacts in the same order as when the archive was created.

To have a decent chance at detecting a class path mismatch, the JVM embeds the string that lists the class path content in the archive when creating it.
When you launch the app with the archive, the JVM compares the actual class path with the embedded one and only launches if the latter is a prefix of the former.

<pullquote>The class path used to launch the app must have the archive's path as a prefix</pullquote>

That means the class path used to launch the app must first list all elements of the archive's path in the same order, but can then append more JARs as it sees fit.
This is of course by no means fool proof, but should detect a decent chunk of problematic situations.

The more specific the class path that is used to create the archive, the more reliably can it be used to "invalidate" the cache / archive.
And that's why you can use neither wild cards nor exploded JARs when creating the archive.

(If you look closely, you will notice that in my examples, I create the archive with `--class-path app.jar` but launch without class path because I use `-jar app.jar`.
Wait, a non-empty class path can hardly be the prefix on an empty class path.
It works nonetheless because the JAR specified with `-jar` is implicitly added to the class path.)

By the way, if you're using IntelliJ IDEA, you have already seen this mechanism in practice.
When executing your code with Java 12, you'll see this message:

```none
OpenJDK 64-Bit Server VM warning:
Sharing is only supported for boot loader classes
because bootstrap classpath has been appended
```

The JVM loads some of the JDK classes with the bootstrap class loader and the rest with the system class loader, but includes all of them in its default archive.
When IntelliJ executes your project, it tells the JVM to load some code with the bootstrap class loader by appending to that class path (second part of the message).
Now, that means that the portion of the archive that contains classes loaded by the system class loader is potentially invalidated and so the JVM partially deactivates sharing (first part of the message).

We've been talking an awful lot about the class path - what's with [the module path](java-module-system-tutorial#module-path)?
Can you put code from [JPMS modules](tag:j_ms) into the archive?
Unfortunately, not - from JEP 310:

> In this release, CDS cannot archive classes from user-defined modules (such as those specified in `--module-path`).
We plan to add that support in a future release.

### Launch Time Measurements

For a simple "Hello, World" application there is of course no performance boost of AppCDS over CDS because loading one class more or less from the archive has no measurable impact.
So I took application class-data sharing for a ride with a large desktop application.

I focused on the launch, which loads about 25'100 classes and takes about 15 seconds.
During that time, there's a lot more going on that just fetching classes, but at least there are no network operations to skew the results.

The archive contained 24'212 classes (so about 900 could not be included) and has roughly 250 MB.
Using it to run the application brought the launch time down by about 3 seconds (20 %), which I felt quite good about.
Your mileage will vary, of course, so you have to do this yourself to know whether it's worth the effort for your app.

## Reflection

Application class-data sharing can be used in various ways:

-   On Java 12+, Java classes are automatically loaded from the archive included in the JDK
-   On Java 13+, class-data archives can be automatically created by the JVM on shutdown with the command line option `-XX:ArchiveClassesAtExit=${ARCHIVE}`; to use it, add `-XX:SharedArchiveFile=${ARCHIVE}` on launch
-   On Java 10+, it is possible to hand-craft archives in three steps:

```shell
# create a list of classes to include in the archive
$ java
	-XX:DumpLoadedClassList=classes.lst
	-jar app.jar

# create the archive
$ java
	-Xshare:dump
	-XX:SharedClassListFile=classes.lst
	-XX:SharedArchiveFile=app-cds.jsa
	--class-path app.jar

# use the archive
$ java
	-XX:SharedArchiveFile=app-cds.jsa
	-jar app.jar
```

Keep in mind that the class path used to launch the application must have the one used to create the archive as a prefix and that you can't use wildcards or exploded JARs for the latter.
Your launch time improvements depend a lot on your application, but anything between a couple and almost 50 % is possible.
Finally, if you have any problems, [use `-Xlog:class+load`](java-unified-logging-xlog) to get more information.
