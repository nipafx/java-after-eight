---
title: "Motivation And Goals Of Project Jigsaw"
tags: [java-next, java-9, project-jigsaw]
date: 2015-06-18
slug: motivation-goals-project-jigsaw
description: "A look at how Project Jigsaw (coming in Java 9) aims to solve JAR/classpath hell and at its goals to improve security, maintainability and performance."
intro: "A look at how Project Jigsaw aims to solve JAR/classpath hell and at its goals to improve encapsulation, security, maintainability and performance as well as creating a modular JDK."
searchKeywords: "Project Jigsaw"
featuredImage: project-jigsaw-2-goals
---

A couple of weeks ago I wrote about [how Project Jigsaw may break existing code](how-java-9-and-project-jigsaw-may-break-your-code).
So what do we get in return?
Let's look at the pain points the project addresses and its goals for how to solve them in Java 9.

We first cover the pain points which motivated the creation of Project Jigsaw before looking at the project's goals.
The main sources are [JSR 376](https://www.jcp.org/en/jsr/detail?id=376) and the talk [Java 9, And Beyond](http://www.infoq.com/presentations/java-9-10), given by Mark Reinhold (chief architect of the Java Platform Group at Oracle) at EclipseCon 2015.

## Pain Points

There are a couple of pain points Project Jigsaw is aimed to solve.

### JAR/Classpath Hell

I've written a dedicated [article about JAR hell](jar-hell) and there is no need to repeat it all.
This problem shows itself when the runtime resolves dependencies differently from how the developer assumed it would.
This can lead to, e.g., running the wrong version of a library.
Finding what caused this can be extremely unpleasant (hence the upbeat term).

This happens because of the way the Java runtime loads classes.
The mechanism is fragile (e.g. depends on order), possibly complex (e.g. with multiple nested class loaders) and hence easy to get wrong.
Additionally, the runtime has no way to analyze which classes are needed so unfulfilled dependencies will only be discovered at runtime.

It is also not generally possible to fulfill dependencies on different versions of the same library.

### Weak Encapsulation Across Packages

Java's visibility modifiers are great to implement encapsulation between classes in the same package.
But across package boundaries there is only one visibility: `public`.

Since a class loader folds all loaded packages into one big ball of mud, all public classes are visible to all other classes.
There is hence no way to create functionality which is visible throughout a whole JAR but not outside of it.

This makes it very hard to properly modularize a system.
If some functionality is required by different parts of a module (e.g. a library or a sub-project of your system) but should not be visible outside of it, the only way to achieve this is to put them all into one package (so package visibility can be used).
This effectively removes any structure the code might have had before.

### Manual Security

An immediate consequence of weak encapsulation across package boundaries is that security relevant functionality will be exposed to all code running in the same environment.
This means that malicious code can access critical functionality which may allow it to circumvent security measures.

Since Java 1.1 this was prevented by a hack: `java.lang.SecurityManager.checkPackageAccess` is invoked on every code path into security relevant code and checks whether the access is allowed.
Or more precisely: it should be invoked on every such path.
Forgetting these calls lead to some of the vulnerabilities, which plagued Java in the past.

### Startup Performance

It currently takes a while before the Java runtime has loaded all required classes and just-in-time compiled the often used ones.

One reason is that class loading executes a linear scan of all JARs on the class path.
Similarly, identifying all occurrences of a specific annotation requires to inspect all classes on the class path.

### Rigid Java Runtime

Before Java 8 there was no way to install a subset of the JRE.
All Java installations had support for, e.g., XML, SQL and Swing which many use cases do not require at all.

While this may be of little relevance for medium sized computing devices (e.g. desktop PCs or laptops) it is obviously important for the smallest devices like routers, TV-boxes, cars and all the other nooks and crannies where Java is used.
With the current trend of containerization it may also gain relevance on servers, where reducing an image's footprint will reduce costs.

Java 8 brought [compact profiles](https://docs.oracle.com/javase/8/docs/technotes/guides/compactprofiles/compactprofiles.html), which define three subsets of Java SE.
They alleviate the problem but do not solve it.
Compact profiles are fixed and hence unable to cover all current and future needs for partial JREs.

## Goals Of Project Jigsaw

Project Jigsaw aims to solve the problems discussed above by introducing a language level mechanism to modularize large systems.
This mechanism will be used on the JDK itself and is also available to developers to use on their own projects.
(More details on the planned features in the next post.)

It is important to note that not all goals are equally important to the JDK and to us developers.
Many are more relevant for the JDK and most will not have a huge impact on day to day coding (unlike, e.g., lambda expressions or [default methods](java-default-methods-guide)).
They will still change the way how big projects are developed and deployed.

### Reliable Configuration

The individual modules will declare their dependencies on other modules.
The runtime will be able to analyze these dependencies at compile-time, build-time and launch-time and can thus fail fast for missing or conflicting dependencies.

### Strong Encapsulation

One of the key goals of Project Jigsaw is to enable modules to only export specific packages.
All other packages are private to the module.

> A class that is private to a module should be private in exactly the same way that a private field is private to a class.
> In other words, module boundaries should determine not just the visibility of classes and interfaces but also their accessibility.
>
> [Mark Reinhold - Project Jigsaw: Bringing the big picture into focus](http://mreinhold.org/blog/jigsaw-focus)

Dependencies of modules on libraries or other modules can also be kept private.
It is hence possible for two modules to use different versions of the same library, each keeping its dependency on that code to itself.
The runtime will then keep the versions separate and thus prevent conflicts.

### Improved Security And Maintainability

The strong encapsulation of module internal APIs can greatly improve security and maintainability.

It will help with security because critical code is now effectively hidden from code which does not require to use it.
It makes maintenance easier as a module's public API can more easily be kept small.

> Casual use of APIs that are internal to Java SE Platform implementations is both a security risk and a maintenance burden.
The strong encapsulation provided by the proposed specification will allow components that implement the Java SE Platform to prevent access to their internal APIs.
>
> [JSR 376](https://www.jcp.org/en/jsr/detail?id=376)

### Improved Performance

With clearer bounds of where code is used, existing optimization techniques can be used more effectively.

> Many ahead-of-time, whole-program optimization techniques can be more effective when it is known that a class can refer only to classes in a few other specific components rather than to any class loaded at run time.
>
> [JSR 376](https://www.jcp.org/en/jsr/detail?id=376)

It might also be possible to index code with regards to the existing annotations so that such classes can be found without a full class path scan.

### Scalable Platform

With the JDK being modularized, users will have the possibility to cherry pick the functionality they need and create their own JRE consisting of only the required modules.
This will maintain Java's position as a key player for small devices as well as for containers.

> The proposed specification will allow the Java SE Platform, and its implementations, to be decomposed into a set of components which can be assembled by developers into custom configurations that contain only the functionality actually required by an application.
>
> [JSR 376](https://www.jcp.org/en/jsr/detail?id=376)

## Reflection

We have seen that Java suffers from some problems with the way classes are loaded, encapsulation in the large and an ever growing, rigid runtime.
Project Jigsaw aims to solve this by introducing a modularization mechanism which will be applied to the JDK and will also be available to users.

It promises reliable configuration and strong encapsulation which can make JAR/classpath hell a thing of the past.
It can be used to improve security, maintainability and performance.
Last not least, this will allow users to create a Java runtime specific for their own needs.

The next post in this series will discuss the features Project Jigsaw will bring to Java 9.
Stay tuned!
If you like what I'm writing about, why don't you follow me?

Got any questions or comments about this post or Project Jigsaw in general?
Feel free to leave a comment or ping me wherever you find me.
