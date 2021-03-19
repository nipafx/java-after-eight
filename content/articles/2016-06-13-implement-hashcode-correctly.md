---
title: "How To Implement hashCode Correctly"
tags: [java-basics]
date: 2016-06-13
slug: implement-java-hashcode-correctly
canonicalUrl: https://www.sitepoint.com/how-to-implement-javas-hashcode-correctly/
description: "So you wrote a nice `equals` implementation? Great! But now you have to implement `hashCode` as well. Let’s see how to do it correctly."
intro: "Hand in hand with a class's definition of equality goes a matching implementation of `hashCode`. Again, there are a couple of things to be considered to get it right. Let’s check 'em out!"
searchKeywords: "hashCode"
featuredImage: implementing-hashcode-correctly
---

So you’ve decided that identity isn’t enough for you and [wrote a nice `equals` implementation](implement-java-equals-correctly)?
Great!
But now you *have to* implement `hashCode` as well.

Let’s see why and how to do it correctly.

## Equality and Hash Code

While equality makes sense from a general perspective, hash codes are much more technical.
If we were being a little hard on them, we could say that they are just an implementation detail to improve performance.

Most data structures use `equals` to check whether they contain an element.
For example:

```java
List<String> list = Arrays.asList("a", "b", "c");
boolean contains = list.contains("b");
```

