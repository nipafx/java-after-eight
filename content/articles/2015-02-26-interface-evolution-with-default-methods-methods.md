---
title: "Interface Evolution With Default Methods - Part I: Methods"
tags: [default-methods, java-8, patterns]
date: 2015-02-26
slug: java-default-methods-interface-evolution
description: "Patterns for interface evolution with default methods: gradually add, replace and remove interface methods without breaking client code."
searchKeywords: "interface evolution"
featuredImage: interface-evolution-with-default-methods-I
---

A couple of weeks back we took [a detailed look into default methods](java-default-methods-guide) - a feature introduced in Java 8 which allows to give interface methods an implementation, i.e. a method body, and thus define behavior in an interface.
This feature was introduced [to enable interface evolution](java-default-methods-guide#interface-evolution).

In the context of the JDK this meant adding new methods to interfaces without breaking all the code out there.
But while Java itself is extremely committed to keeping backwards compatibility, the same is not necessarily true for other projects.
If those are willing, they can evolve their interfaces at the cost of having clients change their code.

Before Java 8 this often involved client-side compile errors so changes were avoided or clients had to migrate in one go.
With default methods interface evolution can become an error free process where clients have time between versions to update their code step by step.
This greatly increases the feasibility of evolving interfaces and makes it a regular library development tool.

Let's have a look at how this is possible for adding, replacing and removing interface methods.
A [future post](java-default-methods-interface-evolution-failure) will look into ways to replace whole interfaces.
The post first defines some terminology before covering ways to add, replace and remove interface methods.
It is written from the perspective of a developer who changes an interface in her library.

## Terminology

Interfaces have **implementations** and **callers**.
Both can exist within the library, in which case they are called **internal**, or in client code, called **external**.
This adds up to four different categories of using an interface.

Depending on how the interface is to be evolved and which uses exist different patterns have to be applied.
Of course if neither external implementations nor external callers exist, none of this is necessary so the rest of the article assumes that at least one of those cases do exist.

## Interface Evolution - Methods

So let's see how we can add, replace or remove interface methods without breaking client code.

This is generally possible by following this process:

**New Version**:
A new version of the library is released where the interface definition is transitional and combines the old as well as the new, desired outline.
Default methods ensure that all external implementations and calls are still valid and no compile errors arise on an update.

**Transition**:
Then the client has time to move from the old to the new outline.
Again, the default methods ensure that adapted external implementations and calls are valid and the changes are possible without compile errors.

**New Version**:
In a new version, the library removes residues of the old outline.
Given the client used her time wisely and made the necessary changes, releasing the new version will not cause compile errors.

This process enables clients to update their code smoothly and on their own schedule which makes interface evolution much more feasible than it used to be.

When following the detailed steps below, make sure to check when internal and external implementations are updated and when internal and external callers are allowed to use the involved method(s).
Make sure to follow this procedure in your own code and properly document it for your clients so they know when to do what.
The Javadoc tags [**@Deprecated**](http://docs.oracle.com/javase/1.5.0/docs/guide/javadoc/deprecation/deprecation.html) and [**@apiNote**](javadoc-tags-apiNote-implSpec-implNote) are a good way to do that.

It is not generally necessary to perform the steps within the transition in that order.
If it is, this is explicitly pointed out.

Tests are included in these steps for the case that you provide your customers with tests which they can run on their interface implementations.

### Add

This process is only necessary if external interface implementations exist.
Since the method is new, it is of course not yet called, so this case can be ignored.
It makes sense to distinguish whether a [reasonable default implementation](java-default-methods-guide#classification) can be provided or not.

#### Reasonable Default Implementation Exists

**New Version**:
* define tests for the new method
* add the method with the default implementation (which passes the tests)
* internal callers can use the method
* internal implementations can override the method where necessary

**Transition**:
* external callers can use the method
* external implementations can override the method where necessary

Nothing more needs to be done and there is no new version involved.
This is what happened with the many new default methods which were added in Java 8.

#### Reasonable Default Implementation Does Not Exists

**New Version**:
* define tests for the new method; these must accept `UnsupportedOperationException`s
* add the method:
	* include a default implementation which throws an `UnsupportedOperationException` (this passes the tests)
	* `@apiNote` comment documents that the default implementation will eventually be removed
* override the method in all internal implementations

**Transition**:
The following steps must happen in that order:
* external implementations must override the method
* external callers can use the method

**New Version**:
* tests no longer accept `UnsupportedOperationException`s
* make the method abstract:
	* remove the default implementation
	* remove the `@apiNote` comment
* internal callers can use the method

The barely conformant default implementation allows external implementations to update gradually.
Note that all implementations are updated before the new method is actually called either internally or externally.
Hence no `UnsupportedOperationException` should ever occur.

### Replace

In this scenario a method is replaced by another.
This includes the case where a method changes its signature (e.g. its name or number of parameters) in which case the new version can be seen as replacing the old.

Applying this pattern is necessary when external implementations or external callers exist.
It only works if both methods are functionally equivalent and can coexist at the same time (which isn't always the case, for example if only the return type differs).
Otherwise it is a case of adding one and removing another function, possibly with different names, so they can coexist.

**New Version**:
-   define tests for the new method
-   add new method:
	-   include a default implementation which calls the old method
	-   `@apiNote` comment documents that the default implementation will eventually be removed
-   deprecate old method:
	-   include a default implementation which calls the new method (the circular calls are intended; if a default implementation existed, it can remain)
	-   `@apiNote` comment documents that the default implementation will eventually be removed
	-   `@Deprecation` comment documents that the new method is to be used
-   internal implementations override the new instead of the old method
-   internal callers use the new instead of the old method

**Transition**:
-   external implementations override the new instead of the old method
-   external callers use the new instead of the old method

**New Version**:
-   make the new method abstract:
	-   remove the default implementation
	-   remove the `@apiNote` comment
-   remove the old method

While the circular calls look funny they ensure that it does not matter which variant of the methods is implemented.
But since both variants have default implementations the compiler will not produce an error if neither is implemented.
Unfortunately this would produce an infinite loop, so make sure to point this out to clients.
If you provide them with tests for their implementations or they wrote their own, they will immediately recognize this though.

### Remove

When removing an abstract method, different patterns can be applied depending on whether external implementations exist or not.

#### External Implementations Exist

**New Version**:
-   tests for the method must accept `UnsupportedOperationException`s
-   deprecate the method:
	-   include a default implementation which throws an `UnsupportedOperationException` (this passes the updated tests)
	-   `@Deprecation` comment documents that the method will eventually be removed
	-   `@apiNote` comment documents that the default implementation only exists to phase out the method
-   internal callers stop using the method

**Transition**:
The following steps must happen in that order:
-   external callers stop using the method
-   external implementations of the method are removed

**New Version**:
-   remove the method

Note that internal and external implementations are only removed after no more calls to the method exist.
Hence no `UnsupportedOperationException` should ever occur.

#### External Implementations Do Not Exist

In this case a regular deprecation suffices.
This case is only listed for the sake of completeness.

**New Version**:
-   deprecate the method with `@Depreated`
-   internal callers stop using the method

**Transition**:
-   external callers stop calling the method

**New Version**:
-   remove the method

## Reflection

We have seen how interface evolution is possible by adding, replacing and removing methods: a new interface version combines old and new outline, the client moves from the former to the latter and a final version removes residues of the old outline.
Default implementatins of the involved methods ensure that the old as well as the new version of the client's code compile and behave properly.
