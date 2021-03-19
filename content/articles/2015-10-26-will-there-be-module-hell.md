---
title: "Will There Be Module Hell?"
tags: [java-next, java-9, project-jigsaw]
date: 2015-10-26
slug: will-there-be-module-hell
description: "Java 9's Project Jigsaw promises to solve JAR hell but falls short when it comes to conflicting versions. Will there be module hell instead?"
searchKeywords: "module hell"
featuredImage: module-hell
---

Project Jigsaw has [ambitious objectives](motivation-goals-project-jigsaw#goals-of-project-jigsaw), one of them is ["to escape the 'JAR hell' of the brittle and error-prone class-path mechanism"](http://mreinhold.org/blog/late-for-the-train).
But while it will achieve many of its goals it looks like it may fall short on this one.

So will there be module hell instead?

To know what we are talking about we'll start with a quick recap of JAR hell.
We will then discuss what aspects Jigsaw touches on and how that might not change the big picture.
Lastly we will have a look at the official stance on the topic and formulate a proposal to prevent looming module hell.

## JAR Hell

I discussed JAR hell in detail in [my last post](jar-hell), which you might want to read if you haven't already.
It ends with this list of the different circles of JAR hell:

-   unexpressed and transitive dependencies
-   shadowing
-   version conflicts
-   complex class loading

Based on what build tools and component systems (called *containers* by the JDK developers) bring to the game and how widely they are used it concludes that unexpressed and transitive dependencies are largely solved, shadowing at least eased and complex class loading not commonplace.

This leaves version conflicts as the most problematic aspect of JAR hell, influencing everyday update decisions in many, many projects.

## What Will Change With Jigsaw?

I have already written about [all the features Project Jigsaw was planned to bring to Java 9](features-project-jigsaw) but this post will take a different angle.
First, it is influenced by experiments with [the current early access build](https://jdk9.java.net/jigsaw/) and, second, it only looks at the aspects pertaining JAR/module hell.

The core concept Jigsaw brings to Java are [*modules*](features-project-jigsaw#the-core-concept).
Overly simplified, a module is like a JAR with some additional information and features.
Some of those pieces of information are a module's name and the names of other modules it depends on.

### Dependencies

The information is interpreted by the compiler and the JVM when they process a module.
Tasked to compile or launch one, they will transitively resolve all dependencies within a universe of modules specified via the [*module path*](http://openjdk.java.net/projects/jigsaw/spec/sotms/#module-paths).
Roughly said, this is analogue to a class path scan but now we are looking for entire modules instead of individual classes and, in case of the JVM, we are doing it at launch-time not at runtime.

Resolving the transitive dependencies of a module fails with an error if not all modules are found on the module path.
This clearly solves the problem of unexpressed and endlessly transitive dependencies.

<pullquote>Jigsaw solves the problem of unexpressed and endlessly transitive dependencies.</pullquote>

I see it as a material benefit that the Java language now officially knows about dependencies and that all the tools, starting with the compiler and JVM, understand and work with them!
This should not be understated.

But I assume it will have little effect on the typical developer's everyday work since this is already sufficiently addressed by existing infrastructure, i.e.
the build tool.

This becomes even clearer when we consider where the module information will come from.
It already exists as part of the build information, e.g. in the `pom.xml`.
It would be redundant to additionally specify names and dependencies for the module system and it is hence assumed that the build tool will use its information to automatically generate the module information.
(I am sure Mark Reinhold or Alan Bateman repeatedly stated this but can't find a quote right now.
Store this as hearsay for now.)

### Shadowing

Jigsaw eliminates the problem of shadowing:

> The module system ensures that every dependence is fulfilled by precisely one other module, \[...\] that every module reads at most one module defining a given package, and that modules defining identically-named packages do not interfere with each other.
>
> [State Of The Module System - Readibility (Sep 2015)](http://openjdk.java.net/projects/jigsaw/spec/sotms/#readability)

To be more precise, the module system quits and reports an error as soon as it encounters ambiguous situations, e.g. two modules exporting the same package to the same module.

### Version Conflicts

We identified conflicting versions of third party libraries as the most daunting aspect of JAR hell.
The most straight forward solution would be a module system able to load different versions of the same module.
It would have to prove that these versions can not interact but given the strong promises regarding encapsulation and readability it looks like it should be able to do that.

Now, here is the problem:

> It is not necessary to support more than one version of a module within a single configuration.
>
> [Java Platform Module System: Requirements - Multiple Versions (Apr 2015)](http://openjdk.java.net/projects/jigsaw/spec/reqs/#multiple-versions)

Indeed the current build neither creates nor understands module version information.

For some time it looked like there would be workarounds.
The ugliest but most promising one renames the conflicting artifacts so that they are no longer two different versions of the same module but appear as two different modules, coincidently exporting the same packages.

But this approach fails.
Apparently ensuring "that modules defining identically-named packages do not interfere with each other" is solved by roundly rejecting any launch configuration where two modules export the same packages.
Even if no module would read them both!

<pullquote>Jigsaw does nothing to help with the problem of conflicting versions.</pullquote>

So apparently Jigsaw does nothing to help with the problem of conflicting versions unless one resorts to component-system-like behavior at runtime.
What a disappointment!

### Complex Class Loading

Discussing how modules and class loaders interact and how that might change the complexity of class loading deserves its own post.
Preferably by someone more experienced with class loaders.

Let's just have a look at the basics.

> The module system, in fact, places few restrictions on the relationships between modules and class loaders.
A class loader can load types from one module or from many modules, so long as the modules do not interfere with each other and the types in any particular module are loaded by just one loader.
>
> [State Of The Module System - Class Loaders (Sep 2015)](http://openjdk.java.net/projects/jigsaw/spec/sotms/#class-loaders)

So there will be a 1:n-relationship of class loaders to modules.

Then there is the new notion of *layers*, which component systems can use to structure class loader hierarchies.

> A layer encapsulates a module graph and a mapping from each module in that graph to a class loader.
The *boot layer* is created by the Java virtual machine at startup by resolving the application’s initial module against the observable modules built-in to the run-time environment and also against those found on the module path.
>
> \[...\]
>
> Layers can be stacked: A new layer can be built on top of the boot layer, and then another layer can be built on top of that.
As a result of the normal resolution process the modules in a given layer can read modules in that layer or in any lower layer.
>
> [State Of The Module System - Layers (Sep 2015)](http://openjdk.java.net/projects/jigsaw/spec/sotms/#layers)

So while the class loader system gets more elements, the mechanics and best practices might improve, possibly resulting in less complexity of well designed systems.
At the same time the new fail-fast properties regarding dependencies and shadowing will make problems more obvious and troubleshooting easier.

So all in all it looks like this problem does not go away but becomes less vexing.

## Module Hell?

With dependencies and shadowing solved and class loading improved why would I talk about module hell?
Just because of version conflicts?
Short answer: Yes!

Long answer: Take a look at the search results for JAR hell - the topic of conflicting versions is by far the most common motivator for discussing this.
Of all the aspects we talked about so far it is the only one that commonly plagues the majority of projects (at least by my conjecture).

<pullquote>If Jigsaw wants to solve JAR hell, it has to address version conflicts.</pullquote>

So if Jigsaw wants to solve JAR hell, it has to address version conflicts!
Otherwise not much might change for many projects.
They will still struggle with it and they will continue to get themselves into custom built class loader nightmares.
Sounds like module hell to me.

Yes, it looks just like [JAR hell](jar-hell#jar-hell) - that's because module hell will be so similar.

## Official Stance On Versions

So what is the official stance on the topic of versions?

### Multiple Versions

> It is not necessary to support more than one version of a module within a single configuration.
>
> Most applications are not containers and, since they currently rely upon the class path, do not require the ability to load multiple versions of a module.
>
> [Java Platform Module System: Requirements - Multiple Versions (Apr 2015)](http://openjdk.java.net/projects/jigsaw/spec/reqs/#multiple-versions)

I strongly disagree with this assessment!
As I said before, I am convinced that this is a problem for pretty much any project.
In fact, I believe that the quoted rationale reverses cause and effect.

In my opinion it's more like this:

> Most applications decide against the complexity of running a container and, since they are consequently stuck with the class path, are not able to load multiple versions of a module.

### Version Information

And why does the current early access build go even further and completely abandon version information?

> A module's declaration does not include a version string, nor constraints upon the version strings of the modules upon which it depends.
This is intentional: It is not a goal of the module system to solve the version-selection problem, which is best left to build tools and container applications.
>
> [State Of The Module System - Module Declarations (Sep 2015)](http://openjdk.java.net/projects/jigsaw/spec/sotms/#module-declarations)

It is easy to agree with the premise.
Many tools have tackled the non-trivial problem of version selection and there is no need to bake one of those solutions into the VM.

But I fail to see what this has to do with completely ignoring version information.
And it does also not exclude letting an external tool select the versions and pass its solution to the launching VM.

### Conflicting Versions

Summarized, the official stance regarding conflicting versions is this:

> The module system isn't suggesting any solutions, it is instead leaving this problem to the build tools and containers.
>
> [Alan Bateman on Jigsaw-Dev (Oct 2015)](http://mail.openjdk.java.net/pipermail/jigsaw-dev/2015-October/004787.html)

Which sounds great except that the module system does currently not provide *any* new mechanisms for build tools to solve this longstanding and fundamental problem.

## Proposal

Given only an initial module and a universe of modules to resolve dependencies within, the current JVM refuses to launch if any ambiguities, e.g. two versions of the same module, are encountered.
This is very reasonable behavior and I would not change it.

My proposal is to enable developers and build tools to pass additional information that solve ambiguous situations.
(While I thought through the proposal [Ali Ebrahimi independently made the same one](http://mail.openjdk.java.net/pipermail/jpms-spec-observers/2015-October/000204.html "Why not use the Manifest? - Ali Ebrahimi").)

### How

The two common ways to pass such information are the command line and configuration files.

Command line arguments would have to be repeated on every launch.
Depending on how comprehensive the information and how large the project is, this could be tedious.

A configuration file could be created by the build tool and later specified via command line.
This looks like the best approach to me.

### What

Currently, the initial module and all transitive dependencies are resolved as a single [configuration](http://download.java.net/jigsaw/docs/api/java/lang/module/Configuration.html), which is used to create a single layer.
But it is already straight forward to load multiple versions of the same module into different layers at runtime.
(This is what component systems might do in the future.)

So all that is needed is to allow users to explicitly specify configurations with multiple layers.
The JVM would parse this when it launches and create the layers accordingly.

<pullquote>All that is needed are explicit configurations with multiple layers.</pullquote>

Looking at the current goals, requirements and capabilities this fits in quite nicely.
Especially since it does not implement version selection and does not require new module system capabilities.
And it would be a nice feature to enable complex configurations at launch-time regardless of version conflicts.
I am sure there are other use cases.

As an add-on, it might be interesting to think about partial configurations.
They would only specify those parts of the module graph that are of special interest, e.g. because of conflicting versions.
Everything else could be resolved relative to them.

### Demarcation

This is not meant to replace existing component systems!
Users of OSGi, Wildfly, ... most likely have more reasons to use them than just version conflicts.
Instead it would be an entry-level mechanism usable by every project out there without much additional complexity.

## Reflection

In the first part we have assessed how Project Jigsaw addresses JAR hell:

-   unexpressed and transitive dependencies: solved
-   shadowing: solved
-   version conflicts: untouched
-   complex class loading: remains to be seen

Since version conflicts are the most relevant aspect of JAR hell today, we concluded that they will give rise to module hell tomorrow.

To prevent that, a proposal was made that requires no notable changes to the module system and utilizes already existing features:

**Enable explicitly specified configurations with multiple layers.**

You can give this proposal more weight by sharing it with the community.
If you care about the topic, you might want to watch or participate in the ongoing discussions on the [Jigsaw-Dev](http://mail.openjdk.java.net/mailman/listinfo/jigsaw-dev) and [JPMS-Spec](http://mail.openjdk.java.net/mailman/listinfo/jpms-spec-observers) mailing lists.
