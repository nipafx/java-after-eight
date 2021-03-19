---
title: "Roll Your Own Pirate-Elvis Operator"
tags: [java-8, lambda, optional]
date: 2015-03-16
slug: java-pirate-elvis-operator
description: "Java has no Elvis operator (or null coalescing operator / null-safe member selection) but with lambda expressions / method references you can roll your own."
searchKeywords: "elvis operator"
featuredImage: elvis-operator
repo: elvis-operator
---

So, Java doesn't have an Elvis operator (or, as it is more formally known, null coalescing operator or null-safe member selection) ... While I personally don't much care about it, some people seem to really like it.
And when a colleague needed one a couple of days back I sat down and explored our options.

And what do you know!
You can get pretty close with method references.

We will first have a look at what the Elvis operator is and why pirates are involved.
I will then show how to implement it with a utility method.

## Elvis?

Isn't he dead?

I thought so, too, but [apparently not](http://www.elvis-is-alive.com/).
And much like rumors about The King being alive, people wishing for the Elvis operator also never quite die out.
So let's see what they want.

(If you want to read one discussion about it for yourself, see [this thread on the OpenJDK mailing list](http://mail.openjdk.java.net/pipermail/coin-dev/2009-March/000047.html), where Stephen Colebourne proposed these operators for Java 7.)

### The Elvis Operator

In its simplest form Elvis is a binary operator which selects the non-null operand, preferring the left one.
So instead of ...

```java
private String getStreetName() {
	return streetName == null ? "Unknown Street" : streetName;
//  or like this?
//  return streetName != null ? streetName : "Unknown Street";
}
```

... you can write ...

```java
private String getStreetName() {
	return streetName ?: "Unknown Street";
}
```

I'd be ok to get this one in Java.
It's a nice shortcut for a frequently used pattern and keeps me from wasting time on the decision which way to order the operands for the ternary "?
:" (because I always wonder whether I want to put the regular case first or want to avoid the double negative).

Emulating this with a static utility function is of course trivial but, I'd say, also borderline pointless.
The effort of statically importing that method and having all readers of the code look up what it means outweighs the little benefit it provides.

So I'm not talking about this Elvis.
Btw, it's called that because ?: looks like a smiley with a pompadour.
And who could that be if not Elvis... And yes, this is how we in the industry pick names all the time!
More formally it is also known as the [null coalescing operator](https://en.wikipedia.org/wiki/Null_coalescing_operator).

### The Pirate-Elvis Operator

Then there is this other thing which doesn't seem to have it's own name and this is what I want to talk about.
It's sometimes also called Elvis, but other times it gets handy names like "null-safe member selection operator".
At least, that explains pretty well what it does: It short circuits a member selection if the instance on which the member is called is null so that the whole call returns null.

This comes in handy when you want to chain method calls but some of them might return null.
Of course you'd have to check for this or you'll run into a NullPointerExeption.
This can lead to fairly ugly code.
Instead of ...

```java
private String getStreetName(Order order) {
	return order.getCustomer().getAddress().getStreetName();
}
```

... you'd have to write ...

```java
private String getStreetName(Order order) {
	Customer customer = order == null ? null : order.getCustomer();
	Address address = customer == null ? null : customer.getAddress();
	return address == null ? null : address.getStreetName();
}
```

That is clearly terrible.
But with the "null-safe member selection operator":

```java
private String getStreetName(Order order) {
	return order?.getCustomer()?.getAddress()?.getStreetName();
}
```

Looks better, right?
Yes.
And it let's you forget about all those pesky nulls, mh?
Yes.
So that's why [I think it's a bad idea](why-elvis-should-not-visit-java).

Fields being frequently null reeks of bad design.
And with Java 8, you can instead [avoid null by using Optional](intention-revealing-code-java-8-optional).
So there should really be little reason to make throwing nulls around even easier.
That said, sometimes you still want to, so let's see how to get close.

By the way, since there seems to be no official term for this variant yet, I name ?.
the Pirate-Elvis operator (note the missing eye).
Remember, you read it here first!
;)

## Implementing The Pirate-Elvis Operator

So now that we know what we're talking about, let's go implement it.
We can use Optional for this or write some dedicated methods.

### With Optional

Just wrap the first instance in an Optional and apply the chained functions as maps:

```java
private String getStreetName(Order order) {
	return Optional.ofNullable(order)
			.map(Order::getCustomer)
			.map(Customer::getAddress)
			.map(Address::getStreetName)
			.orElse(null);
}
```

This requires a lot of boilerplate but already contains the critical aspects: Specify the methods to call with method references and if something is null (which in this case leads to an empty Optional), don't call those methods.

I still like this solution because it clearly documents the optionality of those calls.
It is also easy (and actually makes the code shorter) to do the right thing and return the street name as an `Optional<String>` .

### With Dedicated Utility Methods

Starting from the solution with Optional, finding a shorter way for this special case is pretty straight forward: Just hand the instance and the method references over to a dedicated method and let it sort out when the first value is null.

```java
public static <T1, T2> T2 applyNullCoalescing(T1 target,
		Function<T1, T2> f) {
	return target == null ? null : f.apply(target);
}

public static <T1, T2, T3> T3 applyNullCoalescing(T1 target,
		Function<T1, T2> f1, Function<T2, T3> f2) {
	return applyNullCoalescing(applyNullCoalescing(target, f1), f2);
}

public static <T1, T2, T3, T4> T4 applyNullCoalescing(T1 target,
		Function<T1, T2> f1, Function<T2, T3> f2,
		Function<T3, T4> f3) {
	return applyNullCoalescing(applyNullCoalescing(target, f1, f2), f3);
}

public static <T1, T2, T3, T4, T5> T5 applyNullCoalescing(T1 target,
		Function<T1, T2> f1, Function<T2, T3> f2,
		Function<T3, T4> f3, Function<T4, T5> f4) {
	return applyNullCoalescing(applyNullCoalescing(target, f1, f2, f3), f4);
}
```

(This implementation is optimized for succinctness.
If each method were implemented explicitly, the performance could be improved.)

Using method references these methods can be called in a very readable fashion:

```java
private String getStreetName(Order order) {
	return applyNullCoalescing(order,
			Order::getCustomer, Customer::getAddress, Address::getStreetName);
}
```

Still no `order?.getCustomer()?.getAddress()?.getStreetName();` but close.

## Reflection

We have seen what the null coalescing operator (?:) and the null-safe member selection operator (?.) are.
Even though the latter might encourage bad habits (passing nulls around) we have then gone and implemented it with a utility method which can be called with method references.

Any code you like is free to use.
