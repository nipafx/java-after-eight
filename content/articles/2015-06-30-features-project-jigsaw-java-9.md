---
title: "The Features Project Jigsaw Brings To Java 9"
tags: [java-next, java-9, project-jigsaw]
date: 2015-06-30
slug: features-project-jigsaw
description: "A detailed presentation of the features Project Jigsaw brings to Java 9: modularization, encapsulation, configuration, performance, and more."
searchKeywords: "features project jigsaw"
featuredImage: project-jigsaw-3a-features
---

So, Project Jigsaw... We already know [quite a bit about it](tag:project-jigsaw) but have not yet seen the details of how it plans to deliver on its promises.
This post will do precisely that and present the project's core concepts and features.

The first part of this post covers the core concepts of Project Jigsaw, namely the modules.
We then see which features they will have and how they are planned to interact with existing code and tools.
Main sources for this article are the [requirements of Project Jigsaw](http://openjdk.java.net/projects/jigsaw/goals-reqs/03) and [of JSR 376](http://openjdk.java.net/projects/jigsaw/spec/reqs/02).
While these documents are based on a thorough exploratory phase and are hence very mature, they are still subject to change.
Nothing of what follows is set in stone.

## The Core Concept

With Project Jigsaw the Java language will be extended to have a concept of modules.

> \[Modules\] are named, self-describing program components consisting of code and data.
> A module must be able to contain Java classes and interfaces, as organized into packages, and also native code, in the form of dynamically-loadable libraries.
> A module’s data must be able to contain static resource files and user-editable configuration files.
>
> [Java Platform Module System: Requirements (DRAFT 2)](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#modules)

To get a feeling for modules you can think of well-known libraries like [each of the Apache Commons](https://commons.apache.org/) (e.g. Collections or IO), [Google Guava](https://github.com/google/guava) or (*cough*) [LibFX](http://libfx.codefx.org) as a module.
Well, depending on how granular their authors want to split them, each might actually consist of several modules.

The same is true for an application.
It might be a single monolithic module but it might also be separated into more.
I'd say a project's size and cohesion will be the main determining factors for the number of modules into which it could be carved up.
Whether its actual architecture and implementation allows that is another story of course.

The plan is that modules will become a regular tool in a developer's box to organize her code.

> Developers already think about standard kinds of program components such as classes and interfaces in terms of the language.
> Modules should be just another kind of program component, and like classes and interfaces they should have meaning in all phases of a program’s development.
>
> [Mark Reinholds - Project Jigsaw: Bringing the big picture into focus](http://mreinhold.org/blog/jigsaw-focus)

Modules can then be combined into a variety of configurations in all phases of development, i.e.
at compile time, build time, install time or run time.
They will be available to Java users like us (in that case sometimes called *developer modules*) but they will also be used to dissect the Java runtime itself (then often called *platform modules*).

In fact, this is the current plan for how the JDK will be modularized:

<contentimage slug="project-jigsaw-3b-jdk-modularization"></contentimage>

## Features

So how do modules work?
Looking at the planned features will help us get a feeling for them.

Note that even though the following sections will present a lot of features, they are neither discussed in all available detail nor is the list complete.
If you're interested to learn more, you can start by following the bracketed links or check out the complete [requirements of Project Jigsaw](http://openjdk.java.net/projects/jigsaw/goals-reqs/03) and [of JSR 376](http://openjdk.java.net/projects/jigsaw/spec/reqs/02) straight away.

### Dependency Management

In order to [solve JAR/classpath hell](motivation-goals-project-jigsaw#jarclasspath-hell) one of the core features Project Jigsaw implements is dependency management.

#### Declaration And Resolution

A module will declare which other modules it requires to compile and run \[[dependencies](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#dependences)\].
This will be used by the module system to transitively identify all the modules required to compile or run the initial one \[[resolution](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#resolution)\].

It will also be possible to depend not on specific modules but on a set of interfaces.
The module system will then try to find modules which implement these interfaces and thus satisfy the dependency \[[services](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#services), [binding](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#binding)\].

#### Versioning

There will be support for versioning modules \[[versioning](http://openjdk.java.net/projects/jigsaw/goals-reqs/03#versioning)\].
They will be able to indicate their own version (in pretty much any format as long as it is totally ordered) as well as constraints for their dependencies.
It will be possible to override both of these pieces of information in any phase.
The module system will enforce during each phase that a configuration satisfies all constraints.

Project Jigsaw will not necessarily support multiple versions of a module within a single configuration \[[multiple versions](http://openjdk.java.net/projects/jigsaw/goals-reqs/03#multiple-versions)\].
But wait, then how does this solve JAR hell?
[Good question.](http://mail.openjdk.java.net/pipermail/jigsaw-dev/2015-June/004336.html)

The module system might also not implement version selection .
So when I wrote above that "the module system \[will\] identify all the modules required to compile or run" another module, this was based on the assumption that there is only one version of each.
If there are several, an upstream step (e.g. the developer or, more likely, the build tool he uses) must make a selection and the system will only validate that it satisfies all constraints \[[version-selection](http://openjdk.java.net/projects/jigsaw/goals-reqs/03#version-selection)\].

### Encapsulation

All public classes and interfaces in a JAR are automatically available to all other code which was loaded from the same class path.
This will be different for modules, where the system will enforce a stronger encapsulation in all phases (regardless of whether a security manager is present or not).

A module will declare specific packages and only the types contained in them will be exported.
This means that only they will be visible and accessible to other modules.
Even stricter, the types will only be exported to those modules which explicitly depend on the module containing them \[[export](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#exports), [encapsulation](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#encapsulation)\].

To help developers (especially those modularizing the JDK) in keeping exported API surfaces small, an additional publication mechanism will exist.
This one will allow a module to specify additional packages to be exported but only to an also specified set of modules.
So whereas with the "regular" mechanism the exporting module won't know (nor care) who accesses the packages, this one will allow it to limit the set of possible dependants \[[qualified exports](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#qualified-exports)\].

It will also be possible for a module to re-export the API (or parts thereof) of a module it depends upon.
This will allow to split and merge modules without breaking dependencies because the original ones can continue to exist.
They will export the exact same packages as before even though they might not contain all the code \[[refactoring](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#refactoring)\].
In the extreme case so-called *aggregator modules* could contain no code at all and act as a single abstraction of a set of modules.
In fact, the compact profiles from Java 8 will be exactly that.

Different modules will be able to contain packages with the same name, they will even be allowed to export them \[[export](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#exports), [non-interference](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#non-interference)\].

Oracle will use this opportunity [to make all internal APIs unavailable](how-java-9-and-project-jigsaw-may-break-your-code#internal-apis-become-unavailable).
This will be the biggest impediment for adoption of Java 9 but is definitely setting the right course.
First and foremost, it will greatly improve security as critical code is now hidden from attackers.
It will also make the JDK considerably more maintainable, which will pay off in the long run.

### Configuration, Phases, And Fidelity

As mentioned earlier, modules can be combined into a variety of configurations in all phases of development.
This is true for the platform modules, which can be used to create images identical to the full JRE or JDK, the compact profiles introduced in Java 8, or any custom configuration which contains only a specified set of modules (and their transitive dependencies) \[[JEP 200; Goals](http://openjdk.java.net/jeps/200)\].
Likewise, developers can use the mechanism to compose different variants of their own modularized applications.

At compile time, the code being compiled will only see types which are exported by a configured set of modules \[[compile-time configuration](http://openjdk.java.net/projects/jigsaw/goals-reqs/03#compile-time-configuration)\].
At build-time, a new tool (presumably called *JLink*) will allow the creation of binary run-time images which contain specific modules and their dependencies \[[build-time configuration](http://openjdk.java.net/projects/jigsaw/goals-reqs/03#build-time-configuration)\].
At launch time, an image can be made to appear as if it only contains a subset of its modules \[[launch-time configuration](http://openjdk.java.net/projects/jigsaw/goals-reqs/03#launch-time-configuration)\].

It will also be possible to replace modules which implement an [endorsed standard](http://docs.oracle.com/javase/8/docs/technotes/guides/standards/#endorsed-standards-apis) or a [standalone technology](http://docs.oracle.com/javase/8/docs/technotes/guides/standards/#standalone-technologies) with a newer version in each of the phases \[[upgradeable modules](http://openjdk.java.net/projects/jigsaw/goals-reqs/03#upgradeable-modules)\].
This will replace the deprecated [endorsed standards override mechanism](how-java-9-and-project-jigsaw-may-break-your-code#removal-of-the-endorsed-standards-override-mechanism) and the [extension mechanism](how-java-9-and-project-jigsaw-may-break-your-code#removal-of-the-extension-mechanism).

All aspects of the module system (like dependency management, encapsulation and so forth) will work in the same manner in all phases unless this is not possible for specific reasons \[[fidelity](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#fidelity-across-all-phases)\].

All module-specific information (like versions, dependencies and package export) will be expressed in code files, independent of IDEs and build tools.

### Performance

#### Whole-Program Optimization Techniques

Within a module system with strong encapsulation it is much easier to automatically reason about all the places where a specific piece of code will be used.
This makes certain program analysis and optimization techniques more feasible:

> Fast lookup of both JDK and application classes; early bytecode verification; aggressive inlining of, e.g., lambda expressions, and other standard compiler optimizations; construction of JVM-specific memory images that can be loaded more efficiently than class files; ahead-of-time compilation of method bodies to native code; and the removal of unused fields, methods, and classes.
>
> [Project Jigsaw: Goals & Requirements (DRAFT 3)](http://openjdk.java.net/projects/jigsaw/goals-reqs/03#enable-ahead-of-time-whole-program-optimization-techniques)

These are labeled *whole-program optimization techniques* and at least two such techniques will be implemented in Java 9.
It will also contain a tool which analyzes a given set of modules and applies these optimizations to create a more performant binary image.

#### Annotations

Auto discovery of annotated classes (like e.g. Spring allows) currently requires to scan all classes in some specified packages.
This is usually done during a program's start and can slow it down considerably.

Modules will have an API allowing callers to identify all classes with a given annotation.
One envisioned approach is to create an index of such classes that will be created when the module is compiled \[[annotation-detection](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#efficient-annotation-detection)\].

### Integration With Existing Concepts And Tools

Diagnostic tools (e.g. stack traces) will be upgraded to convey information about modules.
Furthermore, they will be fully integrated into the reflection API, which can be used to manipulate them in the same manner as classes \[[reflection, debugging and tools](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#reflection-debugging-and-tools)\].
This will include the version information which can be reflected on and overriden at runtime \[[version strings in reflective APIs](http://openjdk.java.net/projects/jigsaw/goals-reqs/03#version-strings-in-reflective-apis), [overridable version information](http://openjdk.java.net/projects/jigsaw/goals-reqs/03#overrideable-version-information)\].

The module's design will allow build tools to be used for them "with a minimum of fuss" \[[build tools](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#integrate-smoothly-with-existing-tools)\].
The compiled form of a module will be usable on the class path or as a module so that library developers are not forced to create multiple artifacts for class-path and module-based applications \[[multi-mode artifacts](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#multi-mode-artifacts)\].

Interoperability with other module systems, most notably OSGi, is also planned \[[interoperation](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#interoperation)\].

Even though modules can hide packages from other modules it will be possible to test the contained classes and interfaces \[[white-box testing](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#white-box-testing)\].

### OS-Specific Packaging

The module system is designed with package manager file formats "such as RPM, Debian, and Solaris IPS" in mind.
Not only will developers be able to use existing tools to create OS-specific packages from a set of modules.
Such modules will also be able to call other modules that were installed with the same mechanism \[[module packaging](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#os-specific-module-packaging)\].

Developers will also be able to package a set of modules which make up an application into an OS-specific package "which can be installed and invoked by an end user in the manner that is customary for the target system".
Building on the above, only those modules which are not present on the target system have to be packaged \[[application packaging](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#os-specific-application-packaging)\].

### Dynamic Configuration

Running applications will have the possibility to create, run, and release multiple isolated module configurations \[[dynamic configuration](http://openjdk.java.net/projects/jigsaw/spec/reqs/02#dynamic-configuration)\].
These configurations can contain developer and platform modules.

This will be useful for container architectures like IDEs, application servers, or the Java EE platform.

## Reflection

We have seen most of the features Project Jigsaw will bring to Java 9.
They all revolve around the new core language concept of *modules*.

Maybe most important in day-to-day programming will be dependency management, encapsulation, and configuration across the different phases.
Improved performance is always a nice take-away.
And then there is the work invested into cooperation with existing tools and concepts, like reflection, diagnostics, build tools and OS-specific packaging.

Can't wait to try it out?
Neither can I!
But we'll have to wait until JSR 376 will have come along further before the early access releases of [JDK9](https://jdk9.java.net/download/) or [JDK 9 with Project Jigsaw](https://jdk9.java.net/jigsaw/) will actually contain the module system.
When it finally does, you'll read about it here.
