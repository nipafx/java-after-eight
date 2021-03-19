---
title: "How The Decorator Pattern Saved My Day"
tags: [clean-code, patterns]
date: 2014-09-22
slug: decorator-pattern-saved-my-day
description: "A real-life example how the decorator pattern enables future changes and improves code quality by upholding the Single Responsibility Principle."
searchKeywords: "Decorator Pattern"
featuredImage: decorator-pattern-saved-day
---

At work I am dealing with a large Java code base, which was developed over the course of more than 15 years by many different developers.
Not all things were done by the books but at the same time I usually don't have the possibility to refactor every oddity I come across.

Still, steps towards higher code quality can be taken every day.
And today was just like that...

<admonition type="caveat">

This post does not aim at teaching the decorator pattern as [plenty](https://duckduckgo.com/?q=decorator+pattern) [tutorials](http://www.tutorialspoint.com/design_pattern/decorator_pattern.htm) [already](http://www.oodesign.com/decorator-pattern.html) [exist](http://en.wikipedia.org/wiki/Decorator_pattern).
Instead, it gives a real life example of how it came in handy and saved the day.

</admonition>

## The Situation

Our UI hosts Swing's [JEditorPanes](http://docs.oracle.com/javase/8/docs/api/javax/swing/JEditorPane.html), which are used to display HTML.
Interaction with the various links (like hovering and clicking) triggers one or more of these responses:

1. logging the event
2. changing the cursor\
	(something the JEditorPane already does on its own; seemingly since May 2000 - what the ...?!)
3. updating the pane with the linked content
4. opening an external browser
5. opening an external application
6. handling an internal service request

These responses are not the same for all panes.
There are several of them with partly different needs.
(If you know the decorator pattern, you see where this is going.)

So the question is: How do you implement these responses?

### The Solution With One Configurable Class

You could just lump all this together in one class which implements `HyperlinkListener` and (de)activate the different responses with flags.

<contentimage slug="vaskas-complex-allinone" options="narrow bg"></contentimage>

This class would be hell!
Yes, hell.
It's as simple as that.

First of all, it would be huge.
And it's likely that somehow some weird dependencies between its essentially unrelated responsibilities creeped in.
The size and these relations would make it hard to write and test and even harder to understand and modify.

(By the way, the root cause for the mess is that the `AllInOneHyperlinkListener` violates the [Single Responsibility Principle](http://blog.8thlight.com/uncle-bob/2014/05/08/SingleReponsibilityPrinciple.html).
Something I will not cover in detail as this post is already long enough.)

### The Solution With Inheritance

Anyways, I was lucky enough not to find myself dealing with one behemoth listener class.
Instead I found a small hierarchy of classes which split these responsibilities among them (*HL* is short for *HyperlinkListener*):

1. `CursorSettingHL implements HL`: logs events and sets the cursor
2. `UrlProcessingHL extends CursorSettingHL`:\
	processes a URL by updating the pane's content or opening an external browser/application
3. `ServiceRequestHandlingHL extends UrlProcessingHL`:\
	processes the URL if it is a service request; otherwise delegates to its super class

<contentimage slug="vaskas-complex-inheritance" options="narrow bg"></contentimage>

This looks better, doesn't it?
Well...

First of all, some classes still have several responsibilities.
There is no real reason why logging and changing the cursor should be done by the same class.
(I can only guess that this structure grew organically over time without any deeper design.) So the problem is smaller but not gone yet.

And it showed in the class names, too.
Those above were already improved for better readability.
The originals were full of *Default*, *Simple* and other non-information.
This or even misleading names are not a simple oversight.
They are a natural consequence of the missing [cohesion](https://pragprog.com/magazines/2010-12/cohesive-software-design).

But those problems could've been somewhat mitigated by an even deeper hierarchy.
Six classes could each implement one thing.
But that wouldn't have helped me either.

No, the real issue with this solution is the simulated flexibility.
It looks like you can pick and choose but in fact you can't.
See what happens when things change.

## The Change

We slowly move from Swing to JavaFX and I wanted to replace the JEditorPane with FX' [WebView](http://docs.oracle.com/javase/8/javafx/api/javafx/scene/web/WebView.html).
(It's actually a bit of a hassle to get the HyperlinkListeners into the WebView but I'll come back to that in another post.) The WebView already does some of the things above, so this is the updated list of responses the new listener has to trigger:

1. logging the event
2. ~~changing the cursor~~
3. ~~updating the pane with new content~~
4. opening an external browser
5. opening an external application
6. handling an internal service request

And right here the whole system of classes becomes useless.
(At least as I'm not willing to let the listener do 2.
and 3.
to some invisible control.) At this point, it becomes very clear that responsibilities got mixed up.
I still need some of those but not all and as they are not separated by class boundaries, I'm in an all-or-nothing situation.

## Decorator Pattern To The Rescue

So while I was thinking how much I'd like to mix and match the existing functionality, it eventually bit me (and much later than it should have): this is exactly what the decorator pattern was made for!

### The Decorator Pattern

As I said, I won't go into a detailed explanation of the pattern but the essential idea is this:

When there is an interface where different implementations can provide different features, let each implementation stand on its own.
But implement them such that, at some point during their work, they hand control over to another instance of the same interface.

If one such implementation calls another and uses that result to compute its own, both get to do their thing but the effects will overlap.
The result of the second instance is still there but somewhat altered by the first.
For that reason, the first is said to *decorate* the second.

This can be carried on with more instances, each decorating the former.
It should be seen as a layered system, where each decorator adds another layer of behavior to the whole.

### In Action

The way was clear now: I refactored the above functionality into different decorators like `LoggingHyperlinkListenerDecorator` and `ServiceRequestHandlingHyperlinkListenerDecorator`.

<contentimage slug="vaskas-complex-decorator" options="narrow bg"></contentimage>

Then I removed the original classes and replaced their uses with the right combinations of decorators.
Finally I got around to my new functionality and picked just the right decorators.
[There is a nice way to do this with Java 8](decorator-pattern-default-methods) but for simplicity's sake let's just use constructors here:

```java
// use a lambda expression to create the initial listener
// which does nothing
HyperlinkListener listener = event -> {};
// these decorators first do their own thing and then call the
// decorated listener (the one handed over during construction);
// in the end, the last added decorator will act first
listener =
	new ExternalApplicationOpeningHyperlinkListenerDecorator(listener);
listener =
	new BrowserOpeningHyperlinkListenerDecorator(listener);
listener =
	new ServiceRequestHandlingHyperlinkListenerDecorator(listener);
listener =
	new LoggingHyperlinkListenerDecorator(listener);
```

Besides the boilerplate, it is pretty obvious what happens here.
First, there will be logging, before we identify service requests and handle them.
Anything else will be opened in a browser if possible; otherwise we hand it to some external application.

<contentimage slug="vaskas-complex-decorated" options="narrow bg"></contentimage>

### The Effect

Right away, you can see the positive effects on the code.
First of all, every class has a single, many times very simple responsibility.
This leads to short, easy to understand classes.
Their names are usually right on spot and tell you exactly what they're doing.
Also, testability goes up as there are fewer things going on in each unit.

Additionally, the place where the decorators are put together is much more intention revealing.
You don't have to check the instantiated `ServiceRequestHandlingHyperlinkListener` and its superclasses to find out what exactly the listener does.
Instead you just look at the list of decorations and see what's going to happen.

And last but not least, it made the code ready for future change.
It is now obvious how new listener features are to be implemented.
With the inheriting classes you had to wonder where to put new functionality and how it would effect existing uses of the class.
Now you just implement the umpteenth decorator and add it where needed.

## Reflection

This real life example showed how the application of the decorator pattern made the code easier to read, test and change.

This is of course no automatism; the pattern should only be used where it really does make the code cleaner.
But in oder to decide that, you have to know it and have to be able to reason about its effects.
I hope this post helps with that.
