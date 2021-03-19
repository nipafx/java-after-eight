---
title: "The Decorator Pattern With Default Methods"
tags: [clean-code, patterns, default-methods, java-8]
date: 2014-09-30
slug: decorator-pattern-default-methods
description: "Use Java 8's default methods to make the decorator pattern even more beautiful, which results in more concise and readable code."
searchKeywords: "decorator pattern default methods"
featuredImage: decorator-pattern-default-methods
repo: decorator-java-8
---

In [a recent post](decorator-pattern-saved-my-day) I described how the decorator pattern saved my day.
I gave a small code snippet which contained the simplest way to create decorators but promised that there would be a nicer way with Java 8.

Here it is:

```java
HyperlinkListener listener = this::changeHtmlViewBackgroundColor;
listener = DecoratingHyperlinkListener.from(listener)
	.onHoverMakeVisible(urlLabel)
	.onHoverSetUrlOn(urlLabel)
	.logEvents()
	.decorate(l -> new OnActivateHighlightComponent(l, urlLabel))
	.decorate(OnEnterLogUrl::new);
```

I'll spend the rest of the post explaining how to get there.

To continue on my last post it uses Swing's `HyperlinkListener` as a basis for decoration.
This has the added advantage of keeping it simple as that interface is not generic and has only one method with only one argument (nice for lambda expressions!).

<admonition type="caveat">

