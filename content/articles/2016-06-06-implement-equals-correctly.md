---
title: "How To Implement equals Correctly"
tags: [java-basics]
date: 2016-06-06
slug: implement-java-equals-correctly
canonicalUrl: https://www.sitepoint.com/implement-javas-equals-method-correctly/
description: "A fundamental aspect of any Java class is its definition of equality. It is determined by a class's `equals` method. Let's see how to implement it correctly."
intro: "A fundamental aspect of any Java class is its definition of equality. It is determined by a class's `equals` method and there are a couple of things to be considered for a correct implementation. Let’s check ’em out so we get it right!"
searchKeywords: "equals"
featuredImage: implementing-equals-correctly
---


A fundamental aspect of any Java class is its definition of equality.
It is determined by a class’s `equals` method and there are a couple of things to be considered for a correct implementation.
Let’s check ’em out so we get it right!

**Note that implementing `equals` always means that `hashCode` has to be implemented as well!
We’ll cover that in [a separate article](implement-java-hashcode-correctly) so make sure to read it after this one.**

## Identity Versus Equality

Have a look at this piece of code:

```java
String some = "some string";
String other = "other string";
```

We have two strings and they are obviously different.

What about these two?

```java
String some = "some string";
String other = some;
boolean identical = some == other;
```

Here we have only one String instance and `some` and `other` both reference it.
In Java we say `some` and `other` are *identical* and, accordingly, `identical` is `true`.

What about this one?

```java
String some = "some string";
String other = "some string";
boolean identical = some == other;
```