The variable `contains` is `true` because, while instances of `"b"` are not identical (again, ignoring [String interning](http://javatechniques.com/blog/string-equality-and-interning/)), they are equal.

Comparing every element with the instance given to `contains` is wasteful, though, and a whole class of data structures uses a more performant approach.
Instead of comparing the requested instance with each element they contain, they use a shortcut that reduces the number of potentially equal instances and then only compare those.

This shortcut is the hash code, which can be seen as an object’s equality boiled down to an integer value.
Instances with the same hash code are not necessarily equal but equal instances have the same hash code.
(Or should have, we will discuss this shortly.) Such data structures are often named after this technique, recognizable by the `Hash` in their name, with `HashMap` the most notable representative.

This is how they generally work:

-   When an element is added, its hash code is used to compute the index in an internal array (called a bucket).
-   If other, non-equal elements have the same hash code, they end up in the same bucket and must be bundled together, e.g. by adding them to a list.
-   When an instance is given to `contains`, its hash code is used to compute the bucket.
Only elements therein are compared to the instance.

This way, very few, ideally no `equals` comparisons are required to implement `contains`.

As `equals`, `hashCode` is defined on `Object`.

## Thoughts on Hashing

If `hashCode` is used as a shortcut to determine equality, then there is really only one thing we should care about: Equal objects should have the same hash code.

This is also why, if we override `equals`, we must create a matching `hashCode` implementation!
Otherwise things that are equal according to our implementation would likely not have the same hash code because they use `Object`‘s implementation.

## The `hashCode` Contract

Quoting [the source](https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#hashCode--):

> The general contract of `hashCode` is:
>
> -   Whenever it is invoked on the same object more than once during an execution of a Java application, the `hashCode` method must consistently return the same integer, provided no information used in equals comparisons on the object is modified.
> This integer need not remain consistent from one execution of an application to another execution of the same application.
> -   If two objects are equal according to the `equals(Object)` method, then calling the `hashCode` method on each of the two objects must produce the same integer result.
> -   It is not required that if two objects are unequal according to the `equals(Object)` method, then calling the `hashCode` method on each of the two objects must produce distinct integer results.
> However, the programmer should be aware that producing distinct integer results for unequal objects may improve the performance of hash tables.

The first bullet mirrors the consistency property of `equals` and the second is the requirement we came up with above.
The third states an important detail that we will discuss in a moment.

## Implementing `hashCode`

A very easy implementation of `Person.hashCode` is the following:

```java
@Override
public int hashCode() {
	return Objects.hash(firstName, lastName);
}
```

The person’s hash code is computed by computing the hash codes for the relevant fields and combining them.
Both is left to `Objects`‘ utility function `hash`.

### Selecting Fields

But which fields are relevant?
The requirements help answer this: If equal objects must have the same hash code, then hash code computation should not include any field that is not used for equality checks.
(Otherwise two objects that only differ in those fields would be equal but have different hash codes.)

So the set of fields used for hashing should be a subset of the fields used for equality.
By default both will use the same fields but there are a couple of details to consider.

### Consistency

For one, there is the consistency requirement.
It should be interpreted rather strictly.
While it allows the hash code to change if some fields change (which is often unavoidable with mutable classes), hashing data structures are not prepared for this scenario.

As we have seen above the hash code is used to determine an element’s bucket.
But if the hash-relevant fields change, the hash is not recomputed and the internal array is not updated.

This means that a later query with an equal object or even with the very same instance fails!
The data structure computes the current hash code, different from the one used to store the instance, and goes looking in the wrong bucket.

Conclusion: Better not use mutable fields for hash code computation!

### Performance

Hash codes might end up being computed about as often as `equals` is called.
This can very well happen in performance critical parts of the code so it makes sense to think about performance.
And unlike `equals` there is a little more wiggle room to optimize it.

Unless sophisticated algorithms are used or many, many fields are involved, the arithmetic cost of combining their hash codes is as negligible as it is unavoidable.
But it should be considered whether all fields need to be included in the computation!
Particularly collections should be viewed with suspicion.
Lists and sets, for example, will compute the hash for each of their elements.
Whether calling them is necessary should be considered on a case-by-case basis.

If performance is critical, using `Objects.hash` might not be the best choice either because it requires the creation of an array for its [varargs](https://docs.oracle.com/javase/8/docs/technotes/guides/language/varargs.html).

But the general rule about optimization holds: Don’t do it prematurely!
Use a common hash code algorithm, maybe forego including the collections, and only optimize after profiling showed potential for improvement.

### Collisions

Going all-in on performance, what about this implementation?

```java
@Override
public int hashCode() {
	return 0;
}
```

It’s fast, that’s for sure.
And equal objects will have the same hash code so we’re good on that, too.
As a bonus, no mutable fields are involved!

But remember what we said about buckets?
This way all instances will end up in the same!
This will typically result in a linked list holding all the elements, which is terrible for performance.
Each `contains`, for example, triggers a linear scan of the list.

So what we want is as few items in the same bucket as possible!
An algorithm that returns wildly varying hash codes, even for very similar objects, is a good start.

How to get there partly depends on the selected fields.
The more details we include in the computation, the more likely it is for the hash codes to differ.
Note how this is completely opposite to our thoughts about performance.
So, interestingly enough, using too many *or* too few fields can result in bad performance.

The other part to preventing collisions is the algorithm that is used to actually compute the hash.

### Computing The Hash

The easiest way to compute a field’s hash code is to just call `hashCode` on it.
Combining them could be done manually.
A common algorithm is to start with some arbitrary number and to repeatedly multiply it with another (often a small prime) before adding a field’s hash:

```java
int prime = 31;
int result = 1;
result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
return result;
```

This might result in overflows, which is not particularly problematic because they cause no exceptions in Java.

Note that even great hashing algorithms might result in uncharacteristically frequent collisions if the input data has specific patterns.
As a simple example assume we would compute the hash of points by adding their x and y-coordinates.
May not sound too bad until we realize that we often deal with points on the line `f(x) = -x`, which means `x + y == 0` for all of them.
Collisions, galore!

But again: Use a common algorithm and don’t worry until profiling shows that something isn’t right.

## Summary

We have seen that computing hash codes is something like compressing equality to an integer value: Equal objects must have the same hash code and for performance reasons it is best if as few non-equal objects as possible share the same hash.

This means that `hashCode` must always be overridden if `equals` is.

When implementing `hashCode`:

-   Use a the same fields that are used in `equals` (or a subset thereof).
-   Better not include mutable fields.
-   Consider not calling `hashCode` on collections.
-   Use a common algorithm unless patterns in input data counteract them.

Remember that `hashCode` is about performance, so don’t waste too much energy unless profiling indicates necessity.
