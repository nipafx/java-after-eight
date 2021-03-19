---
title: "The Serialization Proxy Pattern"
tags: [clean-code, patterns, serialization]
date: 2014-10-29
slug: java-serialization-proxy-pattern
description: "A presentation of the Serialization Proxy Pattern as defined in Effective Java. It defines the pattern, describes its implementation and gives examples."
searchKeywords: "Serialization Proxy Pattern"
featuredImage: serialization-proxy-pattern
repo: serialization-proxy-pattern
---

In my [last post](java-concepts-serialization), I talked about serialization in general.
This one is much more focused and presents a single detail: the *Serialization Proxy Pattern*.
It is a good and often the best way to deal with many of the issues with serialization.
If there was only one thing a developer would want to know about the topic, I'd tell them this.

As far as I know, the pattern was first defined in Joshua Bloch's excellent book [Effective Java](http://www.amazon.com/Effective-Java-Edition-Joshua-Bloch/dp/0321356683) (1st edition: item 57; 2nd edition: [item 78](http://books.google.de/books?id=ka2VUBqHiWkC&pg=PA297&source=gbs_toc_r&cad=3#v=onepage&q&f=false)).
This post mostly restates what is said there and focuses on presenting a detailed definition of the pattern before giving two short examples and finally covering the pros and cons.