Now, `some` and `other` point to different instances and are no longer identical, so `identical` is false.
(We’ll ignore [String interning](http://javatechniques.com/blog/string-equality-and-interning/) in this article; if this bugs you, assume every string literal were wrapped in a `new String(...)`.)

But they *do* have *some* relationship as they both “have the same value”.
In Java terms, they are *equal*, which is checked with `equals`:

```java
String some = "some string";
String other = "some string";
boolean equal = some.equals(other);
```

Here, `equals` is `true`.

**A variable’s *Identity* (also called *Reference Equality*) is defined by the reference it holds.
If two variables hold the same reference they are *identical*.
This is checked with `==`.**

**A variable’s *Equality* is defined by the value it references.
If two variables reference the same value, they are *equal*.
This is checked with `equals`.**

But what does “the same value” mean?
It is, in fact, the implementation of `equals` that determines “sameness”.
The `equals` method is defined in `Object` and since all classes inherit from it, all have that method.

The implementation in `Object` checks identity (note that identical variables are equal as well), but many classes override it with something more suitable.
For strings, for example, it compares the character sequence and for dates it makes sure that both point to the same day.

Many data structures, most notably Java’s own collection framework, use `equals` to check whether they contain an element.

For example:

```java
List<String> list = Arrays.asList("a", "b", "c");
boolean contains = list.contains("b");
```

The variable `contains` is `true` because, while the instances of `"b"` are not identical, they are equal.

(This is also the point where `hashCode` comes into play.)

## Thoughts on Equality

Any implementation of `equals` must adhere to a specific contract or the class’s equality is ill-defined and all kinds of unexpected things happen.
We will look at the formal definition in a moment but let’s first discuss some properties of equality.

It might help to think about it as we encounter it in our daily lives.
Let’s say we compare laptops and consider them equal if they have the same hardware specifications.

1. One property is so trivial that it is hardly worth mentioning: Each thing is equal to itself.
Duh.
2. There is another, which is not much more inspiring: If one thing is equal to another, the other is also equal to the first.
Clearly if my laptop is equal to yours, yours is equal to mine.
3. This one is more interesting: If we have three things and the first and second are equal and the second and third are equal, then the first and third are also equal.
Again, this is obvious in our laptop example.

That was an exercise in futility, right?
Not so!
We just worked through some basic algebraic properties of equivalence relations.
No wait, don’t leave!
That’s already all we need.
Because any relation that has the three properties above can be called an equality.

Yes, *any* way we can make up that compares things and has the three properties above, could be how we determine whether those things are equal.
Conversely, if we leave anything out, we no longer have a meaningful equality.

## The `equals` Contract

The `equals` contract is little more but a formalization of what we saw above.
To quote [the source](https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-):

> The `equals` method implements an equivalence relation on non-null object references:
>
> -   It is *reflexive*: for any non-null reference value `x`, `x.equals(x)` should return `true`.
> -   It is *symmetric*: for any non-null reference values `x` and `y`, `x.equals(y)` should return `true` if and only if `y.equals(x)` returns true.
> -   It is *transitive*: for any non-null reference values `x`, `y`, and `z`, if `x.equals(y)` returns `true` and `y.equals(z)` returns `true`, then `x.equals(z)` should return `true`.
> -   It is *consistent*: for any non-null reference values `x` and `y`, multiple invocations of `x.equals(y)` consistently return `true` or consistently return `false`, provided no information used in `equals` comparisons on the objects is modified.
> -   For any non-null reference value `x`, `x.equals(null)` should return `false`.

By now, the first three should be very familiar.
The other points are more of a technicality: Without consistency data structures behave erratically and being equal to null not only makes no sense but would complicate many implementations.

## Implementing `equals`

For a class `Person` with string fields `firstName` and `lastName`, this would be a common variant to implement `equals`:

```java
@Override
public boolean equals(Object o) {
	// self check
	if (this == o)
		return true;
	// null check
	if (o == null)
		return false;
	// type check and cast
	if (getClass() != o.getClass())
		return false;
	Person person = (Person) o;
	// field comparison
	return Objects.equals(firstName, person.firstName)
			&& Objects.equals(lastName, person.lastName);
}
```

Let’s go through it one by one.

### Signature

It is very important that `equals` takes an `Object`!
Otherwise, unexpected behavior occurs.

For example, assume that we would implement `equals(Person)` like so:

```java
public boolean equals(Person person) {
	return Objects.equals(firstName, person.firstName)
			&& Objects.equals(lastName, person.lastName);
}
```

What happens in a simple example?

```java
Person elliot = new Person("Elliot", "Alderson");
Person mrRobot = new Person("Elliot", "Alderson");
boolean equal = elliot.equals(mrRobot);
```

Then `equal` is `true`.
What about now?

```java
Person elliot = new Person("Elliot", "Alderson");
Object mrRobot = new Person("Elliot", "Alderson");
boolean equal = elliot.equals(mrRobot);
```

Now it’s `false`.
[Wat](https://www.destroyallsoftware.com/talks/wat)?!
Maybe not quite what we expected.

The reason is that Java called `Person.equals(Object)` (as inherited from `Object`, which checks identity).
Why?

Java’s strategy for choosing which overloaded method to call is not based on the parameter’s runtime type but on its declared type.
(Which is a good thing because otherwise static code analysis, like call hierarchies, would not work.) So if `mrRobot` is declared as an `Object`, Java calls `Person.equals(Object)` instead of our `Person.equals(Person)`.

Note that most code, for example all collections, handle our persons as objects and thus always call `equals(Object)`.
So we better make sure we provide an implementation with that signature!
We can of course create a specialized `equals` implementation and call it from our more general one if we like that better.

### Self Check

Equality is a fundamental property of any class and it might end up being called very often, for example in [tight loops](https://en.wiktionary.org/wiki/tight_loop) querying a collection.
Thus, its performance matters!
And the self check at the beginning of our implementation is just that: a performance optimization.

```java
if (this == o)
	return true;
```

It might look like it should implement reflexivity but the checks further down would be very strange if they would not also do that.

### Null Check

No instance should be equal to null, so here we go making sure of that.
At the same time, it guards the code from `NullPointerException`s.

```java
if (o == null)
	return false;
```

It can actually be included in the following check, like so:

```java
if (o == null || getClass() != o.getClass())
	return false;
```

### Type Check and Cast

Next thing, we have to make sure that the instance we’re looking at is actually a person.
This is another tricky detail.

```java
if (getClass() != o.getClass())
	return false;
Person person = (Person) o;
```

Our implementation uses `getClass`, which returns the classes to which `this` and `o` belong.
It requires them to be identical!
This means that if we had a class `Employee extends Person`, then `Person.equals(Employee)` would never return `true` – not even if both had the same names.

This might be unexpected.

That an extending class with new fields does not compare well may be reasonable, but if that extension only adds behavior (maybe logging or other non-functional details), it should be able to equal instances of its supertype.
This becomes especially relevant if a framework spins new subtypes at runtime (e.g. Hibernate or Spring), which could then never be equal to instances we created.

An alternative is the `instanceof` operator:

```java
if (!(o instanceof Person))
	return false;
```

Instances of subtypes of `Person` pass that check.
Hence they continue to the field comparison (see below) and may turn out to be equal.
This solves the problems we mentioned above but opens a new can of worms.

Say `Employee extends Person` and adds an additional field.
If it overrides the `equals` implementation it inherits from `Person` and includes the extra field, then `person.equals(employee)` can be `true` (because of `instanceof`) but `employee.equals(person)` can’t (because `person` misses that field).
This clearly violates the symmetry requirement.

There seems to be a way out of this: `Employee.equals` could check whether it compares to an instance with that field and use it only then (this is occasionally called *slice comparison*).

But this doesn’t work either because it breaks transitivity:

```java
Person foo = new Person("Mr", "Foo");
Employee fu = new Employee("Mr", "Foo", "Marketing");
Employee fuu = new Employee("Mr", "Foo", "Engineering");
```

Obviously all three instances share the same name, so `foo.equals(fu)` and `foo.equals(fuu)` are `true`.
By transitivity `fu.equals(fuu)` should also be `true` but it isn’t if the third field, apparently the department, is included in the comparison.

There is really no way to make slice comparison work without violating reflexivity or, and this is trickier to analyze, transitivity.
(If you think you found one, check again.
Then let your coworkers check.
If you are still sure, [ping me](https://twitter.com/nipafx).
;) )

So we end with two alternatives:

-   Use `getClass` and be aware that instances of the type and its subtypes can never equal.
-   Use `instanceof` but make `equals` final because there is no way to override it correctly.

Which one makes more sense really depends on the situation.
Personally, I prefer `instanceof` because its problems (can not include new fields in inherited classes) occurs at declaration site not at use site.

### Field Comparison

Wow, that was a lot of work!
And all we did was solve some corner cases!
So let’s finally get to the test’s core: comparing fields.

This is pretty simple, though.
In the vast majority of cases, all there is to do is to pick the fields that should define a class’s equality and then compare them.
Use `==` for primitives and `equals` for objects.

If any of the fields could be null, the extra checks considerably reduce the code’s readability:

```java
return (firstName == person.firstName
		|| firstName != null && firstName.equals(person.firstName))
	&& (lastName == person.lastName
			|| lastName != null && lastName.equals(person.lastName))
```

And this already uses the non-obvious fact that `null == null` is `true`.

It is much better to use Java’s utility method `Objects.equals` (or, if you’re not yet on Java 7, Guava’s `Objects.equal`):

```java
return Objects.equals(firstName, person.firstName)
		&& Objects.equals(lastName, person.lastName);
```

It does exactly the same checks but is much more readable.

## Summary

We have discussed the difference between identity (must be the same reference; checked with `==`) and equality (can be different references to “the same value”; checked with `equals`) and went on to take a close look at how to implement `equals`.

Let’s put those pieces back together:

-   Make sure to override `equals(Object)` so our method is always called.
-   Include a self and null check for an [early return](java-multiple-return-statements#guard-clauses) in simple edge cases.
-   Use `getClass` to allow subtypes their own implementation (but no comparison across subtypes) or use `instanceof` and make `equals` final (and subtypes can equal).
-   Compare the desired fields using `Objects.equals`.

Or let your IDE generate it all for you and edit where needed.

## Final Words

We have seen how to properly implement `equals` (and will soon [look at `hashCode`](implement-java-hashcode-correctly)).
But what if we are using classes that we have no control over?
What if their implementations of these methods do not suit our needs or are plain wrong?

[LibFX](http://libfx.codefx.org) to the rescue!
It contains [transforming collections](java-transforming-collections) and one of their features is to allow the user to specify the `equals` and `hashCode` methods she needs.
