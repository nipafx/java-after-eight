---
title: "Serialize `Optional`"
tags: [java-8, optional, serialization]
date: 2014-11-02
slug: serialize-java-optional
description: "A summary of why you can't serialize `Optional` and what can be done to deal with that limitation if necessary."
searchKeywords: "serialize optional"
featuredImage: serialize-optional
repo: serialize-optional
---

In [a recent post](why-isnt-java-optional-serializable) I explained why we can't serialize Optional.
But what if, after all that, we still really, really want to?
Let's see how to come as close as possible.

We'll first take a look at possible scenarios in which we want to serialize Optional and then present a serializable wrapper for it.
(By the way, the next release of [LibFX](tag:libfx) will contain that wrapper.)
Finally, bringing both together, we'll see solutions for the different scenarios.

## When To Serialize Optional

Do restate the facts: `Optional` does not implement `Serializable`.
And it is final, which prevents us from creating a serializable subclass.

There are at least two scenarios which require to serialize an Optional:

-   It could be an argument or return type of a method which is send over the wire with a serialization-based [RPC](http://en.wikipedia.org/wiki/Remote_procedure_call) framework, like [RMI](http://en.wikipedia.org/wiki/Java_remote_method_invocation).
-   It could be a field in a serializable class.

Let's have a closer look at both cases.

### Serializing an Optional Argument or Return Value

In this case the argument or return type has to be serializable.
This can only be achieved by changing the method's signature and use a class which implements that interface.
Several approaches exist.

It would be possible to simply create a class which duplicates all the functionality of Optional but is serializable.
It can then be used as a full replacement.
This will likely lead to this class permeating the code base instead of the official one.
In the face of future changes to the language, [which might optimize performance if Optional follows a defined and rigid structure](why-isnt-java-optional-serializable#value-types), and for various other reasons I don't think this is a good idea.

Instead a simple class could be created which just allows to wrap and unwrap an Optional.
It would be serializable by writing/reading the object contained in that Optional.
I implemented such a class in my demo project.
It is called `SerializableOptional` and explained further below.

But let's first look at the other reason to serialize Optional.

#### Disclaimer

I do not have much architectural experience with remote calls.
On the OpenJDK mailing list Remi Forax describes downsides of having the client act upon an empty Optional.
For the exchange regarding that see [Remi's initial statement](http://mail.openjdk.java.net/pipermail/jdk8-dev/2013-September/003218.html), this [request for clarification](http://mail.openjdk.java.net/pipermail/jdk8-dev/2013-September/003221.html) and [Remi's explanation](http://mail.openjdk.java.net/pipermail/jdk8-dev/2013-September/003223.html).

### Serializing an Optional Field

If a class wants to serialize a field of type Optional, it has to customize its serialization mechanism.
This is actually a good idea for any serializable - see [chapter 11 in Java Bloch's excellent *Effective Java* (2nd Edition)](http://books.google.de/books?id=ka2VUBqHiWkC&pg=PA289&source=gbs_toc_r&cad=3#v=onepage&q&f=false).
It can either define a [custom serialized form](java-concepts-serialization#custom-serialized-form) or implement the [serialization proxy pattern](java-serialization-proxy-pattern).

As it is the recommended approach in most cases, I will only cover the proxy pattern.

#### Disclaimer

In most cases there is no need to have a nullable/optional field in a class.
A better design can often be created and should be actively looked for!
([Apparently Optional isn't serializable in order to convey that fact.](why-isnt-java-optional-serializable#return-type))

If the class must have an optional field, it should be carefully decided whether it is part of the class's logical representation.
The fact that it is nullable/optional makes it likely that it is transient and can be recreated after deserialization.
Only if that is not the case, does it make sense to serialize the field.

## Serializable Optional

The `SerializableOptional<T>` ([link](https://github.com/nipafx/demo-serialize-optional/blob/master/src/org/codefx/lab/optional/SerializableOptional.java)) only exists to wrap and unwrap an Optional and offers little of its features.
In the case of arguments or return values, it can (and in most cases should) be used without even declaring a variable of type `SerializableOptional`.

### Wrapping

The class has two methods to wrap and unwrap an Optional (where `T` always extends `Serializable` - left out for brevity):

```java
/**
 * Creates a serializable optional from the specified 'Optional'.
 */
public static <T> SerializableOptional<T> fromOptional(Optional<T> optional);

/**
 * Returns the 'Optional' instance with which this instance was created.
 */
public Optional<T> asOptional();
```

To make construction a little less verbose if no Optional exists, it has these equivalents of Optional's methods with the same name:

```java
/**
 * Usability method which creates a serializable optional which wraps
 * an empty Optional. Equivalent to 'Optional.empty()'.
 */
public static <T> SerializableOptional<T> empty();

/**
 * Usability method which creates a serializable optional for the specified
 * value by wrapping it in an 'Optional.' The value must be non-null.
 * Equivalent to 'Optional.of(Object)'.
 */
public static <T> SerializableOptional<T> of(T value);

/**
 * Usability method which creates a serializable optional for the specified
 * value by wrapping it in an 'Optional'. The value can be null.
 * Equivalent to 'Optional.ofNullable(Object)'.
 */
public static <T> SerializableOptional<T> ofNullable(T value);
```

### Serialization

`SerializableOptional` uses the serialization proxy pattern.
Its logical representation only consists of the value contained in the wrapped Optional (or null if it is empty).

Serialization then works as usual.
See the [demo](https://github.com/nipafx/demo-serialize-optional/blob/master/src/org/codefx/lab/optional/Demo.java) for different use cases.

## Serialize Optional

Let's see how to approach the situations in which we would like to serialize an argument, return value or field of type `Optional`.

### Methods With Optional Arguments Or Return Value

A method which likes to have an argument or return value of type `Optional` but needs it to be serializable, can use `SerializableOptional` instead.
Using it of course adds another layer of indirection, which leads to additional calls:

```java
// these methods require all of their argument and return types
// to be serializable (e.g. for RMI)
public SerializableOptional<String> search(int id);
public void log(int id, SerializableOptional<String> item);

// shows how to quickly wrap and unwrap an 'Optional';
// note that no local variable of type 'SerializableOptional' is needed
private void callMethods() {
	for (int id = 0; id < 7; id++) {
		// unwrap the returned optional using 'asOptional'
		Optional<String> searchResult = search(id).asOptional();
		// wrap the optional using 'fromOptional'
		// (if used often, this could be a static import)
		log(id, SerializableOptional.fromOptional(searchResult));
	}
}
```

### Fields Of Type Optional

The recommended approach to serialization is to use the serialization proxy pattern.
In that case, there are two possibilities to serialize Optional.

#### Serializing The Extracted Value

The proxy can simply have a field of the type which is wrapped by the Optional.
In its constructor it then assigns the value contained in the Optional (or null if it is empty) to that field.

This is done by the `ClassUsingOptionalCorrectly` ([link](https://github.com/nipafx/demo-serialize-optional/blob/master/src/org/codefx/lab/optional/ClassUsingOptionalCorrectly.java)):

```java
private static class SerializationProxy<T> implements Serializable {

	private final T optionalValue;

	public SerializationProxy(
			ClassUsingOptionalCorrectly<T> classUsingOptional) {

		optionalValue = classUsingOptional.optional.orElse(null);
	}
}
```

#### Using SerializableOptional

Alternatively, the serialization proxy can have an instance of `SerializableOptional`.
This is done by the class `TransformForSerializationProxy` ([link](https://github.com/nipafx/demo-serialize-optional/blob/master/src/org/codefx/lab/optional/TransformForSerializationProxy.java)):

```java
private static class SerializationProxy<T> implements Serializable {

	private final SerializableOptional<T> optional;

	public SerializationProxy(TransformForSerializationProxy<T> transform) {
		optional = SerializableOptional.fromOptional(transform.optional);
	}

}
```

The main difference to extracting the value is readability.
It makes it clearer that the logical representation contains an optional field.
The costs are an increased size of byte representation and more time to write it.
I didn't benchmark this so I can't tell whether this can be important.
I guess that, as usual, it depends.

## Reflection

We have seen the two main (and only?) reasons to serialize an Optional and what to do about it: If a method's arguments or return value needs to be serialized, use the `SerializableOptional` and immediately wrap/unwrap it when the method is called.
If a class has an optional field which it wants to serialize, its serialization proxy could either extract the Optional's value or use write the `SerializableOptional` to the byte stream.

The helper class [`SerializableOptional` from my demo project](https://github.com/nipafx/demo-serialize-optional/blob/master/src/org/codefx/lab/optional/SerializableOptional.java) is public domain and can be used without any legal limitations.
