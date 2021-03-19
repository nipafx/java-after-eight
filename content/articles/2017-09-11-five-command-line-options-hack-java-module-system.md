---
title: "Five Command Line Options To Hack The Java Module System"
tags: [java-9, j_ms]
date: 2017-09-11
slug: five-command-line-options-hack-java-module-system
description: "Get your code running on the Java 9 Module System with the command line options `--add-exports`, `--add-opens`, `--add-modules`, `--add-reads`, and `--patch-module`."
searchKeywords: "command line options"
featuredImage: java-9-command-line-options
inlineCodeLanguage: shell
repo: java-9-migration
---

The [Java Platform Module System](tag:j_ms) (JPMS) not only comes with [an entire set of new rules to abide by](java-module-system-tutorial), it also introduces a host of command line options to break them.
Whether you need to access internal APIs, add unforeseen modules, or extend modules with classes of your own, they have you covered.
In this post I want to go over the five most important command line options that you will need to get your project to compile, test, and run in the face of [various migration challenges](java-9-migration-guide).

Beyond presenting a few specific options, I close with some general thoughts on command line options and particularly [their pitfalls](#thepitfallsofcommandlineoptions).

By the way, I use `$var` as placeholders that you have to replace with the module, package, or JAR names that fix your problem.

## Five Critical Command Line Options

This post covers `--add-exports`, `--add-opens`, `--add-modules`, `--add-reads`, and `--patch-module`.
Let's get it on!

### Accessing Internal APIs With `--add-exports`

The command line option `--add-exports $module/$package=$readingmodule` exports `$package` of *\$module* to *\$readingmodule*.
Code in *\$readingmodule* can hence access all public types in `$package` but other modules can not.
(The option is available for the `java` and `javac` commands.)

When setting *\$readingmodule* to `ALL-UNNAMED`, all code from the class path can access that package.
When [accessing internal APIs during a migrating to Java 9](java-9-migration-guide#illegal-access-to-internal-apis), you will always use that placeholder - only once your own code runs in modules does it really make sense to limit exports to specific modules.

As an example, assume you have a class that uses `com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel` - during compilation you would get the following error:

```shell
error: package com.sun.java.swing.plaf.nimbus is not visible
import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;
					          ^
  (package com.sun.java.swing.plaf.nimbus is declared
   in module java.desktop, which does not export it)
1 error
```

The troublesome class imports `NimbusLookAndFeel` from the encapsulated package `com.sun.java.swing.plaf.nimbus`.
Note how the error message points out the specific problem, including the module that contains the class.

This clearly doesn't work out of the box on Java 9, but what if we want to keep using it?
Then we'd likely be making a mistake because there's a standardized alternative in `javax.swing.plaf.nimbus`, but for the sake of this example let's say we still want to use this one - maybe to interact with legacy code that can not be changed.

All we have to do to successfully compile against `com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel` is to add `--add-exports java.desktop/com.sun.java.swing.plaf.nimbus=ALL-UNNAMED` to the compiler command.
If we do that manually, it would like as follows:

```shell
javac
	--add-exports java.desktop/com.sun.java.swing.plaf.nimbus=ALL-UNNAMED
	--class-path $dependencies
	-d $target-folder
	$source-files
```

This way, code happily compiles against encapsulated classes.
But it is important to realize that we've only pushed the problem to run time!
Adding this export on the command line really only changes the one compilation - there is no information put into the resulting bytecode that would allow that class to access that package during execution.
So we still have to figure out how to make it work at run time.

<pullquote>Adding exports on the command line only changes the one compilation</pullquote>

### Reflectively Accessing Internal APIs With `--add-opens`

The `java` option `--add-opens $module/$package=$reflectingmodule` can be used to open `$package` of *\$module* for deep reflection to *\$reflectingmodule*.
Code in *\$reflectingmodule* can hence reflectively access all types and members in `$package` but other modules can not.

When setting *\$reflectingmodule* to `ALL-UNNAMED`, all code from the class path can reflectively access that package.When [accessing internal APIs during a migrating to Java 9](java-9-migration-guide#illegal-access-to-internal-apis), you will always use that placeholder - only once your own code runs in modules does it really make sense to limit exports to specific modules.

A common case are dependency injection libraries like Guice that use the class loader's internal API, which results in errors like the following:

```shell
Caused by: java.lang.reflect.InaccessibleObjectException:
Unable to make ClassLoader.defineClass accessible:
module java.base does not "opens java.lang" to unnamed module
```

Note how the error message points out the specific problem, including the module that contains the class.
To make this work we simply need to open the package containing the class:

```shell
java
	--add-opens java.base/java.lang=ALL-UNNAMED
	--class-path $dependencies
	-jar $appjar
```

### Adding Classes To Modules With `--patch-module`

The compiler and runtime option `--patch-module $module=$artifact` merges all classes from `$artifact` into *\$module*.
There are a few things to look out for, but let's see an example before we get to them.

When [discussing split packages during migration](java-9-migration-guide#split-packages), we looked at the example of a project that uses the annotations `@Generated` (from the *java.xml.ws.annotation* module) and `@Nonnull` (from a JSR 305 implementation).
We discovered three things:

-   both annotations are in the `javax.annotation` package, thus creating a split
-   we need to add the module manually because it's a Java EE module
-   doing so makes the JSR 305 portion of the split package invisible

We can use `--patch-module` to mend the split:

```shell
java
	--add-modules java.xml.ws.annotation
	--patch-module java.xml.ws.annotation=jsr305-3.0.2.jar
	--class-path $dependencies
	-jar $appjar
```

This way all classes in `jsr305-3.0.2.jar` becomes part of the module *java.xml.ws.annotation* and can hence be loaded for a successful execution.
Yay!

There are a few things to look out for, though.
First, patching a module does not automatically add it to the module graph.
If it is not required explicitly, it might still need to be added with `--add-modules`.
Then, classes added to a module with `--patch-module` are subject to normal accessibility rules:

-   code that depends on them needs to read the patched module, which must export the necessary packages
-   likewise these classes' dependencies need to be in an exported package in a module read by the patched one

This might require manipulating the module graph with command line options like `--add-reads` and `--add-exports`.
Since named modules can not access code from the class path, it might also be necessary to create some automatic modules.

### Extending The Module Graph With `--add-modules`

The option `--add-modules $modules`, which is available on `javac` and `java`, allows explicitly defining a comma-separated list of root modules beyond the initial module.
(Root modules form the initial set of modules from which [the module graph is built by resolving their dependencies](http://openjdk.java.net/projects/jigsaw/spec/sotms/#resolution).) This allows you to add modules (and their dependencies) to the module graph that would otherwise not show up because the initial module does not depend on them (directly or indirectly).

A particularly important use case for `--add-modules` are Java EE modules, which are [not resolved by default](java-9-migration-guide#dependencies-on-java-ee-modules) when running an application from the class path.
As an example, let's pick a class that uses `JAXBException` from the Java EE module *java.xml.bind*.
Here's how to make that module available for compilation with `--add-modules`:

<pullquote>An important use case for `--add-modules` are Java EE modules</pullquote>

```shell
javac
	--class-path $dependencies
	--add-modules java.xml.bind
	-d ${output_dir}
	${source_files}
```

When the code is compiled and packaged, you need to add the module again for execution:

```shell
java
	--class-path $dependencies
	--add-modules java.xml.bind
	-jar $appjar
```

Other use cases for `--add-modules` are [optional dependencies](java-modules-optional-dependencies).

The `--add-modules` option has three special values: `ALL-DEFAULT`, `ALL-SYSTEM`, and `ALL-MODULE-PATH`.
The first two only work at run time and are used for very specific cases that this post does not discuss.
The last one can be quite useful, though: With it, all modules on the module path become root modules and hence all of them make it into the module graph.

When adding modules it might be necessary to let other modules read them, so let's do that next.

### Extending The Module Graph With `--add-reads`

The compiler and runtime option `--add-reads $module=$targets` adds readability edges from *\$module* to all modules in the comma-separated list *\$targets*.
This allows *\$module* to access all public types in packages exported by those modules even though *\$module* has no `java§requires` clauses mentioning them.
If *\$targets* is set to `ALL-UNNAMED`, *\$module* can even read the unnamed module.

As an example let's turn to [the *ServiceMonitor* application](https://github.com/nipafx/demo-jpms-monitor), which has a *monitor.statistics* module that could sometimes make use of a *monitor.statistics.fancy* module.
Without resorting to [optional dependencies](java-modules-optional-dependencies) (which would likely be the proper solution for this specific case), we can use `--add-modules` to add the fancy module and then `add-reads` to allow *monitor.statistics* to read it:

```shell
java
	--module-path mods
	--add-modules monitor.statistics.fancy
	--add-reads monitor.statistics=monitor.statistics.fancy
	--module monitor
```

## Thoughts On Command Line Options

With Java 9, you might end up applying more command line options than ever before - it sure has been like that for me.
While doing so I had a few insights that might make your life easier.

### Argument Files

Command line options do not actually have to be applied to the command.
An alternative are so-called [*argument files* (or *@-files*)](https://docs.oracle.com/javase/9/tools/java.htm#GUID-3B1CE181-CD30-4178-9602-230B800D4FAE__GUID-36C0C35E-403B-4A05-9C54-0CBE7D237C1C), which are plain text files that can be referenced on the command line with `@<file-name>`.
Compiler and runtime will then act as if the file content had been added to the command.

The example on `--patch-module` showed how to run code that uses annotations from Java EE and JSR 305:

```shell
java
	--add-modules java.xml.ws.annotation
	--patch-module java.xml.ws.annotation=jsr305-3.0.2.jar
	--class-path $dependencies
	-jar $appjar
```

Here, `--add-modules` and `--patch-module` are added to make the compilation work on Java 9.
We could put these two lines in a file called `java-9-args` and then launch as follows:

```shell
java @java-9-args
	--class-path $dependencies
	-jar $appjar
```

What's new in Java 9 is that the JVM also recognizes argument files, so they can be shared between compilation and execution.

Unfortunately, [argument files don't work with Maven](https://stackoverflow.com/q/43361227/2525313) because the compiler plugin already creates a file for all of its own options and Java does not supported nested argument files.
Sad.

### Relying On Weak Encapsulation

The Java 9 runtime [allows illegal access by default to code on the class path](java-9-migration-guide#illegal-access-to-internal-apis) with nothing more than a warning.
That's great to run unprepared applications on Java 9, but I advise against relying on that during a proper build because it allows new illegal accesses to slip by unnoticed.
Instead, I collect all the `--add-exports` and `--add-opens` I need and then activate strong encapsulation at run time with `--illegal-access=deny`.

<pullquote>Don't rely on weak encapsulation</pullquote>

### The Pitfalls Of Command Line Options

Using command line options has a few pitfalls:

-   these options are infectious in the sense that if a JAR needs them, all of its dependencies need them as well
-   developers of libraries and frameworks that require specific options will hopefully document that their clients need to apply them, but of course nobody reads the documentation until it's too late
-   application developers will have to maintain a list of options that merge the requirements of several libraries and frameworks they use
-   it is not easy to maintain the options in a way that allow sharing them between different build phases and execution
-   it is not easy to determine which options can be removed due to a dependency update to a Java 9 compatible version
-   it can be tricky to apply the options to the right Java processes, for example for a build tool plugin that does not run in the same process as the build tool

All of these pitfalls make one thing very clear: Command line options are a fix, not a proper solution, and they have their own long-term costs.
This is no accident - they were designed to make the undesired possible.
Not easy, though, or there would be no incentive to solve the underlying problem.

<pullquote>Command line options are a fix, not a proper solution</pullquote>

So do your best to only rely on public and supported APIs, not to split packages, and to generally avoid picking fights with the module system.
And, very importantly, reward libraries and frameworks that do the same!
But the road to hell is paved with good intentions, so if everything else fails, use every command line option at your disposal.

## Reflection

These five options should get you through most thickets:

-   `--add-exports` to export a package, which makes its public types and members accessible (`javac` and `java`)
-   `--add-opens` to open a package, which makes all its types and members accessible (`java`)
-   `--patch-module` adds classes to a specific module
-   `--add-modules` adds the listed modules and their transitive dependencies to the module graph
-   `--add-reads` makes one module read another

As discussed, command line options come with a set of pitfalls, so make sure to only use them where absolutely necessary and work to reduce those cases.