Like the earlier post, this one also doesn't try to teach the pattern itself.
(I found another [nice explanation](http://javapapers.com/design-patterns/decorator-pattern/), though.)
Instead, it recommends a way to implement it in Java 8 such that it becomes very convenient to use.
As such, the post heavily relies on Java 8 features, especially [default methods](http://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html) and [lambda expressions](http://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html).

</admonition>

The diagrams are just sketches and leave out a lot of details.
More complete ones are [easy to find](https://encrypted.google.com/search?tbm=isch&q=decorator%20pattern&tbs=imgo:1).

## Vanilla

<contentimage slug="decorator-pattern-diagram-vanilla" options="bg"></contentimage>

In the usual realization of the pattern there is an interface (called `Component` above), which will be implemented in the regular way by "normal" classes as well as all the decorators.

### The Abstract Decorator Class

The decorators usually inherit from an intermediate abstract base class (`AbstractDecorator`), which eases the implementation.
It takes another component as a constructor argument and implements the interface itself by forwarding all calls to it.
Thus, the behavior of the decorated component is unchanged.

It is now up to the subclasses to actually alter it.
They do this by selectively overriding those methods, whose behavior they want to change.
This often includes calls to the decorated component.

### Creation Of Decorators

Usually, no special technique is used to create the decorators; just simple constructors.
With complicated decorators you might even use a factory.

I'm a big fan of static constructor methods so I use them and make the constructors private.
In order to keep callers of these methods in the dark about the details, I declare the return type of those methods as `Component` as opposed to the more detailed type of the decorator.
This can, for example, be seen in [*LogEventsToConsole*](https://github.com/nipafx/demo-decorator-java-8/blob/master/src/org/codefx/lab/decorator/def/LogEventsToConsole.java).

My proposal changes the way decorators are created.

## With Java 8

<contentimage slug="decorator-pattern-diagram-default-methods" options="bg"></contentimage>

To use all the power of Java 8 I recommend to add a special interface for all decorators, the `DecoratingComponent`.
The abstract superclass for decorators implements that interface but, as before, only holds a reference to `Component`.

It is important to notice that due to the definition of the new interface (see below) nothing changes for the concrete decorators.
They are exactly identical in both realizations of the pattern.
The abstract class also undergoes virtually no change (see further below) so switching to this solution has no noticeable costs.

### The New Interface

The new interface `DecoratingComponent` extends the basic component interface and provides factory methods for decorators.
These are static or [default/defender methods](java-default-methods-guide) (so they are already implemented [and would be final if they could be](http://stackoverflow.com/a/23476994)) and no abstract methods should be declared.
This way, the new interface does not add an extra burden on the implementations further down the inheritance tree.

Regarding the following code samples: The generic ones were only created for this post.
The ones which involve hyperlink listeners come from the [demo application](https://github.com/nipafx/demo-decorator-java-8).
Most notable is the `DecoratingHyperlinkListener` ([link to source file](https://github.com/nipafx/demo-decorator-java-8/blob/master/src/org/codefx/lab/decorator/def/DecoratingHyperlinkListener.java)), which extends Swing's [HyperlinkListener](http://docs.oracle.com/javase/8/docs/api/javax/swing/event/HyperlinkListener.html).

#### Methods

The interface itself is actually quite simple and consists of three types of methods.

##### Adapter

To quickly move from a `Component` to a `DecoratingComponent`, the interface should have a static method which takes the first and returns the latter.
Since `DecoratingComponent` extends `Component` and adds no abstract methods, this is trivial.
Simply create an anonymous implementation and forward all calls to the adapted component.

The general approach would look like this:

```java
static DecoratingComponent from(Component component) {
	DecoratingComponent adapted = new DecoratingComponent() {
		@Override
		public SomeReturn someMethod(SomeArgument argument) {
			return component.someMethod(argument);
		}

		// ... more methods here ...
	};
	return adapted;
}
```

In case of the `DecoratingHyperlinkListener` it is much easier because it's a functional interface so a lambda expression can be used:

```java
static DecoratingHyperlinkListener from(HyperlinkListener listener) {
	return event -> listener.hyperlinkUpdate(event);
}
```

##### Generic Decoration

This is the essential method of the interface:

```java
default DecoratingComponent decorate(
		Function<? super DecoratingComponent, ? extends DecoratingComponent>
			decorator) {

	return decorator.apply(this);
}
```

It takes a function from one decorating component to another as an argument.
It applies the function to itself to create a decorated instance, which is then returned.

This method can be used throughout the whole code to decorate any component in a simple and readable way:

```java
Component some = ...;
DecoratingComponent decorated = DecoratingComponent
	// create an instance of 'DecoratingComponent' from the 'Component'
	.from(some)
	// now decorate it
	.decorate(component -> new MyCoolComponentDecorator(component, ...));

// if you already have an instance of 'DecoratingComponent', it get's easier
decorated = decorated
	.decorate(component -> new MyBestComponentDecorator(component, ...));

// constructor references are even clearer (but cannot always be used)
decorated = decorated.decorate(MyBestComponentDecorator::new);
```

##### Concrete Decorations

You can also add methods to decorate instances with concrete decorators:

```java
default DecoratingHyperlinkListener logEvents() {
	return LogEventsToConsole.decorate(this);
}

default DecoratingHyperlinkListener onHoverMakeVisible(JComponent component) {
	return OnHoverMakeComponentVisible.decorate(this, component);
}
```

They make decorating very succinct and readable:

```java
DecoratingComponent decorated = ...
decorated = decorated.logEvents();
```

But it is debatable whether these methods should really be added.
While they are very convenient, a strong argument can be made against them as they create a circular dependency.
Not only do the decorators know about the interface (which they implement indirectly via the abstract superclass), now the interface also knows its implementations.
In general this is a pungent code smell.

The final call is not yet in on this but I recommend a pragmatic middle way.
I let the interface know about the implementations which live in the same package.
This will be the generic ones as they do not reference anything too concrete from the rest of my code.
But I would not let it know about every crazy decorator I created deep in the bowels of the system.
(And of course I would neither add all those decorators to the same package unless it's already called *the\_kraken*...)

#### Why an Extra Interface?

Yes, yes, all those Java 8 features are very nice but couldn't you simply add these methods to `AbstractDecorator`?
Good question!

Of course, I could've just added them there.
But I don't like that solution for two reasons.

##### Single Responsibility Principle

First, that would blur the responsibilities of the classes.
The new interface is responsible for decorating instances of `Component`, the abstract superclass is responsible for enabling easy implementation of decorators.

These are not the same things and they do not change for the same reason.
The new interface might change whenever a new decorator has to be included.
The abstract class will change whenever `Component` changes.

##### Type Hierarchy

If these methods were added to `AbstractDecorator`, they could only be called on such instances.
So all decorators would have to inherit from that class, which limits the range for future implementations.
Who knows, maybe some really good reason comes up, why another class can not be an `AbstractDecorator`.

Worse though, all decorators would have to expose the fact that they are an `AbstractDecorator`.
Suddenly there is an abstract class, which was only created to ease the implementation, creeping through the whole code base.

### Other Differences

Besides introducing the new interface this variation of the pattern does not change much.

#### Changes To The Abstract Decorator Class

If you have access to the class, you should let it implement `DecoratingComponent` instead of `Component`.
As no new abstract methods were introduced this entails no further changes.
This is shown in the UML diagram above.

If you can not change the class, your decorators will only implement `Component`.
This will keep you from using their constructors to create a function which maps a component to a decorating component.
As you need that function as an argument for the `decorate` method, you have to change that method to look as follows:

```java
// note the more general second type of the 'Function' interface
default DecoratingComponent decorate(
		Function<? super DecoratingComponent, ? extends Component> decorator) {

	// create the decorated instance as before
	Component decorated = decorator.apply(this);
	// since it is no 'DecoratingComponent' use 'from' to turn it into one
	return from(decorated);
}
```

#### Changes To The Decorators

No changes to those classes are necessary.
Unless of course, you are one of those crazy people who use static factory methods.
Than you would have to make sure that they declare their return type as `DecoratingComponent` or you're in the same situation as when the abstract superclass can not implement the new interface.
If you can not change the decorator classes, the same solution works here.

### Example

So let's look at the snippet from above again:

```java
// create a 'HyperlinkListener' with a method reference
HyperlinkListener listener = this::changeHtmlViewBackgroundColor;
// decorate that instance with different behaviors
// (note that each call actually returns a new instance
//  so the result has to be assigned to a variable)
listener = DecoratingHyperlinkListener
	// adapt the 'HyperlinkListener' to be a 'DecoratingHyperlinkListener'
	// (looks better if it is not on its own line)
	.from(listener)
	// call some concrete decorator functions
	.onHoverMakeVisible(urlLabel)
	.onHoverSetUrlOn(urlLabel)
	.logEvents()
	// call the generic decorator function with a lambda expression
	.decorate(l -> new OnActivateHighlightComponent(l, urlLabel))
	// call the generic decorator function with a constructor reference
	.decorate(OnEnterLogUrl::new);
```

## Reflection

We saw how Java 8's static and default interface methods can be used to create a fluent API for the decorator pattern.
It makes the code more concise and more readable at the same time while not interfering with the pattern's mechanism.

As it is, we used the default methods to create [traits](http://www.scala-lang.org/old/node/126) about which [Brian Goetz writes](http://stackoverflow.com/a/23476994):

> The key thing to understand about default methods is that the primary design goal is *interface evolution*, not "turn interfaces into (mediocre) traits"

Sorry Brian, it was just too tempting.
;)

Got some insights on the decorator pattern?
Want to improve on my idea or criticize it?
Then leave a comment!
And don't forget to check out [the code on GitHub](https://github.com/nipafx/demo-decorator-java-8).
