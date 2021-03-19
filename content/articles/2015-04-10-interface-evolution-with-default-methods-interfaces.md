---
title: "Interface Evolution With Default Methods – Part II: Interfaces"
tags: [default-methods, generics, java-8]
date: 2015-04-10
slug: java-default-methods-interface-evolution-failure
description: "Why interface evolution with default methods does not work for whole interfaces - at least not smooth enough to be practical."
searchKeywords: "interface evolution"
featuredImage: interface-evolution-with-default-methods-II
repo: java-x-demo
---

[Default methods](java-default-methods-guide) were introduced to enable interface evolution.
If backwards compatibility is sacrosanct, this is limited to adding new methods to interfaces (which is their exclusive use in the JDK).
But if clients are expected to update their code, default methods can be used to gradually evolve interfaces without causing compile errors, thus giving clients time to update their code to a new version of the interface.

[The first part of this mini-series](java-default-methods-interface-evolution) explained how default implementations allow to add, replace and remove methods without breaking client code.
I foolishly announced that "a future post will look into ways to replace whole interfaces" - also without breaking client code.

Well, you're reading this post now and the unfortunate summary is:
**I couldn't make it work.**

Why?
Generics.

Why exactly?
You really want to know?
Well, read on then, but the rest of the post is really only a description of how I ended up at a roadblock so don't expect too much of it.
(Great incentive, eh?)

## The Problem Statement

This is what we want to do:

Assume, your code base contains an interface which your clients use in all imaginable ways: they have their own implementations, call your code with instances of it and your code returns such instances and of course they use it as types for arguments and return values.

Now you want to substantially change the interface: rename it, move it or revamp it in a way that can not be expressed with changes to individual methods.
(But both interfaces are still equivalent in the sense that adapters can be provided to get from one version to the other.)

You could just do it, release a new version with the changes and tell your clients to fix their resulting compile errors.
If their code is highly coupled to yours, they might have to do this in a separate branch to spend some time on it but that's life, right?
You're a really nice guy/gal, though, so instead of requiring a flag day you would like to give them the opportunity to change their code gradually over time (e.g. until the next release) without any compile errors.

(Note that this is the principal requirement for all that follows.
I'm largely ignoring whether that's a good idea in the first place.
I just wanted to look how far I can get.)

The only way I see to even have a chance of achieving this is to define a transitional phase where both the old and the new version of the interface coexist.
So what we really need is a general step-by-step approach of how to move implementations, callers and declarations from one interface to another.

## The Idea

When announcing this post, I had a specific idea of how this was going to work.
It was essentially the same approach I used for methods.

### Evolving Interface Methods

Using default methods to add, replace or remove single methods of an interface is pretty straight forward and usually consists of three steps (in some cases less):

New Version
:   A new version of the library is released where the interface definition is transitional and combines the old as well as the new, desired outline.
Default methods ensure that all external implementations and calls are still valid and no compile errors arise on an update.

Transition
:   Then the client has time to move from the old to the new outline.
Again, the default methods ensure that adapted external implementations and calls are valid and the changes are possible without compile errors.

New Version
:   In a new version, the library removes residues of the old outline.
Given the client used her time wisely and made the necessary changes, releasing the new version will not cause compile errors.

If you are interested in a more detailed description of these steps, you can read [my earlier post](java-default-methods-interface-evolution).

### Evolving the Interface

This approach seemed to make a lot of sense for this case, too, so I sat down to play it out.

It is a little more complicated if the whole interface changes because where methods only have callers and implementations, the interface is also a type, i.e.
it can be used in declarations.
This makes it necessary to distinguish three ways to use the interface:

-   **internal use** where you own the implementation and the code using the interface
-   **published use** where you own the implementation but the client makes calls to the code
-   **external use** where the client owns the implementation and the code using the interface

The part that works, follows the same approach as evolving methods:

New Version
:   Release a new version with the new interface, which extends the old one.
Let all internal code implement and use the new interface.
All published code will use the old interface to declare argument types and the new interface for return types.
If instances have to be converted, this can be done with an adapter.
Ignoring parameterized types for now, this change will not cause compile errors in client code.

Transition
:   After the release the clients change their code.
Starting with the implementations of the old interface (which are changed to implement the new one) and the instances returned by your published code, they can start declaring instances of the new type, update the argument types of methods they are passing them to and so on.
If necessary, the adapter can be used temporarily to interact with old instances through the new interface.

New Version
:   Release a version which removes the old interface.

