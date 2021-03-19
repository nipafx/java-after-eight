---
title: "Impulse: \"Adventures On The Road to Valhalla\""
tags: [java-next, impulse, project-valhalla, generics, primitive-classes]
date: 2015-08-14
slug: java-road-to-valhalla
description: "A summary of Brian Goetz' talk \"Adventures On The Road to Valhalla\" given at JVMLS in August 2015. Focused on generic specialization and the two prototypes."
intro: "A summary of Brian Goetz' talk \"Adventures On The Road to Valhalla\" given at JVMLS in August 2015. Focused on generic specialization and the two currently existing prototypes."
searchKeywords: "valhalla"
featuredImage: adventures-on-the-road-to-project-valhalla
---

With all this talk about [Java 9](http://blog.takipi.com/5-features-in-java-9-that-will-change-how-you-develop-software-and-2-that-wont/) and [Project Jigsaw](tag:project-jigsaw) we should not loose sight of another big change coming to Java.
Hopefully in version 10 or 11 [Project Valhalla](http://openjdk.java.net/projects/valhalla/) will come to fruition and introduce value types and specialization.

So what is this about, how far along is the project and what challenges does it face?
A couple of days ago Brian Goetz, Java Language Architect at Oracle and project lead for Valhalla, answered these questions in a talk at the [JVM Language Summit 2015](http://openjdk.java.net/projects/mlvm/jvmlangsummit/).

Let's have a look.

This post presents three out of four parts of Goetz's talk ["Adventures On The Road to Valhalla"](https://www.youtube.com/watch?v=uNgAFSUXuwc).
He begins with a prologue, which I padded with a couple of additional explanations for those who do not yet know about Project Valhalla.
Goetz continues to present the two prototypes, of which the first was made publicly available last year and the second only two weeks ago.
I will not cover his last part about future experiments as the post is already long enough.
If you find this topic interesting, you should definitely watch the whole talk!

All quotes throughout the text are either taken from the slides or verbatim.

## The Talk

Here's the talk:

https://www.youtube.com/watch?v=uNgAFSUXuwc

(Btw, big kudos to the JVMLS team for getting all the talks online within a couple of hours!)

If you can spare the 50 minutes, go watch it!
No need to read this post, then.

## The Gist

### Prologue

The two major topics addressed by Project Valhalla are [value types](http://cr.openjdk.java.net/~jrose/values/values-0.html) and [generic specialization](http://cr.openjdk.java.net/~briangoetz/valhalla/specialization.html).

#### Value Types

The former will allow users to define "int-like" types with the same properties (like immutability, equality instead of identity) and the performance advantages emerging from that.
They are preceded by Java 8's [value-based classes](java-value-based-classes).

(Unless otherwise noted, when the rest of this post talks about primitives, value types are included.)

#### Generic Specialization

With everybody declaring their own primitive-ish types, the problems caused by the fact that generics do not work over them (i.e.
no `ArrayList<int>`) become insufferable.
While having to box primitives is ok from a conceptual point of view, it has notable performance costs.

First of all, storing objects instead of primitives costs extra memory (e.g. for object headers).
Then, and this is worse, boxing destroys [cache locality](http://stackoverflow.com/q/12065774/2525313 "Why does cache locality matter for array performance?
- StackOverflow").
When the CPU caches an `Integer`-array, it only gets pointers to the actual values.
Fetching those is an additional random memory access.
This extra level of indirection costs dearly and potentially cripples parallelization when the CPUs are mostly waiting for cache misses.

So another goal of Project Valhalla is to expand the scope of parametric polymorphism to enable generics over primitives.
To be successful the JVM should use primitives instead of boxes for generic fields, arguments and return values in a generic class.

Because of the way it will likely be implemented, this is called *generic specialization*.

> So generics need to play nicely with value types and primitives can come along for the ride.

#### Current State Of Generics

Due to erasure, type variables are erased to their bound, i.e.
`ArrayList<Integer>` effectively becomes `ArrayList<Object>` (or rather just `ArrayList`).
Such a bound must be the supertype of all possible instantiations.
But Java has no type above primitives and reference types.

Additionally, JVM bytecode instructions are typically orthogonal, i.e.
split along the same lines.
An `aload` or `astore` can only move references.
Specialized variants have to be used for primitives, e.g. `iload` or `istore` for `int`.
There is no bytecode that can move both a reference and an `int`.

So neither the type system nor the bytecode instruction set are up to the task of generifying over primitives.
This was well understood when generics were developed over ten years ago and, as a compromise, the decision was to simply not allow it.

> Today's problems come from yesterday's solutions...

#### Compatibility!

Everything that happens under Project Valhalla has of course to be backwards compatible.
This takes several forms:

Binary Compatibility
:   Existing bytecode, i.e.
compiled class files, must continue to mean the same thing.
This ensures that dependencies continue to work without having to be recompiled.

Source Compatibility
:   Source files must continue to mean exactly the same thing, so recompiling them must not change anything "just because the language has changed".

Migration Combatibility
:   Compiled classes from different Java versions must work together to allow migrating one dependency at a time.

An additional requirement is to not make the JVM mimic the Java language in too many details.
Doing so would force other JVM languages to deal with semantics of the Java language.

### Prototype Model 1: Making It Work

About a year ago Goetz and his colleagues presented the first experimental implementation of specialization.

#### The Idea

In this prototype the compiler continues to produce erased classfiles but augments them with additional type information.

This information is ignored by the VM but will be used by the *specializer*, which is a new part of the class loader.
The latter will recognizes when a class with a primitive type parameter is required and let the specializer generate it on the fly from the erased but augmented classfile.

With erasure, all generic instantiations of a class use the same classfile.
In contrast, creating a new classfile for each primitive type is called *specialization*.

#### The Details

In this prototype specialized classes are described with a "name-mangling technique".
The class name is appended with a string that denotes which type argument is specialized to which primitive.
E.g. `ArrayList${0=I}` means "`ArrayList` instantiated with first type variable `int`".

During specialization the signatures *and* the bytecode have to be changed.
To do this correctly the specializer needs to know which of the occurrences of `Object` (to which all generic types were erased) have to be specialized to which type.
The required signature information were already mostly present in the classfile and the prototype annotates the bytecode with the additional type metadata.

From [8:44](https://www.youtube.com/watch?v=uNgAFSUXuwc#t=8m44s) on Goetz gives a couple of examples of how this plays out.
He also uses them to point to some of the details that such an implementation would have to be aware of, like the topic of generic methods.

> I know that was a lot of fast hand-waving.
The point is, this is straight-forward enough but there is lots of fiddly little bits of complexity.

#### The Summary

This experiment shows that on-the-fly specialization based on classfile metadata works without changes to the VM.
These are important achievements but there are prohibitive disadvantages.

First, it requires the implementation of a complicated set of details.

Second and maybe most importantly, it has problematic type system characteristics.
Without changes to the VM there is still no common supertype of `int` and `String` and hence no common supertype of `ArrayList<int>` and `ArrayList<String>`.
This means there is no way to declare "any instantiation of `ArrayList`".

Third, this has terrible code sharing properties.
Even though much of the code of `ArrayList<int>` and `ArrayList<String>` is identical, it would be duplicated in `ArrayList${0=I}` and `ArrayList`.

> Death by 1000 cuts.

### Prototype Model 2: Rescuing Wildcards

The second, and [very new](http://mail.openjdk.java.net/pipermail/valhalla-dev/2015-July/001245.html) prototype addresses the problematic type system characteristics.

#### The Problem

Currently, unbounded wildcards express "any instantiation of a class", e.g. `ArrayList<?>` means "any `ArrayList`".
They are heavily used, especially by library developers.
In a system where `ArrayList<int>` and `ArrayList<String>` are different classes, wildcards may be even more important as they bridge the gap between them "and express the basic `ArrayList`-ness".

But if we assume `ArrayList<?>` were a supertype to `ArrayList<int>`, we'd end up in situations where we require multiple inheritance of classes.
The reason is that `ArrayList<T>` extends `AbstractList<T>` so we'd also want `ArrayList<int>` to extend `AbstractList<int>`.
Now `ArrayList<int>` would extend both `ArrayList<?>` and `AbstractList<int>` (which have no inheritance relationship).

<contentimage slug="adventures-on-the-road-to-project-valhalla-multiple-inheritance" options="bg"></contentimage>

(Note the difference to the current generics with erasure.
In the VM, `ArrayList<Integer>` and `ArrayList<?>` are the same class `ArrayList`, which is free to extend `AbstractList`.)

The root cause is that while `ArrayList<?>` might look like it means "any `ArrayList`" it actually means `ArrayList<?
extends Object>`, i.e.
"any `ArrayList` over reference types".

#### The Idea

The prototype introduces a new hierarchy of wildcards with `ref`, `val`, and `any`:

-   `ref` comprises all reference types and replaces `?`
-   `val` comprises all primitives and value types (this is not currently supported by the prototype and not mentioned in the talk but was [announced on the Valhalla mailing list](http://mail.openjdk.java.net/pipermail/valhalla-dev/2015-August/001256.html))
-   `any` contains both `ref` and `val`

The multiple inheritance of specialized classes will be solved by representing the any-types with synthetic interfaces.
`ArrayList<int>` will thus extend `AbstractList<int>` and implement `ArrayList<any>`.

#### The Details

##### Hierarchy

`ArrayList<ref>`, which is `ArrayList<?>`, will continue to be the erased type.

To represent `ArrayList<any>` the compiler will create an interface `ArrayList$any`.
It will be implemented by all classes generated from `ArrayList` (e.g. `ArrayList<int>` and the erased `ArrayList`) and will extend all the synthetic interfaces that correspond to the superclasses, e.g. `AbstractList$any` for `AbstractList<any>`.

<contentimage slug="adventures-on-the-road-to-project-valhalla-any-interface" options="bg"></contentimage>

The interface will contain declarations for all of the class's methods and accessors for its fields.
Because there is still no common supertype to objects and primitives, their generic parameter and return types would have to be boxed.

But this detour would only have to taken if the class is accessed as `ArrayList<any>` whereas the access is direct for, e.g., `ArrayList<int>`.
So the performance cost of boxing is only borne by those developers using wildcards, while code using primitive specializations directly gets the improved performance it expects.

> It works prety cleanly.
>
> You shouldn't believe me, it gets complicated.
But it's a good story.
We'll keep going.

From [26:33](https://www.youtube.com/watch?v=uNgAFSUXuwc#t=26m33s) on Goetz starts giving examples to explain some details.

##### Accessibility

Accessibility is an area where the VM needs to change.
Up to now, interfaces can not have private or package visible methods.
(In Java 9 [private default methods will be possible](https://bugs.openjdk.java.net/browse/JDK-8071453) but that doesn't help here because the need to have an implementation.)

A connected but much older problem is that an outer class and its inner classes can access each others private members even though the VM does not allow that because to it these are all unrelated classes.
This is currently solved by generating bridge methods, i.e.
methods with a higher visibility that will then be called instead of the inaccessible private members.

Creating even more bridge methods for specialized classes would be possible but unwieldly.
Instead a possible change is to create the notion of a *nest* of classes.
It would contain all specialized and inner classes and the VM would allow access of private members inside a nest.

This would align the interpretation of the language, which sees a class with all its specializations and inner classes as one unit, and of the VM, which up to now only sees a bunch of unrelated classes.

##### Arrays

Generic methods might also take or return arrays.
But while specialization can box an `int` to an `Object`, an `int[]` is no `Object[]` and boxing each individual `int` is a terrible idea.

[Arrays 2.0](http://cr.openjdk.java.net/%7Ejrose/pres/201207-Arrays-2.pdf) might come to the rescue here.
Because the discussion requires a basic familiarity with the proposal I will not go into details.
In summary, it looks like they will solve the problem.

#### The Summary

The changes to the language are conceptually simple.
In the absence of `any` nothing changes.
Type variables can be decorated with `any` and if such an instance needs to be assigned to a wildcarded type, the wildcard has to use `any` as well.

With the common supertype to generic classes across primitive and reference types, e.g. `ArrayList<any>`, the resulting programming model is way more reasonable.
Talking about his team's experience with porting the Stream API to this prototype, Goetz says:

> It's just really smooth.
It's exactly what you want.
About 70% of the code just evaporates because all of the hand-specialized primitive stuff just goes away and then a lot of the complex machinery to support the hand-specialization, that goes away, and it becomes this simple library a third year student could write.
So we consider that a pretty successful experiment.

There is also excellent compatibility with existing code.

Unfortunately, the bad code sharing properties of the first prototype remain.
`ArrayList<int>` and `ArrayList<String>` are still different classes that are very similar but share no code.
The next part, which I will not cover in this post, addresses that and presents possible approaches to solving this problem.

## Reflection

The talk is very dense and covers a lot of ground.
We have seen that the introduction of value types and desired performance improvements require generic specialization so boxing can be reduced or even prevented.

The first prototype achieves this without JVM changes by specializing classes when they are loaded.
But it has the problem that there is no common supertype to all instantiations of a class because primitive and reference type parameters yield entirely unrelated classes.
The second prototype introduces the wildcards `ref`, `val`, and `any` and uses synthetic interfaces to to denote any-types.

This is all very exciting and I can't wait to try it out!
Unfortunately, I'm going on a holiday so I can't for a while.
Stupid real life... Don't wreck things while I'm gone!
