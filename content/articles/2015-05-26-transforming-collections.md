---
title: "Transforming Collections"
tags: [collections, libfx]
date: 2015-05-26
slug: java-transforming-collections
description: "Transforming collections are a view onto another collection, making it appear to be of a different parametric type. They are available in LibFX 0.3.0."
intro: "Transforming collections are a view onto another collection, making it appear to be of a different parametric type. This can be used to remove optionality from a collection or substitute `equals` and `hashCode`."
searchKeywords: "Transforming Collections"
featuredImage: transforming-collections
repo: libfx
---

Did you ever want to substitute the `equals` and `hashCode` methods a `HashSet` or `HashMap` uses?
Or have a `List` of some element type masquerade as a `List` of a related type?
Transforming collections make that possible and this post will show how.

Transforming collections are a feature of **[LibFX](http://libfx.codefx.org) 0.3.0**, which will be released any day now.
This post will present the general idea, cover technical details and finish with some use cases where they might come in handy.

<admonition type="update">

In the meantime, [version 0.3.0 was released](libfx-0-3-0).
While this post might get out of date at some point, [the wiki page for transforming collections](https://github.com/nipafx/LibFX/wiki/TransformingCollections) will be updated.
If you're a visitor from the future, make sure to check it out.
The ongoing example is a slightly adapted variant of [the feature demo contained in **LibFX**](https://github.com/nipafx/LibFX/blob/master/src/demo/java/org/codefx/libfx/collection/transform/TransformingSetDemo.java).
Keep in mind that it is only an example to demonstrate the concept.

</admonition>

## Transforming Collections

A transforming collection is a view onto another collection (e.g. list onto list, map onto map, ...), which appears to contain elements of a different type (e.g. integers instead of strings).

The view elements are created from the inner elements by applying a transformation.
This happens on demand so the transforming collection itself is stateless.
Being a proper view, all changes to the inner collection as well as to the transforming view are reflected in the other (like, e.g., [Map and its entrySet](https://docs.oracle.com/javase/8/docs/api/java/util/Map.html#entrySet--)).

### Nomenclature

A transforming collection can also be seen as a decorator.
I will refer to the decorated collection as the *inner collection* and it's generic type accordingly as the *inner type*.
The transforming collection and its generic type are referred to as *outer collection* and *outer type*, respectively.

### Example

Let's see an example.
Say we have a set of strings but we know that those strings only ever contain natural numbers.
We can use a transforming set to get a view which appears to be a set of integers.

(Comments like `// "[0, 1] ~ [0, 1]"` are the console output of `System.out.println(innerSet + " ~ " + transformingSet);`.)

```java
Set<String> innerSet = new HashSet<>();
Set<Integer> transformingSet = TransformingCollectionBuilder
	/* skipping some details */
	.transformSet(innerSet);
// both sets are initially empty: "[] ~ []"

// now let's add some elements to the inner set
innerSet.add("0");
innerSet.add("1");
innerSet.add("2");
// these elements can be found in the view: "[0, 1, 2] ~ [0, 1, 2]"

// modifying the view reflects on the inner set
transformingSet.remove(1);
// again, the mutation is visible in both sets: "[0, 2] ~ [0, 2]"
```

See how pleasant transformations can be?

## Details

As usual, the devil is in the details so let's discuss the important parts of this abstraction.

### Forwarding

Transforming collections are a view onto another collection.
This means that they do not hold any elements by themselves but forward all calls to the inner/decorated collection.

They do this by transforming call arguments from the outer to the inner type and calling the inner collection with these arguments.
Return values are then transformed from the inner to the outer type.
This gets a little more complicated for calls which take collections as arguments but the approach is essentially the same.

All transforming collections are implemented in a way that forwards each call of a method to *the same method* on the inner collection (including [default methods](java-default-methods-guide)).
This implies that any guarantees the inner collection makes regarding thread-safety, atomicity, ... are also upheld by the transforming collection.

### Transformation

The transformation is computed with a pair of functions, which is specified during construction.
One is used to transform outer elements to inner elements and another one for the other direction.
(For maps two such pairs exist: one for keys and one for values.)

The transforming functions must be inverse to each other with regard to [`equals`](http://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-) , i.e.
`outer.equals(toOuter(toInner(outer))` and `inner.equals(toInner(toOuter(inner))` must be true for all outer and inner elements.
If this is not the case, the collections might behave in an unpredictable manner.

The same is not true for identity, i.e.
` outer == toOuter(toInner(outer))` may be false.
The details depend on the applied transformation and are generally unspecified - it might never, sometimes or always be true.

#### Example

Let's see how the transforming functions look for our sets of string and integers:

```java
private Integer stringToInteger(String string) {
	return Integer.parseInt(string);
}

private String integerToString(Integer integer) {
	return integer.toString();
}
```

And this is how we use them to create the transforming set:

```java
Set<Integer> transformingSet = TransformingCollectionBuilder
	/* still skipping some details */
	.toOuter(Integer::parseInt)
	.toInner(Object::toString)
	.transformSet(innerSet);
```

Straight forward, right?

Yes, but even this simple example contains pitfalls.
Note how strings with leading zeros are mapped to the same integer.
This can be used to create undesired behavior:

```java
innerSet.add("010");
innerSet.add("10");
// now the transforming sets contains the same entry twice:
// "[010, 10] ~ [10, 10]"

// sizes of different sets:
System.out.println(innerSet.size()); // "2"
System.out.println(transformingSet.size()); // "2"
System.out.println(new HashSet<>(transformingSet).size()); // "1" !

// removing is also problematic
transformingSet.remove(10) // the call returns true
// one of the elements could be removed: "[010] ~ [10]"
transformingSet.remove(10) // the call returns false
// indeed, nothing changed: "[010] ~ [10]"

// now things are crazy - this returns false:
transformingSet.contains(transformingSet.iterator().next())
// the transforming set does not contain its own elements ~> WAT?
```

So when using transforming collections, it is very important to think carefully about the transformations.
They must be inverse to each other!

But it suffices if this is limited to the actually occurring inner and outer elements.
In the example the problems only begin when strings with leading zeros are introduced.
If these were forbidden by some business rule, which is properly enforced, everything will be fine.

### Type Safety

All operations on transforming collections are type safe in the usual static, compile-time way.
But since many methods from the collection interfaces allow objects (e.g. `Collection.contains(Object)`) or collections of unknown generic type (e.g. `Collection.addAll(Collection<?>)`) as arguments, this does not cover all cases which can occur at runtime.

Note that the arguments of those calls must be transformed from the outer to the inner type in order to forward the call to the inner collection.
If they are called with an instance which is not of the outer type, it is likely that it can not be passed to the transforming function.
In this case the method may throw a `ClassCastException`.
While this is in accordance with the methods' contracts it might still be unexpected.

To reduce this risk, constructors of transforming collections require tokens of the inner and outer type.
They are used to check whether an element is of the required type and if it is not, the query can be answered gracefully without an exception.

#### Example

We can finally see exactly how to create the transforming set:

```java
Set<Integer> transformingSet = TransformingCollectionBuilder
	.forInnerAndOuterType(String.class, Integer.class)
	.toOuter(Integer::parseInt)
	.toInner(Object::toString)
	.transformSet(innerSet);
```

The builder actually accepts `Class<?
super I>` so this would compile as well:

```java
Set<Integer> transformingSetWithoutTokens = TransformingCollectionBuilder
	.forInnerAndOuterType(Object.class, Object.class)
	.toOuter(Integer::parseInt)
	.toInner(Object::toString)
	.transformSet(innerSet);
```

(For unknown types it would be easier to call `forInnerAndOuterTypeUnknown`, which is equivalent to passing object tokens.)

But since everything is an object, the type check against the token becomes useless and calling the transforming function can cause an exception:

```java
Object o = new Object();
innerSet.contains(o); // false
transformingSet.contains(o); // false
transformingSetWithoutTokens.contains(o); // exception
```

This should hence only be used if no tokens are available.

## Use Cases

I'd say transforming collections are a very specialized tool, which is unlikely to be used frequently but still has a place in every well sorted toolbox.

It is important to note that if performance is critical, they can be problematic.
Every call to a transforming collection which takes or returns an element causes at least one, often more objects to be created.
These put pressure on the garbage collector and cause an additional level of indirection on the way to the payload.
(As always when performance is discussed: profile first!)

So what are the use cases for transforming collections?
We have already seen above how a collection's element type can be changed to another.
While this presents the general idea I do not think it is a very common use case (although a valid approach in certain edge cases).

Here I will show two more narrow solutions, which you might want to use at some point.
But I also hope this gives you an idea of how transforming collections can be used to solve tricky situations.
Maybe the solution to your problem lies in applying this concept in a clever way.

### Substituting Equals And HashCode

I always liked how .NET's hash map (they call it a dictionary) has [a constructor which takes an EqualityComparer as an argument](https://msdn.microsoft.com/en-us/library/ms132072%28v=vs.110%29.aspx).
All calls to `equals` and `hashCode`, which would usually be called on the keys, are delegated to this instance instead.
It is thus possible to replace problematic implementations on the fly.

This can be a life saver when dealing with problematic legacy or library code which you do not have full control over.
It is also useful when some special comparison mechanism is required.

With transforming collections, this is easy.
To make it even easier, **LibFX** already contains an `EqualityTransformingSet` and `EqualityTransformingMap`.
They decorate another set or map implementation and `equals`/`hashCode` functions for the keys/elements can be provided during construction.

#### Example

Let's say you want to use strings as set elements but for comparison you are only interested in their length.

```java
Set<String> lengthSet = EqualityTransformingCollectionBuilder
	.forType(String.class)
	.withEquals((a, b) -> a.length() == b.length())
	.withHash(String::length)
	.buildSet();

lengthSet.add("a");
lengthSet.add("b");
System.out.println(lengthSet); // "[a]"
```

### Removing Optionality From A Collection

Maybe you're working with someone who took the idea of [using `Optional` everywhere](intention-revealing-code-java-8-optional), ran wild with it and now you're having a `Set<Optional<String>>`.
In case modifying the code (or your colleague) is no option, you can use transforming collections to get a view which hides `Optional` from you.

Again, implementing this was straight forward so **LibFX** already contains this in the form of `OptionalTransforming[Collection|List|Set]`.

#### Example

```java
Set<Optional<String>> innerSet = new HashSet<>();
Set<String> transformingSet =
	new OptionalTransformingSet<String>(innerSet, String.class);

innerSet.add(Optional.empty());
innerSet.add(Optional.of("A"));

// "[Optional.empty, Optional[A]] ~ [null, A]"
```

Note how the empty optional is represented by `null`.
This is the default behavior but you can also specify another string as a value for empty optionals:

```java
Set<String> transformingSet =
	new OptionalTransformingSet<String>(innerSet, String.class, "DEFAULT");

// ... code as above ...
// "[Optional.empty, Optional[A]] ~ [DEFAULT, A]"
```

This avoids Optional as well as null as an element but now you have to be sure that there is never an Optional which contains *DEFAULT*.
(If it does, the implicit transformations are not inverse to each other, which we have already seen above to cause problems.)

For more details on this example, check out [the demo](https://github.com/nipafx/LibFX/blob/master/src/demo/java/org/codefx/libfx/collection/transform/OptionalTransformingSetDemo.java).

## Reflection

We have covered that transforming collections are a view onto another collection.
Using type tokens (to minimize `ClassCastExceptions`) and a pair of transforming functions (which must be inverse to each other) every call will be forwarded to the decorated collection.
The transforming collection can uphold all guarantees regarding thread-safety, atomicity, ... made by the decorated collection.

We have then seen two specific use cases of transforming collections: substituting equals and hash code used by hashing data structures and removing optionality from a `Collection<Optional<E>>`.

### A Word On LibFX

As I said, transforming collections are a part of my open source project **LibFX**.
If you consider using it, I'd like to point out a few things:

-   This post presents the idea and *some* details but does not replace the documentation.
Check out [the wiki](https://github.com/nipafx/LibFX/wiki) for an up-to-date description and pointers to the javadoc (currently [here](http://libfx.codefx.org/apidocs/org/codefx/libfx/collection/transform/package-summary.html)).
-   I take testing seriously.
[Thanks to Guava](test-collection-implementations-guava), transforming collections are covered by about 6.500 unit tests.
-   **LibFX** is licensed under GPL.
If that does not suit your licensing model, feel free to contact me.

