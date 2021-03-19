---
title: "Java 9 Additions To `Optional`"
tags: [java-9, optional]
date: 2016-06-26
slug: java-9-optional
description: "Java 9 is coming! One of the changes are new methods on Optional: `stream()`, `or()`, and `ifPresentOrElse()`, which considerably improve Optional's API."
intro: "More about Java 9 - this time: `Optional`. We'll see how the new methods `stream()`, `or()`, and `ifPresentOrElse()` considerably improve its API."
searchKeywords: "java 9 optional"
featuredImage: java-9-optional
repo: java-x-demo
---

Wow, people were *really* interested in [Java 9's additions to the Stream API](java-9-stream).
Want some more?
Let's look at ...

## Optional

### `Optional::stream`

This one requires no explanation:

```java
Stream<T> stream();
```

The first word that comes to mind is: *finally*!
Finally can we easily get from a stream of optionals to a stream of present values!

<pullquote>Finally we get from Optional to Stream!</pullquote>

Given a method `Optional<Customer> findCustomer(String customerId)` we had to do something like this:

```java
public Stream<Customer> findCustomers(Collection<String> customerIds) {
	return customerIds.stream()
		.map(this::findCustomer)
		// now we have a Stream<Optional<Customer>>
		.filter(Optional::isPresent)
		.map(Optional::get);
}
```

Or this:

```java
public Stream<Customer> findCustomers(Collection<String> customerIds) {
	return customerIds.stream()
		.map(this::findCustomer)
		.flatMap(customer -> customer.isPresent()
			? Stream.of(customer.get())
			: Stream.empty());
}
```

We could of course push that into a utility method (which I hope you did) but it was still not optimal.

Now, it would've been interesting to have `Optional` actually implement `Stream` but

1. it doesn't look like it has been considered [when `Optional` was designed](design-java-optional), and
2. that ship has sailed since streams are lazy and `Optional` is not.

So the only option left was to add a method that returns a stream of either zero or one element(s).
With that we again have two options to achieve the desired outcome:

```java
public Stream<Customer> findCustomers(Collection<String> customerIds) {
	return customerIds.stream()
		.map(this::findCustomer)
		.flatMap(Optional::stream)
}

public Stream<Customer> findCustomers(Collection<String> customerIds) {
	return customerIds.stream()
		.flatMap(id -> findCustomer(id).stream());
}
```

It's hard to say which I like better - both have upsides and downsides - but that's a discussion for another post.
Both look better than what we had to do before.

Another small detail: If we want to, we can now more easily move from eager operations on `Optional` to lazy operations on `Stream`.

<pullquote>We can now operate lazily on Optional.</pullquote>

```java
public List<Order> findOrdersForCustomer(String customerId) {
	return findCustomer(customerId)
		// 'List<Order> getOrders(Customer)' is expensive;
		// this is 'Optional::map', which is eager
		.map(this::getOrders)
		.orElse(new ArrayList<>());
}

public Stream<Order> findOrdersForCustomer(String customerId) {
	return findCustomer(customerId)
		.stream()
		// this is 'Stream::map', which is lazy
		.map(this::getOrders)
		.flatMap(List::stream);
}
```

I think I didn't have a use case for that yet but it's good to keep in mind.

### `Optional::or`

Another addition that lets me to think *finally*!
How often have you had an `Optional` and wanted to express "use this one; unless it is empty, in which case I want to use this other one"?
Soon we can do just that:

```java
Optional<T> or(Supplier<Optional<T>> supplier);
```

Say we need some customer's data, which we usually get from a remote service.
But because accessing it is expensive and we're very clever, we have a local cache instead.
Two actually, one on memory and one on disk.
(I can see you cringe.
Relax, it's just an example.)

This is our local API for that:

```java
public interface Customers {

	Optional<Customer> findInMemory(String customerId);

	Optional<Customer> findOnDisk(String customerId);

	Optional<Customer> findRemotely(String customerId);

}
```

Chaining those calls in Java 8 is verbose (just try it if you don't believe me).
But with `Optional::or` it becomes a piece of cake:

```java
public Optional<Customer> findCustomer(String customerId) {
	return customers.findInMemory(customerId)
		.or(() -> customers.findOnDisk(customerId))
		.or(() -> customers.findRemotely(customerId));
}
```

Isn't that cool?!
How did we even live without it?
Barely, I can tell you.
Just barely.

### `Optional::ifPresentOrElse`

This last one, I am less happy with:

```java
void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction);
```

You can use it to cover both branches of an `isPresent`-if:

```java
public void logLogin(String customerId) {
	findCustomer(customerId)
		.ifPresentOrElse(
			this::logLogin,
			() -> logUnknownLogin(customerId)
		);
}
```

Where `logLogin` is overloaded and also takes a customer, whose login is then logged.
Similarly `logUnknownLogin` logs the ID of the unknown customer.

Now, why wouldn't I like it?
Because it forces me to do both at once and keeps me from chaining any further.
I would have preferred this by a large margin:

```java
Optional<T> ifPresent(Consumer<? super T> action);

Optional<T> ifEmpty(Runnable action);
```

The case above would look similar but better:

```java
public void logLogin(String customerId) {
	findCustomer(customerId)
		.ifPresent(this::logLogin)
		.ifEmpty(() -> logUnknownLogin(customerId));
}
```

First of all, I find that more readable.
Secondly it allows me to just have the `ifEmpty` branch if I whish to (without cluttering my code with empty lambdas).
Lastly, it allows me to chain these calls further.
To continue the example from above:

```java
public Optional<Customer> findCustomer(String customerId) {
	return customers.findInMemory(customerId)
		.ifEmpty(() -> logCustomerNotInMemory(customerId))
		.or(() -> customers.findOnDisk(customerId))
		.ifEmpty(() -> logCustomerNotOnDisk(customerId))
		.or(() -> customers.findRemotely(customerId))
		.ifEmpty(() -> logCustomerNotOnRemote(customerId))
		.ifPresent(ignored -> logFoundCustomer(customerId));
}
```

The question that remains is the following: Is adding a return type to a method (in this case to `Optional::ifPresent`) an incompatible change?
Not obviously but I'm currently too lazy to investigate.
Do you know?

## `Optional`[`Double`|`Int`|`Long`]

For some reason only `stream` and `ifPresentOrElse` made it to the primitive specializations.
And they still don't have `map`.
What's going on there?
(Thanks to the Pedant for [making me](java-9-optional)<!-- comment-2838631744 --> put this in here.)

## Reflection

To sum it up:

-   Use `Optional::stream` to map an `Optional` to a `Stream`.
-   Use `Optional::or` to replace an empty `Optional` with the result of a call returning another `Optional`.
-   With `Optional::ifPresentOrElse` you can do both branches of an `isPresent`-if.

Very cool!

What do you think?
I'm sure someone out there still misses his favorite operation.
Tell me about it!