In the same way as with evolving methods, default implementations in the new interface allow client code to stop implementing the old interface explicitly which lets you remove it in the second release.
Additionally a handy `asNew()` method on the old interface can invoke the adapter to return itself adapted to the new interface.

I glossed over some of the details but I hope you believe me that this works.
Now let's come back to generics...

## The Roadblock

The crucial piece in the presented approach is the published code.
It is called by your clients, so the first release must change it in a compatible manner.
And as all internal code requires the new interface it must make the step from `Old` to `New`.

Without generics it might look like this:

```java
// in version 0
public Old doSomething(Old o) {
	// 'callToInternalCode' requires an 'Old'
	callToInternalCode(o);
	return o;
}

// in version 1 the method still accepts 'Old' but returns 'New'
public New doSomething(Old o) {
	// 'callToInternalCode' now requires a 'New'
	New n = o.asNew();
	callToInternalCode(n);
	return n;
}
```

Ok, so far so good.
Now let's see how that might look with generics.

```java
// in version 0
public Container<Old> doSomething(Container<Old> o) {
	// 'callToInternalCode' requires a 'Container<Old>'
	callToInternalCode(o);
	return o;
}

// in version 1
// doesn't work because it breaks assignments of the return value
public Container<New> doSomething(Container<Old> o) {
	// 'callToInternalCode' requires a 'Container<New>'
	// but we can not hand an adapted version to 'callToInternalCode'
	// instead we must create a new container
	New nInstance = o.get().asNew();
	Container<New> n = Container.of(nInstance);
	callToInternalCode(n);
	return n;
}
```

So using the published layer of code to adapt from the old to the new interface does not generally work for (at least) two reasons:

-   Due to the invariance of generics in Java, all assignments of the return value will break:

	```java
	Container<Old> old = // ...
	// works in version 0; breaks in version 1
	Container<Old> o = published.doSomething(old);
	```

-   The same `Container` instance can not be passed from the published to the internal code.
This leads to two problems:
	-   Creating a new container might be hard or impossible.
	-   Changes the internal code makes to the new container are not propagated to the container passed by the external code.

Damn...

From the outset on I felt that generics would be trouble - in retrospect that's actually pretty obvious.
When types are involved how can generics *not* be a problem.
So, maybe I should've tried to solve the hard problem first.

## Possible Detours

After banging my head against the wall for a time, I still don't see a general way to solve this.
But I came up with some ideas which might help solve special cases.

### Wildcards

You could check whether the published and internal code makes maximum use of wildcards (remember [PECS](http://stackoverflow.com/q/2723397/2525313 "What is PECS?
- StackOverflow")).
You could also advice your clients on how to use them.

Depending on the situation this might produce a solution.

### Specialized Interfaces, Classes, Instances

Depending on the concrete code, it could be possible to provide a new version of the published interfaces, classes or instances which use the old interface.
If the code can be massaged in a way which lets the client choose whether to use the interface, class or instance which depends on the old interface or the one which depends on the new interface, the individual implementations do not have to make the transition.

But this may push the old interface back down into the internal code, which was just updated to only use the new one.
That doesn't sound good either.

### Adapters For Containers

You could provide adapters for containers which are used with the old interface in published code.
This will essentially allow you to call `asNew()` on those containers.

(For an unrelated reason I'm currently working on such transformations for some of the JDK collections.
The next version of [LibFX](http://libfx.codefx.org) will contain them; if you're curious, you can already check out a demo over at [GitHub](https://github.com/nipafx/LibFX/blob/2718dd2d0c9df8745e901cee9157be87a4d5f2da/src/demo/java/org/codefx/libfx/collection/transform/TransformingSetDemo.java).)

### Screw It!

All this and for what?
To keep the client from creating a branch, spend some time fixing things there before merging everything back into master?
Screw it!

At this point, this is my opinion on the matter.
While interface evolution is smooth as long as you only deal with individual methods, it seems to become a pain when you want to replace whole interfaces.
So unless there are pretty good reasons to introduce all this complexity, I'd just do it the hard way and let the client sort it out.
Or not do it at all.

And if you're just renaming or moving an interface, most or even all of the work can be done by a simple search-replace anyways.

## Reflection

We reiterated how default methods can be used for interface evolution with a three part sequence of Release, Transition, Release.
While this works for single methods, we saw that it fails for replacing whole interfaces.
The principal problem is that invariance of parametric types prevents us from using the published code as an adapting layer.

Even though we saw some approaches how that problem might be tackled no good solution stood out.
In the end it doesn't look like it is worth the trouble.

Did I overlook something?
Or is the whole idea just stupid?
Why not leave a comment!