The code samples used throughout this post come from a [demo project](https://github.com/nipafx/demo-serialization-proxy-pattern) I created on GitHub.
Check it out for more details!

## Serialization Proxy Pattern

This pattern is applied to a single class and defines its mechanism of serialization.
For easier readability, the following text will refer to that class or its instances as the *original* one or ones, respectively.

### The Serialization Proxy

As the name suggests the pattern's key is the *serialization proxy*.
It is written to the byte stream instead of the original instance.
After it is deserialized it will create an instance of the original class which takes its place in the object graph.

<contentimage slug="serialization-proxy" options="bg"></contentimage>

The goal is to design the proxy such that it is the best possible [logical representation](java-concepts-serialization#logical) of the original class.

### Implementation

The `SerializationProxy` is a static nested class of the original class.
All its fields are final and its only constructor has an original instance as its sole argument.
It extracts the logical representation of that instance's state and assigns it to its own fields.
As the original instance is considered "safe", there is no need for consistency checks or defensive copying.

The original as well as the proxy class implement Serializable.
But since the former is never actually written to the stream, only the latter needs a [*stream unique identifier*](http://docs.oracle.com/javase/8/docs/platform/serialization/spec/class.html#a4100) (often called the *serial version UID*).

#### Serializing

When an original instance is to be serialized, the serialization system can be informed to instead write the proxy to the byte stream.
To do this, the original class must implement the following method:

```java
private Object writeReplace() {
	return new SerializationProxy(this);
}
```

#### Deserializing

On deserialization this translation from original to proxy instance has to be inverted.
This is implemented in the following method in the `SerializationProxy`, which is called after a proxy instance was successfully deserialized:

```java
private Object readResolve() {
	// create an instance of the original class
	// in the state defined by the proxy's fields
}
```

Creating an instance of the original class will be done via its regular API (e.g. a constructor).

##### Artificial Byte Stream

Due to `writeReplace` regular byte streams will only contain encodings of the proxy.
But the same is not true for [artificial streams](java-concepts-serialization#artificial-byte-stream)!
They can contain encodings of original instances and as deserializing those is not covered by the pattern, it does not provide any safeguards for that case.

Deserializing such instances is in fact unwanted and has to be prevented.
This can be done by letting the method in the original class which is called in that case throw an exception:

```java
private void readObject(ObjectInputStream stream) throws InvalidObjectException {
	throw new InvalidObjectException("Proxy required.");
}
```

## Examples

The following examples are excerpts from a [complete demo project](https://github.com/nipafx/demo-serialization-proxy-pattern).
They only show the juicy parts and leave out some details (like `writeReplace` and `readObject`).

### ComplexNumber

The simple case is the one of an immutable type for [complex numbers](http://en.wikipedia.org/wiki/Complex_number), called `ComplexNumber` (surprise!).
For the sake of this example, it stores the coordinates as well as the polar form in its fields (supposedly for performance reasons):

```java
private final double real;
private final double imaginary;
private final double magnitude;
private final double angle;
```

The serialization proxy looks like this:

```java
private static class SerializationProxy implements Serializable {

	private final double real;
	private final double imaginary;

	public SerializationProxy(ComplexNumber complexNumber) {
		this.real = complexNumber.real;
		this.imaginary = complexNumber.imaginary;
	}

	/**
	 * After the proxy is deserialized, it invokes a static factory method
	 * to create a 'ComplexNumber' "the regular way".
	 */
	private Object readResolve() {
		return ComplexNumber.fromCoordinates(real, imaginary);
	}
}
```

As can be seen, the proxy does not store the polar form values.
The reason is that it should capture the best logical representation.
And since only one pair of values (either coordinates or polar form) is needed to create the other, only one is serialized.
This prevents the implementation detail of storing both pairs for better performance from leaking into the public API via serialization.

Note that all fields in the original class as well as the proxy are final.
Also note the call of the static factory method, making any added validity checks unnecessary.

### InstanceCache

The `InstanceCache` is a [heterogeneous type-safe container](http://stackoverflow.com/q/6139325/2525313) which uses a map from classes to their instances as a backing data structure:

```java
private final ConcurrentMap<Class<?>, Object> cacheMap;
```

Since the map can contain arbitrary types, not all of them have to be serializable.
The class's contract states that it suffices to store the serializable ones.
It is hence necessary to filter the map.
An advantage of the proxy is that it is the single point for all such code:

```java
private static class SerializationProxy implements Serializable {

	// array lists are serializable
	private final ArrayList<Serializable> serializableInstances;

	public SerializationProxy(InstanceCache cache) {
		serializableInstances = extractSerializableValues(cache);
	}

	private static ArrayList<Serializable> extractSerializableValues(
			InstanceCache cache) {

		return cache.cacheMap.values().stream()
				.filter(instance -> instance instanceof Serializable)
				.map(instance -> (Serializable) instance)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * After the proxy is deserialized, it invokes a constructor to create
	 * an 'InstanceCache' "the regular way".
	 */
	private Object readResolve() {
		return new InstanceCache(serializableInstances);
	}

}
```

## Pros and Cons

The Serialization Proxy Pattern mitigates many of the problems of the serialization system.
In most cases it is the best option to implement serialization and should be the default way to approach it.

### Pros

These are the advantages...

#### Lessened Extralinguistic Character

The central advantage of the pattern is that it reduces the [extralinguistic character](java-concepts-serialization#extralinguistic-character) of serialization.
This is mainly achieved by using a class's public API to create instances (see `SerializationProxy.readResolve` above).
Hence *every* creation of an instance goes through the constructor(s) and all code which is necessary to properly initialize an instance is always executed.

This also implies that such code does not have to be explicitly called during deserialization, which prevents its duplication.

#### No Limitation on Final Fields

Since the deserialized instance is initialized in its constructor, this approach does not limit which fields can be final (which is usually the case with a [custom serialized form](java-concepts-serialization#custom-serialized-form)).

#### Flexible Instantiation

It is actually not necessary for the proxy's `readResolve` to return an instance of the same type as was serialized.
It can also return any subclass.

Bloch gives the following example:

> Consider the case of `EnumSet`.
This class has no public constructors, only static factories.
From the client's perspective, they return `EnumSet` instances, bit in fact, they return one of two subclasses, depending on the size of the underlying enum type.
If the underlying enum type has sixty-four or fewer elements, the static factories return a `RegularEnumSet`; otherwise, they return a `JumboEnumSet`.
>
> Now consider what happens if you serialize an enum set whose enum type has sixty elements, then add five more elements to the enum type, and then deserialize the enum set.
It was a `RegularEnumSet` instance when it was serialized, but it had better be a `JumboEnumSet` instance once it is deserialized.
>
> Effective Java, 2nd edition: p.
314

The proxy pattern makes this trivial: `readResolve` just returns an instance of the matching type.
(This only works well if the types conform to the [Liskov substitution principle](http://en.wikipedia.org/wiki/Liskov_substitution_principle).)

#### Higher Security

It also greatly reduces the extra thought and work necessary to prevent certain attacks with artificial byte streams.
(Assuming the constructors are properly implemented.)

#### Conforms To The Single Responsibility Principle

Serialization is typically not a functional requirement of a class but still vastly changes the way it is implemented.
This problem can not be removed but at least reduced by a better separation of responsibilities.
Let the class do what it was made for and let the proxy take care of serialization.
This means that the proxy contains all nontrivial code regarding serialization but nothing else.

As usual for the [SRP](http://blog.8thlight.com/uncle-bob/2014/05/08/SingleReponsibilityPrinciple.html), this greatly improves readability.
All behavior regarding serialization can be found in one place.
And the serialized form is also much easier to spot as it suffices in most cases to just look at the proxy's fields.

### Cons

Joshua Bloch describes some limitations of the pattern.

#### Unsuited For Inheritance

> It is not compatible with classes that are extendable by their clients.
>
> Effective Java, 2nd edition: p.
315

Yep, that's it.
No further comment.
I don't quite understand that point but I'll find out more...

#### Possible Problems With Circular Object Graphs

> It is not compatible with some classes whose object graphs contain circularities: if you attempt to invoke a method on an object from within its serialization proxy's `readResolve` method, you'll get a `ClassCastException`, as you don't have the object yet, only its serialization proxy.
>
> Effective Java, 2nd edition: p.
315

#### Performance

The proxy adds a constructor execution to both serializing and deserializing.
Bloch gives an example where this was 14 percent more expensive on his machine.
This is of course no precise measurement but corroborates the theory that those constructor calls are not for free.

## Reflection

We have seen how the serialization proxy pattern is defined and implemented as well as which pros and cons it has.
It should have become clear that it has some major advantages over default and custom serialization and should be used whenever applicable.

A final word from Joshua Bloch:

> In summary, consider the serialization proxy pattern whenever you find yourself having to write `readObject` or `writeObjet` method \[for a custom serialized form\] on a class that is not extendable by its clients.
This pattern is perhaps the easiest way to robustly serialize objects with nontrivial invariants.
>
> Effective Java, 2nd edition: p.
315
