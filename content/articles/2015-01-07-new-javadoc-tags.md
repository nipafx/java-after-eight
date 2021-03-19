---
title: "New Javadoc Tags `@apiNote`, `@implSpec`, and `@implNote`"
tags: [java-8, tools]
date: 2015-01-07
slug: javadoc-tags-apiNote-implSpec-implNote
description: "There are new Javadoc tags used in Java 8: `@apiNote`, `@implSpec`, and `@implNote`. Take a look at their history, meaning and use on command line and with Maven."
searchKeywords: "javadoc tags"
featuredImage: javadoc-8-tag
repo: javadoc-8-tags
---

If you're already using Java 8, you might have seen some new Javadoc tags: `@apiNote`, `@implSpec`, and `@implNote`.
What's up with them?
And what do you have to do if you want to use them?

This post has a quick view at the tags' origin and current status.
It then explains their meaning and detail how they can be used with IDEs, the Javadoc tool and via Maven's Javadoc plugin.

## Context

### Origin

The new Javadoc tags are a byproduct of [JSR-335](https://www.jcp.org/en/jsr/detail?id=335), which introduced lambda expressions.
They came up in the context of default methods because these required a more standardized and fine grained documentation.

In January 2013 Brian Goetz [gave a motivation and made a proposal](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-January/001211.html) for these new tags.
After a short discussion it turned into a [feature request](http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8008632) three weeks later.
By April the [JDK Javadoc maker was updated](http://cr.openjdk.java.net/~mduigou/JDK-8008632/0/webrev/) and the [mailing list informed](http://mail.openjdk.java.net/pipermail/core-libs-dev/2013-April/016149.html) that they were ready to use.

### Current Status

It is important to note that the new tags are not officially documented (they are missing in the [official list of Javadoc tags](http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html#CHDJGIJB)) and thus subject to change.
Furthermore, the implementer Mike Duigou [wrote](http://mail.openjdk.java.net/pipermail/core-libs-dev/2013-December/023866.html):

> There are no plans to attempt to popularize these particular tags outside of use by JDK documentation.

So while it is surely beneficial to understand their meaning, teams should carefully consider whether using them is worth the risk which comes from relying on undocumented behavior.
Personally, I think so as I deem the considerable investment already made in the JDK as too high to be reversed.
It would also be easy to remove or search/replace their occurrences in a code base if that became necessary.

## `@apiNote`, `@implSpec`, and `@implNote`

Let's cut to the heart of things.
What is the meaning of these new tags?
And where and how are they used?

### Meaning

The new Javadoc tags are explained pretty well in the feature request's description (I changed the layout a little):

> There are lots of things we might want to document about a method in an API.
Historically we've framed them as either being "specification" (e.g., necessary postconditions) or "implementation notes" (e.g., hints that give the user an idea what's going on under the hood.) But really, there are four boxes (and we've been cramming them into two, or really 1.5):
>
>  **{ API, implementation } x { specification, notes }**
>
> (We sometimes use the terms normative/informative to describe the difference between specification/notes.) Here are some descriptions of what belongs in each box.
>
> **1. API specification.**
>
> This is the one we know and love; a description that applies equally to all valid implementations of the method, including preconditions, postconditions, etc.
>
> **2. API notes.**
>
> Commentary, rationale, or examples pertaining to the API.
>
> **3. Implementation specification.**
>
> This is where we say what it means to be a valid default implementation (or an overrideable implementation in a class), such as "throws UOE." Similarly this is where we'd describe what the default for `putIfAbsent` does.
> It is from this box that the would-be-implementer gets enough information to make a sensible decision as to whether or not to override.
>
> **4. Implementation notes.**
>
> Informative notes about the implementation, such as performance characteristics that are specific to the implementation in this class in this JDK in this version, and might change.
> These things are allowed to vary across platforms, vendors and versions.
>
> The proposal: add three new Javadoc tags, `@apiNote`, `@implSpec`, and `@implNote`.
> (The remaining box, API Spec, needs no new tag, since that's how Javadoc is used already.) **@impl{spec,note}** can apply equally well to a concrete method in a class or a default method in an interface.

So the new Javadoc tags are meant to categorize the information given in a comment.
It distinguishes between the specification of the method's, class's, ... behavior (which is relevant for all users of the API - this is the "regular" comment and would be `@apiSpec` if it existed) and other, more ephemeral or less universally useful documentation.
More concretely, an API user can not rely on anything written in `@implSpec` or `@implNote`, because these tags are concerned with **this** implementation of the method, saying nothing about overriding implementations.

This shows that using these tags will mainly benefit API designers.
But even Joe Developer, working on a large project, can be considered a designer in this context as his code is surely consumed and/or changed by his colleagues at some point in the future.
In that case, it helps if the comment clearly describes the different aspects of the API.
E.g. is "runs in linear time" part of the method's specification (and should hence not be degraded) or a detail of the current implementation (so it could be changed).

### Examples

Let's see some examples!
First from the [demo project](https://github.com/nipafx/demo-javadoc-8-tags) to show some rationale behind how to use the tags and then from the JDK to see them in production.

#### The Lottery

The project contains an interface `Lottery` from some fictitious library.
The interface was first included in version 1.0 of the library but a new method has to be added for version 1.1.
To keep backwards compatibility this is a default method but the plan is to make it abstract in version 2.0 (giving customers some time to update their code).

With the new tags the method's documentation clearly distinguishes the meanings of its documentation:

```java
/**
 * Picks the winners from the specified set of players.
 * <p>
 * The returned list defines the order of the winners, where the first
 * prize goes to the player at position 0. The list will not be null but
 * can be empty.
 *
 * @apiNote This method was added after the interface was released in
 *          version 1.0. It is defined as a default method for compatibility
 *          reasons. From version 2.0 on, the method will be abstract and
 *          all implementations of this interface have to provide their own
 *          implementation of the method.
 * @implSpec The default implementation will consider each player a winner
 *           and return them in an unspecified order.
 * @implNote This implementation has linear runtime and does not filter out
 *           null players.
 * @param players
 *            the players from which the winners will be selected
 * @return the (ordered) list of the players who won; the list will not
 *         contain duplicates
 * @since 1.1
 */
default List<String> pickWinners(Set<String> players) {
	return new ArrayList<>(players);
}
```

#### JDK

The JDK widely uses the new tags.
Some examples:

-   [`ConcurrentMap`](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentMap.html):
	-   Several `@implSpec`s defining the behavior of the default implementations, e.g. on [`replaceAll`](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentMap.html#replaceAll-java.util.function.BiFunction-).
	-   Interesting `@implNote`s on [`getOrDefault`](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentMap.html#getOrDefault-java.lang.Object-V-) and [`forEach`](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentMap.html#forEach-java.util.function.BiConsumer-).
	-   Repeated `@implNote`s on abstract methods which have default implementations in Map documenting that "This implementation intentionally re-abstracts the inappropriate default provided in Map.", e.g. [`replace`](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentMap.html#replace-K-V-).
-   [`Objects`](https://docs.oracle.com/javase/8/docs/api/java/util/Objects.html) uses `@apiNote` to explain why the seemingly useless methods [`isNull`](https://docs.oracle.com/javase/8/docs/api/java/util/Objects.html#isNull-java.lang.Object-) and [`nonNull`](https://docs.oracle.com/javase/8/docs/api/java/util/Objects.html#nonNull-java.lang.Object-) were added.
-   The abstract class [`Clock`](https://docs.oracle.com/javase/8/docs/api/java/time/Clock.html) uses `@implSpec` and `@implNote` in its class comment to distinguish what implementations must beware of and how the existing methods are implemented.

### Inheritance

When an overriding method has no comment or inherits its comment via `{@inheritDoc}`, the new tags are not included.
This is a good thing, since they will not generally apply.
To inherit specific tags, just add the snippet `@tag {@inheritDoc}` to the comment.

The implementing classes in the [demo project](https://github.com/nipafx/demo-javadoc-8-tags) examine the different possibilities.
The README gives an overview.

## Tool Support

### IDEs

You will likely want to see the improved documentation (the JDK's and maybe your own) in your IDE.
So how do the most popular ones currently handle them?

**Eclipse** displays the tags and their content but provides no special rendering, like ordering or prettifying the tag headers.
There is a [feature request](https://bugs.eclipse.org/bugs/show_bug.cgi?id=422073) to resolve this.

**IntellyJ**'s current community edition 14.0.2 displays neither the tags nor their content.
This was apparently solved on Christmas Eve (see [this ticket](https://youtrack.jetbrains.com/issue/IDEA-128304#tab=History "Issue 128304: JavaDoc.
Support additional annotations")) so I guess the next version will not have this problem anymore.
I cannot say anything regarding the rendering, though.

**NetBeans** also shows neither tags nor content and I could find no ticket asking to fix this.

All in all not a pretty picture but understandable considering the fact that this is no official Javadoc feature.

### Generating Javadoc

If you start using those tags in your own code, you will soon realize that generating Javadoc fails because of the unknown tags.
That is easy to fix, you just have to tell it how to handle them.

#### Command Line

This can be done via the [command line argument *-tag*](http://docs.oracle.com/javase/8/docs/technotes/tools/windows/javadoc.html#tag).
The following arguments allow those tags everywhere (i.e.
on packages, types, methods, ...) and give them the headers currently used by the JDK:

```shell
-tag "apiNote:a:API Note:"
-tag "implSpec:a:Implementation Requirements:"
-tag "implNote:a:Implementation Note:"
```

(I read the official documentation as if those arguments should be `-tag apiNote:a:"API Note:"` \[note the quotation marks\] but that doesn't work for me.
If you want to limit the use of the new tags or not include them at all, the documentation of *-tag* tells you how to do that.)

By default all new tags are added to the end of the generated doc, which puts them below, e.g., **@param** and **@return**.
To change this, *all* tags have to be listed in the desired order, so you have to add the known tags to the list *below* the three above:

```shell
-tag "param"
-tag "return"
-tag "throws"
-tag "since"
-tag "version"
-tag "serialData"
-tag "see"
```

#### Maven

[Maven's Javadoc plugin](http://maven.apache.org/plugins/maven-javadoc-plugin/) has a [configuration setting *tag*](http://maven.apache.org/plugins/maven-javadoc-plugin/javadoc-mojo.html#tags) which is used to verbosely create the same command line arguments.
The demo project on GitHub shows [how this looks like in the *pom*](https://github.com/nipafx/demo-javadoc-8-tags/blob/master/pom.xml?ts=4#L110-L133).

## Reflection

We have seen that the new Javadoc tags `@apiNote`, `@implSpec`, and `@implNote` were added to allow the division of documentation into parts with different semantics.
Understanding them is helpful to every Java developer.
API designers might chose to employ them in their own code but must keep in mind that they are still undocumented and thus subject to change.

We finally took a look at some of the involved tools and saw that IDE support needs to improve but the Javadoc tool and the Maven plugin can be parameterized to make full use of them.
