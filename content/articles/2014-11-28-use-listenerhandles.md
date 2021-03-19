---
title: "Don't Remove Listeners - Use ListenerHandles"
tags: [clean-code, javafx, libfx]
date: 2014-11-28
slug: java-listenerhandles
description: "Keeping references around to remove listeners is a hazard. ListenerHandles encapsulate the complexity and LibFX has an implementation."
searchKeywords: "listenerhandle"
featuredImage: use-listener-handle
---

Listening to an observable instance and reacting to its changes is fun.
Doing what is necessary to interrupt or end this listening is way less fun.
Let's have a look at where the trouble comes from and what can be done about it.

While the examples use Java, the deficiency is present in many other languages as well.
The proposed solution can be applied in all object oriented languages.
Those too lazy to implement the abstraction in Java themselves, can use [**LibFX**](http://libfx.codefx.org).

## The Situation

Say we want to listen to the changes of a property's value.
That's straight forward:

```java
private void startListeningToNameChanges(Property<String> name) {
	name.addListener((obs, oldValue, newValue) -> nameChanged(newValue));
}
```

Now assume we want to interrupt listening during certain intervals or stop entirely.

## Keeping References Around

The most common approach to solve this is to keep a reference to the listener and another one to the property around.
Depending on the concrete use case, the implementations will differ, but they all come down to something like this:

```java
private Property<String> listenedName;
private ChangeListener<String> nameListener;

...

private void startListeningToNameChanges(Property<String> name) {
	listenedName = name;
	nameListener = (obs, oldValue, newValue) -> nameChanged(newValue);
	listenedName.addListener(nameListener);
}

private void stopListeningToNameChanges() {
	listenedName.removeListener(nameListener);
}
```

While this might look ok, I'm convinced it's actually a bad solution (albeit being the default one).

First, the extra references clutter the code.
It is hard to make them express the intent of why they are kept around, so they reduce readability.

Second, they increase complexity by adding a new invariant to the class: The property must always be the one to which the listener was added.
Otherwise the call to `removeListener` will silently do nothing and the listener will still be executed on future changes.
Unriddling this can be nasty.
While upholding that invariant is easy if the class is short, it can become a problem if it grows more complex.

Third, the references (especially the one to the property) invite further interaction with them.
This is likely not intended but nothing keeps the next developer from doing it anyway (see the first point).
And if someone *does* start to operate on the property, the second point becomes a very real risk.

These aspects already disqualify this from being the default solution.
But there is more!
Having to do this in many classes leads to code duplication.
And finally, the implementation above contains a race condition.

## ListenerHandle

Most issues come from handling the observable and the listener directly in the class which needs to interrupt/end the listening.
This is unnecessary and all of these problems go away with a simple abstraction: the `ListenerHandle` .

```java
public interface ListenerHandle {
	void attach();
	void detach();
}
```

The ListenerHandle holds on to the references to the observable and the listener.
Upon calls to `attach()` or `detach()` it either adds the listener to the observable or removes it.
For this to be embedded in the language, all methods which currently add listeners to observables should return a handle to that combination.

Now all that is left to do is to actually implement handles for all possible scenarios.
Or convince those developing your favorite programming language to do it.
This is left as an exercise to the reader.

Note that this solves all problems described above with the exception of the race condition.
There are two ways to tackle this:

-   handle implementations could be inherently thread-safe
-   a synchronizing decorator could be implemented

## ListenerHandles in LibFX

As a Java developer you can use **LibFX**, which supports listener handles on three levels.

### Features Are Aware Of ListenerHandles

Every feature of **LibFX** which can do so without conflicting with the Java API returns a `ListenerHandle` when adding listeners.

Take the [WebViewHyperlinkListener](https://github.com/nipafx/LibFX/wiki/WebViewHyperlinkListener) as an example:

```java
WebView webView;

ListenerHandle eventProcessingListener = WebViews
	.addHyperlinkListener(webView, this::processEvent);
```

### Utilities For JavaFX

Since **LibFX** has strong connections to JavaFX (who would have thought!), it provides a utility class which adds listeners to observables and returns handles.
This is implemented for all observable/listener combinations which exist in JavaFX.

As an example, let's look at at the combination `ObservableValue<T>` / `ChangeListener<?
superT>`:

```java
public static <T> ListenerHandle createAttached(
		ObservableValue<T> observableValue,
		ChangeListener<? super T> changeListener);

public static <T> ListenerHandle createDetached(
		ObservableValue<T> observableValue,
		ChangeListener<? super T> changeListener);
```

### ListenerHandleBuilder

In all other cases, i.e.
for any observable/listener combination not covered above, a handle can be created with a builder:

```java
// These classes do not need to implement any special interfaces.
// Their only connection are the methods 'doTheAdding' and 'doTheRemoving',
// which the builder does not need to know about.
MyCustomObservable customObservable;
MyCustomListener customListener;

ListenerHandles
		.createFor(customObservable, customListener)
		.onAttach((obs, listener) -> obs.doTheAdding(listener))
		.onDetach((obs, listener) -> obs.doTheRemoving(listener))
		.buildAttached();
```

## Reactive Programming

While this is no post on [reactive programming](http://en.wikipedia.org/wiki/Reactive_programming), it should still be mentioned.
Check out [ReactiveX](http://reactivex.io/) (for many languages including Java, Scala, Python, C++, C\# and more) or [ReactFX](https://github.com/TomasMikula/ReactFX) (or [this introductory post](http://tomasmikula.github.io/blog/2014/05/01/reactfxs-general-stream-combinator-state-machine.html)) for some implementations.

## Reflection

We have seen that the default approach to remove listeners from observables produces a number of hazards and needs to be avoided.
The listener handle abstraction provides a clean way around many/all problems and [LibFX](http://libfx.codefx.org) provides an implementation.
