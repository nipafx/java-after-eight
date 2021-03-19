---
title: "Java 9 Migration Guide: The Seven Most Common Challenges"
tags: [java-9, j_ms, migration]
date: 2017-07-24
slug: java-9-migration-guide
description: "Solutions to the seven most common challenges for a Java 9 migration. Each of them explained with background, symptoms, and fixes."
intro: "Migrating to Java 9 is no walk in the park, but it's not intractable either. If you know how to fix these seven most common problems, you'll be able to power through and make your project Java 9 compatible."
searchKeywords: "java 9 migration"
featuredImage: java-9-migration
repo: java-9-migration
---

I'm sure you've heard that updating to Java 9 is no walk in the park, maybe even that it's an incompatible update and that a migration makes no sense for large code bases.
After doing exactly that, migrating an old and fairly large code base, I can tell you that it's not that bad.
It's more work than bumping to Java 8, true, but it's time well spent.
More than anything else, the migration uncovered some small and a few not so small problems that needed fixing regardless of the migration itself and we took the opportunity to do just that.

I collected the seven largest issues into this Java 9 migration guide.
It's as much a post as it is a resource to come back to, so put it on speed dial and search it when you have a concrete problem.
Also note that while you need to know a bit about [the module system](tag:j_ms) ([here's a tutorial](java-module-system-tutorial)), this is not about modularizing your application - it is only about getting it to compile and run on Java 9.

## Illegal Access To Internal APIs

One of the module system's biggest selling points is strong encapsulation.
It makes sure non-public classes as well as classes from non-exported packages are inaccessible from outside the module.
First and foremost, this of course applies to the platform modules shipped with the JDK, where only `java.*` and `javax.*` packages are fully supported.
Most `com.sun.*` and `sun.*` packages, on the other hand, are internal and hence inaccessible by default.

While the Java 9 compiler behaves exactly as you would expect and prevents illegal access, the same is not true for the run time.
To offer a modicum of backwards compatibility it eases migration and improves the chances of applications built on Java 8 to run on Java 9 by granting access to internal classes.
If reflection is used for the access, a warning is emitted.

### Symptoms

During compilation against Java 9 you see compile errors similar to the following:

```shell
error: package com.sun.java.swing.plaf.nimbus is not visible
import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel;
					          ^
	(package com.sun.java.swing.plaf.nimbus is declared
	in module java.desktop, which does not export it)
1 error
```

Warnings emitted for reflection look as follows:

```shell
Static access to [Nimbus Look and Feel]
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by j9ms.internal.Nimbus
	(file:...) to constructor NimbusLookAndFeel()
WARNING: Please consider reporting this
	to the maintainers of j9ms.internal.Nimbus
WARNING: Use --illegal-access=warn to enable warnings
	of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
Reflective access to [Nimbus Look and Feel]
```

### Fixes

The most obvious and sustainable fix for dependencies on internal APIs is to get rid of them.
Replace them with maintained APIs and you paid back some high-risk technical debt.

If that can't be done for whatever reason, the next best thing is to acknowledge the dependencies and inform the module system that you need to access it.
To that end you can use two command line options:

-   The option `--add-exports $module/$package=$readingmodule` can be used to export `$package` of *\$module* to *\$readingmodule*.
Code in *\$readingmodule* can hence access all public types in `$package` but other modules can not.
When setting *\$readingmodule* to `ALL-UNNAMED`, all code from the class path can access that package.
During a migration to Java 9, you will always use that placeholder.
The option is available for the `java` and `javac` commands.
-   This covers access to public members of public types but reflection can do more than that: With the generous use of `setAccessible(true)` it allows interaction with non-public classes, fields, constructors, and methods (sometimes called *deep reflection*), which even in exported packages are still encapsulated.
The `java` option `--add-opens` uses the same syntax as `--add-exports` and opens the package to deep reflection, meaning all of its types and their members are accessible regardless of their visibility modifiers.

You obviously need `--add-exports` to appease the compiler but gathering `--add-exports` and `--add-opens` for the run time has advantages as well:

1. the run time's permissive behavior will change in future Java releases, so you have to do that work at some point anyway
2. `--add-opens` makes the warnings for illegal reflective access go away
3. as I will show in a minute, you can make sure no new dependencies crop up by making the run time actually enforce strong encapsulation

### Going Further

Compiling against Java 9 helps hunting down dependencies on internal APIs in the project's code base.
But the libraries and frameworks your project uses are just as likely to make trouble.

JDeps is the perfect tool to find compile dependencies on JDK-internal APIs in your project *and* your dependencies.
If you're not familiar with it, I've written [a tutorial](jdeps-tutorial-analyze-java-project-dependencies) that gets you started.
Here's how to use it for the task at hand:

```shell
jdeps --jdk-internals -R --class-path 'libs/*' $project
```

Here, `libs` is a folder containing all of your dependencies and `$project` your project's JAR.
Analyzing the output is beyond this article's scope but it's not that hard - you'll manage.

Finding reflective access is a little tougher.
The run time's default behavior is to warn you once for the first illegal access to a package, which is insufficient.
Fortunately, there's the `--illegal-access=$value` option, where `$value` can be:

-   `permit`: Access to all JDK-internal APIs is permitted to code on the class path.
For reflective access, a single warning is issued for the *first* access to each package.
(Default in Java 9, but [will be removed in a future release](http://mail.openjdk.java.net/pipermail/jigsaw-dev/2017-June/012841.html).)
-   `warn`: Behaves like `permit` but a warning is issued for *each* reflective access.
-   `debug`: Behaves like `warn` but a stack trace is included in each warning.
-   `deny`: The option for those who believe in strong encapsulation:
	All illegal access is forbidden by default.

Particularly `deny` is very helpful to hunt down reflective access.
It is also a great default value to set once you've collected all required `--add-exports` and `--add-opens` options.
This way, no new dependencies can crop up without you noticing it.

## Dependencies On Java EE Modules

There's a lot of code in Java SE that's actually Java EE related.
It ended up in these six modules:

-   *java.activation* with `javax.activation` package
-   *java.corba* with `javax.activity`, `javax.rmi`, `javax.rmi.CORBA`, and `org.omg.*` packages
-   *java.transaction* with `javax.transaction` package
-   *java.xml.bind* with all `javax.xml.bind.*` packages
-   *java.xml.ws* with `javax.jws`, `javax.jws.soap`, `javax.xml.soap`, and all `javax.xml.ws.*` packages
-   *java.xml.ws.annotation* with `javax.annotation` package

For various compatibility reasons (one of them being split packages, which we will look at next), code on the class path does not see these modules by default, which leads to compile or run time errors.

### Symptoms

Here's a compile error for a class using `JAXBException` from the *java.xml.bind* module:

```shell
error: package javax.xml.bind is not visible
import javax.xml.bind.JAXBException;
				^
	(package javax.xml.bind is declared in module java.xml.bind,
		which is not in the module graph)
1 error
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

### Fixes on Java 9 and 10

Once you modularized your code, you can declare a regular dependency in the module's declaration.
Until then, `--add-modules $module` comes to your rescue, which makes sure `$module` is available and can be added to both `java` and `javac`.
If you add *java.se.ee*, you'll have access to all Java EE modules.

### Fixes on Java 11 and later

Java 11 [removes the Java EE modules](http://openjdk.java.net/jeps/320), so from then on you will need third-party implementations instead.
[This StackOverflow answer contains a list of alternatives.](https://stackoverflow.com/a/48204154/2525313) Note that using third-party dependencies already works from Java 9 on, so you don't have to use `--add-modules` as a stopgap.

## Split Packages

This one is a little tricky... To enforce consistency a module is not allowed to read the same package from two different modules.
The actual implementation is stricter, though, and no two modules are allowed to even *contain* the same package (exported or not).
The module system operates under that assumption and whenever a class needs to be loaded, it looks up which module contains that package and goes looking for the class in there (which should boost class loading performance).

To safeguard the assumption the module system checks that no two named modules *split a package* and barfs if it finds any that do.
During migration you're not quite in that situation, though.
Your code comes from the class path, which puts it into the so-called unnamed module.
To maximize compatibility it is not scrutinized and no module-related checks are applied to it.

Now, in the case of split packages, this means a split between a named module (e.g. in the JDK) and the unnamed module is not discovered.
Which may sound very fortunate, is the opposite if you mix in the class loading behavior: If a package is split between a module and the class path, for classes from that package class loading will always *and only* look into the module.
This means classes in the class path portion of the package are effectively invisible.

### Symptoms

The symptom is that a class from the class path can not be loaded even though it's definitely there, leading to compile errors like this:

```shell
error: cannot find symbol
	symbol:   class Nonnull
	location: package javax.annotation
```

Or, at run time, to `NoClassDefFoundError`s like above.

One example where this can occur is with the various JSR-305 implementations.
A project using, for example, the annotations `javax.annotation.Generated` (from *java.xml.ws.annotation*) and `java.annotation.Nonnull` (from *com.google.code.findbugs:jsr305*) will have trouble compiling.
It is either missing the Java EE annotations or, when the module is added like described above, will encounter a split package and not see the JSR 305 module.

### Fixes

The migration path will be different, depending on the artifact that splits the JDK package.
In some cases it might be more than just some classes that go into a random JDK package but a replacement for an entire JDK module, for example because it [overrides an endorsed standard](https://docs.oracle.com/javase/8/docs/technotes/guides/standards/).
In that case, you are looking for the `--upgrade-module-path $dir` option - modules found in `$dir` are used to *replace* upgradeable modules in the run time.

If you indeed just have a couple of classes that split a package, the long-term solution is to remove the split.
In case that is not possible in the short-term, you can patch the named module with the content from the class path.
The option `--patch-module $module=$artifact` will merge all classes from `$artifact` into `$module`, putting all portions of the split package into the same module, thus mending split.

In the case of *java.xml.ws.annotation* and `@Nonnull` that would be

`--patch-module java.xml.ws.annotation=path/to/jsr305-3.0.2.jar`.
For this particular problem, there are [other viable solutions, though, which I explore in a separate post](jsr-305-java-9).

There are a few things to look out for, though.
First of all, the patched module must actually make it into the module graph, for which it might be necessary to use `--add-modules`.
Then, it must have access to all the dependencies that it needs to run successfully.
Since named modules can not access code from the class path, this might make it necessary to start creating some [automatic modules](http://openjdk.java.net/projects/jigsaw/spec/sotms/#automatic-modules), which goes beyond the scope of this post.

### Going Further

Finding split package by try and error is pretty unnerving.
Fortunately [JDeps](jdeps-tutorial-analyze-java-project-dependencies) reports them, so if you analyze your project and its dependencies, the first lines of output will report split packages.
You can use the same command as above:

```shell
jdeps --jdk-internals -R --class-path '$libs/*' project.jar
```

## Casting To `URLClassLoader`

The class loading strategy that I just described is implemented in a new type and in Java 9 the application class loader is of that type.
That means it is not a `URLClassLoader`, anymore, so the occasional `(URLClassLoader) getClass().getClassLoader()` or `(URLClassLoader) ClassLoader.getSystemClassLoader()` sequences will no longer execute.
This is another typical example where Java 9 is backwards compatible in the strict sense (because that it's a `URLCassLoader` was never specified) but which can nonetheless cause migration challenges.

### Symptoms

This one is very obvious.
You'll get a `ClassCastException` complaining that the new `AppClassLoader` is no `URLClassLoader`:

```shell
Exception in thread "main" java.lang.ClassCastException:
	java.base/jdk.internal.loader.ClassLoaders$AppClassLoader
	cannot be cast to java.base/java.net.URLClassLoader
		at monitor.Main.logClassPathContent(Main.java:46)
		at monitor.Main.main(Main.java:28)
```

### Fixes

The class loader was probably cast to access methods specific to `URLClassLoader`.
If so, your chances to do a migration with only small changes are slim.
The only supported (and hence accessible) super types of the new `AppClassLoader` are [`SecureClassLoader` and `ClassLoader`](https://docs.oracle.com/javase/9/docs/api/java/security/SecureClassLoader.html) and only few methods were added here in 9.
Still, have a look, they might do what you're looking for.

If you've used the `URLClassLoader` to dynamically load user provided code (for example as part of a plugin infrastructure) by appending to the class path, then you have to find a new way to do that as it can not be done with Java 9.
You should instead consider creating a new class loader for that.
This has the added advantage that you'll be able to get rid of the new classes as they are not loaded into the application class loader.
If you're compiling against Java 9, you should read up on [layers](https://docs.oracle.com/javase/9/docs/api/java/lang/ModuleLayer.html) - they give you a clean abstraction for loading an entirely new module graph.

## Rummaging Around In Runtime Images

With the JDK being modularized the layout of the run time image fundamentally changed.
Files like `rt.jar`, `tools.jar`, and `dt.jar` are gone; the JDK classes are now bundled into `jmod` files (one per module), a purposely unspecified file format that allows future optimizations without regards to backwards compatibility.
Furthermore the distinction between JRE and JDK is gone.

All of this has been unspecified but that doesn't mean that there's no code out there depending on these details.
Particularly tools like IDEs (although these have mostly been updated already) will have compatibility problems with these changes and will stop working in unpredictable ways unless they're updated.

As a consequence of these changes, the URL you get for system resources, e.g. from `ClasLoader::getSystemResource`, changed.
It used to be of the following form: `jar:file:$javahome/lib/rt.jar!$path`, where `$path` is something like `java/lang/String.class`.
It now looks like `jrt:/$module/$path`.
Of course all APIs that create or consume such URLs were updated but non-JDK code handcrafting these URLs will have to be updated for Java 9.

Furthermore, the `Class::getResource*` and `ClassLoader::getResource*` methods no longer read JDK-internal resources.
Instead use `Module::getResourceAsStream` to access module-internal resources or create a JRT file system as follows:

```java
FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
fs.getPath("java.base", "java/lang/String.class"));
```

## Boot Class Path

I'm in murky waters here because I never used the `-Xbootclasspath` option, which is mostly removed.
Apparently its features are replaced by various new command line options (paraphrasing from [JEP 220](http://openjdk.java.net/jeps/220) here):

-   the `javac` option `--system` can be used to specify an alternate source of system modules
-   the `javac` option `--release` can be used to specify an alternate platform version
-   the `java` option `--patch-module` option, mentioned above, can be used to inject content into modules in the initial module graph

## New Version Strings

After more than 20 years, Java has finally and officially accepted that it's no longer on version `1.x`.
Hooray!
So from Java 9 on, the system property `java.version` and its siblings no longer start with `1.x` but with `x`, i.e.
`9` in Java 9.

### Symptoms

There are no clear-cut symptoms - pretty much everything could go wrong if some utility function determines the wrong version.
It's not too hard to find, though.
A full text search for the following strings should lead to all version-string-specific code: `java.version`, `java.runtime.version`, `java.vm.version`, `java.specification.version`, `java.vm.specification.version`.

### Fixes

If you are willing to raise your project's requirements to Java 9, you can eschew the whole system property prodding and parsing and instead use [the new `Runtime.Version` type](https://docs.oracle.com/javase/9/docs/api/java/lang/Runtime.Version.html), which makes all of this much easier.
If you want to stay compatible to pre Java 9, you could still use the new API by creating a [multi-release JAR](https://www.sitepoint.com/inside-java-9-part-i/#multireleasejars).
If that's also out of the question, it looks like you actually have to write some code (uch!) and branch based on the major version.

## Summary

Now you know how to use internal APIs (`--add-export` and `--add-opens`), how to make sure Java EE modules are present (`--add-modules`), and how to deal with split packages (`--patch-module`).
These are the most likely problems you'll encounter during a migration.
Less common and also less easy to fix without access to the problematic code are casts to `URLClassLoader`, problems due to the new runtime image layout and resource URLs, the removed `-Xbootclasspath`, and new version strings.

Knowing how to fix these will give you very good chances to overcome all your migration challenges and make your application compile and run on Java 9.
If not, take a look at [JEP 261's *Risks and Assumptions* sections](http://openjdk.java.net/jeps/261#Risks-and-Assumptions), which lists a few other potential pitfalls.

If you're a little overwhelmed by all this, wait for [my next post](planning-your-java-9-update), which gives some advice on how to string these individual fixes into a comprehensive migration strategy, for example by including build tools and continuous integration.
Or [get my book](https://www.manning.com/books/the-java-9-module-system?a_aid=nipa&a_bid=869915cb), where I explain all of this and more.
