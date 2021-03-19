---
title: "Jigsaw Hands-On Guide"
tags: [java-next, project-jigsaw]
date: 2015-12-25
slug: jigsaw-hands-on-guide
canonicalUrl: https://www.javaadvent.com/2015/12/project-jigsaw-hands-on-guide.html
canonicalText: "I originally wrote this post for the Java Advent Calendar, where it was [published on December 10th, 2015](https://www.javaadvent.com/2015/12/project-jigsaw-hands-on-guide.html)."
description: "A Jigsaw tutorial explaining how to create modules, state dependencies between them, and use the module system as a service locator to decouple modules."
searchKeywords: "Jigsaw tutorial"
featuredImage: jigsaw-hands-on-guide-advent
---

Project Jigsaw will bring modularization to the Java platform and according to [the original plan](http://mail.openjdk.java.net/pipermail/jdk9-dev/2015-May/002172.html) it was going to be feature complete on the 10th of December.
So here we are but where is Jigsaw?

Surely a lot happened in the last six months: The [prototype came out](http://openjdk.java.net/projects/jigsaw/ea), the looming removal of internal APIs [caused quite a ruckus](http://blog.dripstat.com/removal-of-sun-misc-unsafe-a-disaster-in-the-making/), the [mailing list](http://mail.openjdk.java.net/mailman/listinfo/jigsaw-dev) is full of [critical discussions](http://mail.openjdk.java.net/pipermail/jigsaw-dev/2015-December/005511.html) about the project's design decisions, and JavaOne saw [a series of great introductory talks](tag:community) by the Jigsaw team.
And [then Java 9 got delayed for half year](delay-of-java-9-release) due to Jigsaw.

But let's ignore all of that for now and just focus on the code.
In this post we'll take an existing demo application and modularize it with Java 9.
If you want to follow along, head over [to GitHub](https://github.com/nipafx/demo-jigsaw-advent-calendar), where all of the code can be found.
The [setup instructions](https://github.com/nipafx/demo-jigsaw-advent-calendar/tree/master#setup) are important to get the scripts running with Java 9.
For brevity, I removed the prefix `org.codefx.demo` from all package, module, and folder names in this article.

## The Application Before Jigsaw

Even though I do my best to ignore the whole Christmas kerfuffle, it seemed prudent to have the demo uphold the spirit of the season.
So it models an advent calendar:

-   There is a calendar, which has 24 calendar sheets.
-   Each sheet knows its day of the month and contains a surprise.
-   The death march towards Christmas is symbolized by printing the sheets (and thus the surprises) to the console.

Of course the calendar needs to be created first.
It can do that by itself but it needs a way to create surprises.
To this end it gets handed a list of surprise factories.
This is what [the `main` method](https://github.com/nipafx/demo-jigsaw-advent-calendar/blob/00-before-jigsaw/src/org.codefx.demo.advent/org/codefx/demo/advent/Main.java#L11-L18) looks like:

```java
public static void main(String[] args) {
	List<SurpriseFactory> surpriseFactories = Arrays.asList(
			new ChocolateFactory(),
			new QuoteFactory()
	);
	Calendar calendar =
		Calendar.createWithSurprises(surpriseFactories);
	System.out.println(calendar.asText());
}
```

The [initial state of the project](https://github.com/nipafx/demo-jigsaw-advent-calendar/tree/00-before-jigsaw) is by no means the best of what is possible before Jigsaw.
Quite the contrary, it is a simplistic starting point.
It consists of a single module (in the abstract sense, not the Jigsaw interpretation) that contains [all required types](https://github.com/nipafx/demo-jigsaw-advent-calendar/tree/00-before-jigsaw/src/org.codefx.demo.advent/org/codefx/demo/advent):

-   "Surprise API" - `Surprise` and `SurpriseFactory` (both are interfaces)
-   "Calendar API" - `Calendar` and `CalendarSheet` to create the calendar
-   Surprises - a couple of `Surprise` and `SurpriseFactory` implementations
-   Main - to wire up and run the whole thing.

[Compiling and running](https://github.com/nipafx/demo-jigsaw-advent-calendar/blob/00-before-jigsaw/compileAndRun.sh) is straight forward (commands for Java 8):

```shell
# compile
javac -d classes/advent ${source files}
# package
jar -cfm jars/advent.jar ${manifest and compiled class files}
# run
java -jar jars/advent.jar
```

## Entering Jigsaw Land

The [next step](https://github.com/nipafx/demo-jigsaw-advent-calendar/tree/01-creating-a-module) is small but important.
It changes nothing about the code or its organization but moves it into a Jigsaw module.

### Modules

So what's a module?
To quote the highly recommended [State of the Module System](http://openjdk.java.net/projects/jigsaw/spec/sotms/):

> A *module* is a named, self-describing collection of code and data.
Its code is organized as a set of packages containing types, i.e., Java classes and interfaces; its data includes resources and other kinds of static information.
>
> To control how its code refers to types in other modules, a module declares which other modules it *requires* in order to be compiled and run.
To control how code in other modules refers to types in its packages, a module declares which of those packages it *exports*.

(The last paragraph is actually from an old version of the document but I like how it summarizes dependencies and exports.)

So compared to a JAR a module has a name that is recognized by the JVM, declares which other modules it depends on and defines which packages are part of its public API.

#### Name

A module's name can be arbitrary.
But to ensure uniqueness it is recommended to stick with the inverse-URL naming schema of packages.
So while this is not necessary it will often mean that the module name is a prefix of the packages it contains.

#### Dependencies

A module lists the other modules it depends on to compile and run.
This is true for application and library modules but also for modules in the JDK itself, which was split up into about 100 of them (have a look at them with `java --list-modules`).

Again from the design overview:

> When one module depends directly upon another in the module graph then code in the first module will be able to refer to types in the second module.
We therefore say that the first module *reads* the second or, equivalently, that the second module is *readable* by the first.
>
> \[...\]
>
> The module system ensures that every dependence is fulfilled by precisely one other module, that the module graph is acyclic, that every module reads at most one module defining a given package, and that modules defining identically-named packages do not interfere with each other.

When any of the properties is violated, the module system refuses to compile or launch the code.
This is an immense improvement over the brittle classpath, where e.g. missing JARs would only be discovered at runtime, crashing the application.

It is also worth to point out that a module is only able to access another's types if it directly depends on it.
So if *A* depends on *B*, which depends on *C*, then *A* is unable to access *C* unless it requires it explicitly.

#### Exports

A module lists the packages it exports.
Only public types in these packages are accessible from outside the module.

This means that `public` is no longer really public.
A public type in a non-exported package is as inaccessible to the outside world as a non-public type in an exported package.
Which is even more inaccessible than package-private types are before Java 9 because the module system does not even allow reflective access to them.
As Jigsaw is currently implemented [command line flags](https://www.sitepoint.com/reflection-vs-encapsulation-in-the-java-module-system/#commandlineescapehatches) are the only way around this.

### Implementation

To be able to create a module, the project needs a `module-info.java` in its root source directory:

```java
module advent {
	// no imports or exports
}
```

Wait, didn't I say that we have to declare dependencies on JDK modules as well?
So why didn't we mention anything here?
All Java code requires `Object` and that class, as well as the few others the demo uses, are part of the module `java.base`.
So literally *every* Java module depends on `java.base`, which led the Jigsaw team to the decision to automatically require it.
So we do not have to mention it explicitly.

The biggest change is the script to compile and run (commands for Java 9):

```shell
# compile (include module-info.java)
javac -d classes/advent ${source files}
# package (add module-info.class and specify main class)
jar --create \
	--file=mods/advent.jar \
	--main-class=advent.Main \
	${compiled class files}
# run (specify a module path and simply name to module to run)
java --module-path mods --module advent
```

We can see that compilation is almost the same - we only need to include the new `module-info.java` in the list of classes.

The jar command will create a so-called *modular JAR*, i.e.
a JAR that contains a module.
Unlike before we need no manifest anymore but can specify the main class directly.
Note how the JAR is created in the directory `mods`.

Utterly different is the way the application is started.
The idea is to tell Java where to find the application modules (with `--module-path mods`, this is called the *module path*) and which module we would like to launch (with `--module advent`).

## Splitting Into Modules

Now it's time to really get to know Jigsaw and [split that monolith up](https://github.com/nipafx/demo-jigsaw-advent-calendar/tree/02-splitting-into-modules) into separate modules.

### Made-up Rationale

The "surprise API", i.e.
`Surprise` and `SurpriseFactory`, is a great success and we want to separate it from the monolith.

The factories that create the surprises turn out to be very dynamic.
A lot of work is being done here, they change frequently and which factories are used differs from release to release.
So we want to isolate them.

At the same time we plan to create a large Christmas application of which the calendar is only one part.
So we'd like to have a separate module for that as well.

We end up with these modules:

-   *surprise* - `Surprise` and `SurpriseFactory`
-   *calendar* - the calendar, which uses the surprise API
-   *factories* - the `SurpriseFactory` implementations
-   *main* - the original application, now hollowed out to the class `Main`

Looking at their dependencies we see that *surprise* depends on no other module.
Both *calendar* and *factories* make use of its types so they must depend on it.
Finally, *main* uses the factories to create the calendar so it depends on both.

<contentimage slug="jigsaw-hands-on-splitting-into-modules" options="bg"></contentimage>

### Implementation

The first step is to reorganize the source code.
We'll stick with the directory structure as proposed by the [official quick start guide](http://openjdk.java.net/projects/jigsaw/quick-start) and have all of our modules in their own folders below `src`:

```shell
src
  - advent.calendar: the "calendar" module
	  - org ...
	  module-info.java
  - advent.factories: the "factories" module
	  - org ...
	  module-info.java
  - advent.surprise: the "surprise" module
	  - org ...
	  module-info.java
  - advent: the "main" module
	  - org ...
	  module-info.java
.gitignore
compileAndRun.sh
LICENSE
README
```

To keep this readable I truncated the folders below `org`.
What's missing are the packages and eventually the source files for each module.
See it [on GitHub](https://github.com/nipafx/demo-jigsaw-advent-calendar/tree/02-splitting-into-modules) in its full glory.

Let's now see what those module infos have to contain and how we can compile and run the application.

#### *surprise*

There are no required clauses as *surprise* has no dependencies.
(Except for `java.base`, which is always implicitly required.) It exports the package `advent.surprise` because that contains the two classes `Surprise` and `SurpriseFactory`.

So the `module-info.java` looks as follows:

```java
module advent.surprise {
	// requires no other modules
	// publicly accessible packages
	exports advent.surprise;
}
```

Compiling and packaging is very similar to the previous section.
It is in fact even easier because *surprise* contains no main class:

```shell
# compile
javac -d classes/advent.surprise ${source files}
# package
jar --create --file=mods/advent.surprise.jar ${compiled class files}
```

#### *calendar*

The calendar uses types from the surprise API so the module must depend on *surprise*.
Adding `requires advent.surprise` to the module achieves this.

The module's API consists of the class `Calendar`.
For it to be publicly accessible the containing package `advent.calendar` must be exported.
Note that `CalendarSheet`, private to the same package, will not be visible outside the module.

But there is an additional twist: We just made [`Calendar.createWithSurprises(List<SurpriseFactory>)`](https://github.com/nipafx/demo-jigsaw-advent-calendar/blob/02-splitting-into-modules/src/org.codefx.demo.advent.calendar/org/codefx/demo/advent/calendar/Calendar.java#L22) publicly available, which exposes types from the *surprise* module.
So unless modules reading *calendar* also require *surprise*, Jigsaw will prevent them from accessing these types, which would lead to compile and runtime errors.

Marking the requires clause as `transitive` fixes this.
With it any module that depends on *calendar* also reads *surprise*.
This is called [*implied readability*](java-modules-implied-readability).

The final module-info looks as follows:

```java
module advent.calendar {
	// required modules
	requires transitive advent.surprise;
	// publicly accessible packages
	exports advent.calendar;
}
```

Compilation is almost like before but the dependency on *surprise* must of course be reflected here.
For that it suffices to point the compiler to the directory `mods` as it contains the required module:

```shell
# compile (point to folder with required modules)
javac --module-path mods \
	-d classes/advent.calendar \
	${source files}
# package
jar --create \
	--file=mods/advent.calendar.jar \
	${compiled class files}
```

#### *factories*

The factories implement `SurpriseFactory` so this module must depend on *surprise*.
And since they return instances of `Surprise` from published methods the same line of thought as above leads to a `requires transitive` clause.

The factories can be found in the package `advent.factories` so that must be exported.
Note that the public class `AbstractSurpriseFactory`, which is found in another package, is not accessible outside this module.

So we get:

```java
module advent.factories {
	// required modules
	requires transitive advent.surprise;
	// publicly accessible packages
	exports advent.factories;
}
```

Compilation and packaging is analog to *calendar*.

#### *main*

Our application requires the two modules *calendar* and *factories* to compile and run.
It still has no API to export.

```java
module advent {
	// required modules
	requires advent.calendar;
	requires advent.factories;
	// no exports
}
```

Compiling and packaging is like with last section's single module except that the compiler needs to know where to look for the required modules:

```shell
#compile
javac --module-path mods \
	-d classes/advent \
	${source files}
# package
jar --create \
	--file=mods/advent.jar \
	--main-class=advent.Main \
	${compiled class files}
```

With all the modules in `mods` , we can run the calendar.

```shell
# run
java --module-path mods --module advent
```

## Services

Jigsaw enables loose coupling by implementing the [service locator pattern](https://en.wikipedia.org/wiki/Service_locator_pattern), where the module system itself acts as the locator.
Let's see [how that goes](https://github.com/nipafx/demo-jigsaw-advent-calendar/tree/03-services).

### Made-up Rationale

Somebody recently read a blog post about how cool loose coupling is.
Then she looked at our code from above and complained about the tight relationship between *main* and *factories*.
Why would *main* even know *factories*?

Because...

```java
public static void main(String[] args) {
	List<SurpriseFactory> surpriseFactories = Arrays.asList(
			new ChocolateFactory(),
			new QuoteFactory()
	);
	Calendar calendar =
		Calendar.createWithSurprises(surpriseFactories);
	System.out.println(calendar.asText());
}
```

Really?
Just to instantiate some implementations of a perfectly fine abstraction (the `SurpriseFactory`)?

And we know she's right.
Having someone else provide us with the implementations would remove the direct dependency.
Even better, if said middleman would be able to find *all* implementations on the module path, the calendar's surprises could easily be configured by adding or removing modules before launching.

this is exactly what services are there for!
We can have a module specify that it provides implementations of an interface.
Another module can express that it uses said interface and find all implementations with the `ServiceLocator`.

We use this opportunity to split *factories* into *chocolate* and *quote* and end up with these modules and dependencies:

-   *surprise* - `Surprise` and `SurpriseFactory`
-   *calendar* - the calendar, which uses the surprise API
-   *chocolate* - the `ChocolateFactory` as a service
-   *quote* - the `QuoteFactory` as a service
-   *main* - the application; no longer requires individual factories

<contentimage slug="jigsaw-hands-on-services" options="bg"></contentimage>

### Implementation

The first step is to reorganize the source code.
The only change from before is that `src/advent.factories` is replaced by `src/advent.factory.chocolate` and `src/advent.factory.quote`.

Lets look at the individual modules.

#### *surprise* and *calendar*

Both are unchanged.

#### *chocolate* and *quote*

Both modules are identical except for some names.
Let's look at *chocolate* because it's more yummy.

As before with *factories* the module `requires transitive` the *surprise* module.

More interesting are its exports.
It provides an implementation of `SurpriseFactory`, namely `ChocolateFactory`, which is specified as follows:

```java
provides advent.surprise.SurpriseFactory
	with advent.factory.chocolate.ChocolateFactory;
```

Since this class is the entirety of its public API it does not need to export anything else.
Hence no other export clause is necessary.

We end up with:

```java
module advent.factory.chocolate {
	// list the required modules
	requires transitive advent.surprise;
	// specify which class provides which service
	provides advent.surprise.SurpriseFactory
		with advent.factory.chocolate.ChocolateFactory;
}
```

Compilation and packaging is straight forward:

```shell
javac --module-path mods \
	-d classes/advent.factory.chocolate \
	${source files}
jar --create \
	--file mods/advent.factory.chocolate.jar \
	${compiled class files}
```

#### *main*

The most interesting part about *main* is how it uses the ServiceLocator to find implementation of SurpriseFactory.
From [its main method](https://github.com/nipafx/demo-jigsaw-advent-calendar/blob/03-services/src/org.codefx.demo.advent/org/codefx/demo/advent/Main.java#L13-L14):

```java
List<SurpriseFactory> surpriseFactories = new ArrayList<>();
ServiceLoader.load(SurpriseFactory.class)
	.forEach(surpriseFactories::add);
```

Our application now only requires *calendar* but must specify that it uses `SurpriseFactory`.
It has no API to export.

```java
module advent {
	// list the required modules
	requires advent.calendar;
	// list the used services
	uses advent.surprise.SurpriseFactory;
	// exports no functionality
}
```

Compilation and execution are like before.

And we can indeed change the surprises the calendar will eventually contain by simply removing one of the factory modules from the module path.
Neat!

## Summary

So that's it.
We have seen how to move a monolithic application into a single module and how we can split it up into several.
We even used a service locator to decouple our application from concrete implementations of services.
All of this is [on GitHub](https://github.com/nipafx/demo-jigsaw-advent-calendar) so check it out to see more code!

But there is lots more to talk about!
Jigsaw brings [a couple of incompatibilities](how-java-9-and-project-jigsaw-may-break-your-code) but also the means to solve many of them.
And we haven't talked about [how reflection interacts with the module system](java-modules-reflection-vs-encapsulation) and how to migrate external dependencies.

If these topics interest you, watch [this tag](tag:project-jigsaw) as I will surely write about them over the coming months.
