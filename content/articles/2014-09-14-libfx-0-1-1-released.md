---
title: "LibFX 0.1.1 Released"
tags: [javafx, libfx]
date: 2014-09-14
slug: libfx-0-1-1
description: "Release post for LibFX 0.1.1 including a description of `Nestings` and pointers to GitHub, Maven and the Javadoc."
searchKeywords: "libfx"
featuredImage: libfx-library
repo: libfx
---

Just today I released [LibFX 0.1.1](https://github.com/nipafx/LibFX/releases/tag/v0.1.1)!

> That's one small step for everybody else, one giant leap for me.

Or something like that...
😉
It's surely no big deal for a seasoned open source developer but since it's the very first production-ready release of my very first own open source library, it feels like quite the accomplishment to me.

So I am here to proudly present it!

## LibFX 0.1.1

So what can **LibFX** do for you?
Currently it has only one feature, namely *Nestings*.
It is [described in detail in the project's wiki](https://github.com/nipafx/LibFX/wiki/Nestings) but I will give a quick introduction here.

<contentimage slug="LibFX-v0.1.1" options="sidebar"></contentimage>

### Nestings

Nestings enhance JavaFX' properties with a neat way to capture object hierarchies.
Imagine the model for your UI has an `ObjectProperty<Employee> currentEmployee` and the employee has an `ObjectProperty<Address> addressProperty()` which in turn has a `StringProperty streetNameProperty()`.

Now let's say you create an editor that you want to use to display and edit the current employee's street name.
Note that the current employee can be replaced as well as the employee's address or the address' street name and of course you want your editor to always be up to date and point to the correct property.

With the standard FX classes you are out of luck and essentially have to implement a lot of listening to property changes and updating bindings which will soon clutter your code.

Enter **LibFX**!
Using [method references](http://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html) you can do this:

```java
StringProperty currentEmployeesStreetName = Nestings
	.on(currentEmployeeProperty)
	.nest(Employee::addressProperty)
	.nest(Address::streetNameProperty)
	.buildProperty();
```

Now you have a property which always points to current employee's current address' street name.
This is a fully functional property so you can do all the usual things with it.
In this case, you could bidirectionally bind the editor's value property to it and you're done!

## Infrastructure

But as important and surely as work-intensive as the feature was the infrastructure.
I finally got (almost) all the tools up and running.

### About LibFX

The project is hosted on [GitHub](https://github.com/nipafx/LibFX), which also offers the awesome [GitHub Pages](https://pages.github.com/).

They run the project page under [libfx.codefx.org](http://libfx.codefx.org), which is the central point for all information regarding **LibFX**.
It links to all tools, services and resources involved in creating the library, which especially include the [wiki](https://github.com/nipafx/LibFX/wiki) and the [Javadoc](http://libfx.codefx.org/javadoc/).

### Maven Coordinates

LibFX 0.1.1 is available in Maven Central and these are the coordinates:

```xml
<groupId>org.codefx.libfx</groupId>
<artifactId>LibFX</artifactId>
<version>0.1.1</version>
```

Maven Central provides [further information for **LibFX** 0.1.1](http://search.maven.org/#artifactdetails%7Corg.codefx.libfx%7CLibFX%7C0.1.1%7Cjar), which can be used for other build tools.

### Continuous Inspection and Integration

As far as I can tell at the moment, these are the only pieces missing to the puzzle.
I don't think continuous integration is very important at this stage of the project but I'm curious and would like to get to know it before it really counts.

But what I really crave is [SonarQube](http://www.sonarqube.org/)!
I want it badly!
But I'm still looking for a way to get it without breaking my budget.
As soon as I find one I'll fix every possible issue and be back here to brag about the code quality.
😁
