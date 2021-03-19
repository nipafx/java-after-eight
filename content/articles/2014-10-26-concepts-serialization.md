---
title: "Concepts of Serialization"
tags: [java-basics, serialization]
date: 2014-10-26
slug: java-concepts-serialization
description: "A close look at serialization and a presentation of some key concepts of Java's serialization system."
searchKeywords: "Serialization"
featuredImage: serialization-concepts
---

With all this talk about [why Optional isn't serializable](why-isnt-java-optional-serializable) and what to do about it (coming up soon), let's have a closer look at serialization.

This post presents some key concepts of serialization.
It tries to do so succinctly without going into great detail, which includes keeping advice to a minimum.
It has no narrative and is more akin to a wiki article.
The main source is Joshua Bloch's excellent book [*Effective Java*](http://www.amazon.com/Effective-Java-Edition-Joshua-Bloch/dp/0321356683), which has several items covering serialization (1st edition: 54-57; 2nd edition: [74-78](http://books.google.de/books?id=ka2VUBqHiWkC&pg=PA297&source=gbs_toc_r&cad=3#v=onepage&q&f=false)).
Way more information can be found in the [official serialization specification](http://docs.oracle.com/javase/8/docs/platform/serialization/spec/serialTOC.html)

## Definition

With [Serialization](http://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html) instances can be encoded as a byte stream (called *serializing*) and such a byte stream can be turned back into an instance (called *deserializing*).

The key feature is that both processes do not have to be executed by the same JVM.
This makes serialization a mechanism for storing objects on disk between system runs or transferring them between different systems for remote communication.

## Extralinguistic Character

Serialization is a somewhat strange mechanism.
It converts instances into a stream of bytes and vice versa with only little visible interaction with the class.
Neither does it call accessors to get to the values nor does it use a constructor to create instances.
And for that to happen all the developer of the class is required to do is implement an interface with no methods.

Bloch describes this as an *extralinguistic character* and it is the root for many of the issues with serialization.

### Methods

The serialization process can be customized by implementing some of the following methods.
They can be private and the JVM will find them based on their signature.
The descriptions are taken from the [class comment on `Serializable`](http://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html).

-   `private void writeObject(java.io.ObjectOutputStream out) throws IOException`
	Is responsible for writing the state of the object for its particular class so that the corresponding readObject method can restore it.
-   `private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException`
	Is responsible for reading from the stream and restoring the classes fields.
-   `private void readObjectNoData() throws ObjectStreamException`
	Is responsible for initializing the state of the object for its particular class in the event that the serialization stream does not list the given class as a superclass of the object being deserialized.
-   `ANY-ACCESS-MODIFIER Object writeReplace() throws ObjectStreamException`
	Designates an alternative object to be used when writing an object of this class to the stream.
-   `ANY-ACCESS-MODIFIER Object readResolve() throws ObjectStreamException;`
	Designates a replacement object when an instance of this class is read from the stream.

A good way to deal with the extralinguistic character of deserialization is to see all involved methods as an additional constructor of that class.

The object streams involved in (de)serializing provide these helpful default (de)serialization methods:

-   `java.io.ObjectOutputStream.defaultWriteObject() throws IOException`
	Writes the non-static and non-transient fields of the current class to this stream.
-   `java.io.ObjectInputStream.defaultReadObject() throws IOException, ClassNotFoundException`
	Reads the non-static and non-transient fields of the current class from this stream.

### Invariants

One effect of not using a constructor to create instances is that a class's invariants are not automatically established on deserialization.
So while a class does usually check all constructor arguments for validity, this mechanism is not automatically applied to the deserialized values of fields.

Implementing such a check for deserialization is an extra effort which easily leads to code duplication and all the problems it typically ensues.
If forgotten or done carelessly, the class is open for bugs or security holes.

## Serialized Form

The structure of a serializable class's byte stream encoding is called its [*serialized form*](http://docs.oracle.com/javase/8/docs/platform/serialization/spec/protocol.html).
It is mainly defined by the names and types of the class's fields.

The serialized form has some properties that are not immediately obvious.
While some of the problematic ones can be mitigated by carefully defining the form, they will usually still be a burden on future development of a class.

### Public API

The most important property of the serialized form is:

**It is part of the class's public API!**

From the moment a serializable class is deployed, it has to be assumed that serialized instances exist.
And it is usually expected of a system to support the deserialization of instances which were created with older versions of the same system.
Users of a class rely on its serialized form as much as on its documented behavior.

### Reduced Information Hiding

The concept of information hiding allows a class to maintain its documented behavior while changing its way of implementing it.
This expressively includes the representation of its state, which is usually hidden and can be adapted as needed.
Since the serialized form, which captures that representation of the state, becomes part of the public API so does the representation itself.

A serializable class only effectively hides the implementation of its behavior while exposing the definition of that behavior *and* the state it uses to implement it.

### Reduced Flexibility

Hence, like changing a class's API (e.g. by changing or removing methods or altering their documented behavior) might break code using it, so does changing the serialized form.
It is easy to see that improving a class becomes vastly more difficult if its fields are fixed.
This greatly reduces the flexibility to change such a class if the need arises.

> Making something in the JDK serializable makes a dramatic increase in our maintenance costs, because it means that the representation is frozen for all time.
This constrains our ability to evolve implementations in the future, and the number of cases where we are unable to easily fix a bug or provide an enhancement, which would otherwise be simple, is enormous.
So, while it may look like a simple matter of "implements Serializable" to you, it is more than that.
The amount of effort consumed by working around an earlier choice to make something serializable is staggering.
>
> [Brian Goetz](http://mail.openjdk.java.net/pipermail/jdk8-dev/2013-September/003276.html)

### Increased Testing Effort

If a serializable class is changed, it is necessary to test whether serialization and deserialization works across different versions of the system.
This is no trivial task and will create measurable costs.

## Class representations

The serialized from represents a class but not all representations are equal.

### Physical

If a class defines fields with reference types (i.e.
non-primitives), its instances contain pointers to instances of those types.
Those instance, in turn, can point to other ones and so on.
This defines a directed graph of interlinked instances.
The physical representation of an instance is the graph of all instances reachable from it.

As an example, consider a doubly linked list.
Each element of the list is contained in a node and each node knows the previous and the next one.
This is basically already the list's physical representation.
A list with a dozen elements would be a graph of 13 nodes.
The list instance points to the first and last list node and starting from there one can traverse the ten nodes in between in both directions.

One way to serialize an instance of a class is to simply traverse the graph and serialize each instance.
This effectively writes the physical representation to the byte stream, which is the default serialization mechanism.

While the physical representation of a class is usually an implementation detail, this way to serialize it exposes this otherwise hidden information.
Serializing the physical representation effectively binds the class to it which makes it extremely hard to change it in the future.
There are other disadvantages, which are described in *Effective Java* (p.
297 in 2nd edition).

### Logical

The logical representation of a class's state is often more abstract.
It is usually more removed from the implementation details and contains less information.
When trying to formulate this representation, it is advisable to push both aspects as far as possible.
It should be as implementation independent as possible and should be minimal in the sense that leaving out any bit of information makes it impossible to recreate an instance from it.

To continue the example of the linked list, consider what it actually represents: just some elements in a certain order.
Whether these are contained in nodes or not and how those hypothetical nodes might be linked is irrelevant.
A minimal, logical representation would hence only consist of those elements.
(In order to properly recreate an instance from the stream it is necessary to add the number of elements.
While this is redundant information it doesn't seem to hurt much.)

So a good logical representation only captures the state's abstract structure and not the concrete fields representing it.
This implies that while changing the former is still problematic the latter can be evolved freely.
Compared to serializing the physical representation this restores a big part of the flexibility for further development of the class.

## Serialization Patterns

There are at least three ways to serialize a class.
Calling all of them patterns is a little overboard so the term is used loosely.

### Default Serialized Form

This is as simple as adding `implements Serializable` to the declaration.
The serialization mechanism will then write all non-transient fields to the stream and on deserialization assign all the values present in a stream to their matching fields.

This is the most straight forward way to serialize a class.
It is also the one where all the sharp edges of serialization are unblunted and waiting for their turn to really hurt you.
The serialized form captures the physical representation and there is absolutely no checking of invariants.

### Custom Serialized Form

By implementing `writeObject` a class can define what gets written to the byte stream.
A matching `readObject` must read an according stream and use the information to assign values to fields.

This approach allows more flexibility than the default form and can be used to serialize the class's logical representation.
There are some details to consider and I can only recommend to read the respective item in *Effective Java* (item 55 in 1st edition; item 75 in 2nd edition).

### Serialization Proxy Pattern

In this case the instance to serialize is replaced by a proxy.
This proxy is written to and read from the byte stream instead of the original instance.
This is achieved by implementing the methods `writeReplace` and `readResolve`.

In most cases this is by far the best approach to serialization.
It deserves [its own post](java-serialization-proxy-pattern) and it will get it soon ([stay](feed.xml) [tuned](news)).

## Misc

Some other details about serialization.

### Artificial Byte Stream

The happy path of deserialization assumes a byte stream which was created by serializing an instance of the same class.
While doing so is alright in most situations, it must be avoided in security critical code.
This includes any publicly reachable service which uses serialization for remote communication.

Instead the assumption must be that an attacker carefully handcrafted the stream to violate the class's invariants.
If this is not countered, the result can be an unstable system which might crash, corrupt data or be open for attacks.

### Documentation

Javadoc has special annotations to document the serialized form of a class.
For this it creates [a special page in the docs](http://docs.oracle.com/javase/8/docs/api/serialized-form.html) where it lists the following information:

-   The tag `@serialData` can annotate methods and the following comment is supposed to document the data written do the byte stream.
The method signature and the comment is shown under *Serialization Methods*.
-   The tag `@serial` can annotate fields and the following comment is supposed to describe the field.
The field's type and name and the comment are then listed under *Serialized Fields*.

A good example is the [documentation for the LinkedList](http://docs.oracle.com/javase/8/docs/api/serialized-form.html#java.util.LinkedList).
