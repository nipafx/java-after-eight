---
title: "SPJCN II: What's Taking So Long?"
tags: [java-9, project-jigsaw]
date: 2016-12-07
slug: spjcn-whats-taking-long
canonicalText: "I originally wrote this for Sitepoint's Java newsletter, but this issue never got published online."
description: "In the second issue of SitePoint’s Java Channel Newsletter (from September 23rd 2016) I wonder why Java 9 takes so long."
featuredImage: whats-taking-so-long
---

As you might have heard, [Java 9 got delayed](http://mail.openjdk.java.net/pipermail/jdk9-dev/2016-September/004887.html) - [again](delay-of-java-9-release).
The current schedule plans general availability for July 2017.

But why?
What's taking so long?
In one word: Jigsaw.
In more words: Read on...

## The Neverending Jigsaw

(This cultural reference might be a little obscure, so let me drop an [explanatory link](https://en.wikipedia.org/wiki/The_Neverending_Story).)

### A Little History

Wishing for modularity in Java has a long history, dating back at least eleven years to when [JSR 277](https://jcp.org/en/jsr/detail?id=277) was filed in 2005.
Jigsaw itself came a little later - [Mark Reinhold announced it on his blog](http://mreinhold.org/blog/jigsaw) in December 2008.
Since then it underwent a series of technical and organizational challenges and reshuffles before going back on track in 2011 when it got fully staffed and started an exploratory phase, which ended three years later.

In that time a lot happened and it is easy to ignore just how much work had to be done!
There are three Java Enhancement Proposals (JEPs) just to [envision how to cut the JDK](http://openjdk.java.net/jeps/200), to [reorganize the (massive) code base](http://openjdk.java.net/jeps/201) accordingly, and to [restructure the run-time image](http://openjdk.java.net/jeps/220).
And none of that even touched on the design, let alone implementation, of the module system itself.

### Bones of Contention

Because that's the fly in the ointment - how exactly should the module system behave?

Let's take *strong encapsulation* as an example.
This is one of Jigsaw's two major goals, a way for module developers to keep users from breaking into the module's internals and depending on implementation details, thus deteriorating maintainability and hindering evolution.
But it's not that easy.
To really encapsulate internals, reflection must also be stopped at module boundaries.
And suddenly [everybody is up in arms](http://blog.dripstat.com/removal-of-sun-misc-unsafe-a-disaster-in-the-making/) because now many workarounds and performance-critical hacks break down.

The other main goal is *reliable dependencies*, which allows Java to do some sanity checks during compilation and launch to verify that the setup makes sense:

-   Are all dependencies present?
-   Are there no statically defined dependency cycles?
-   Are there no ambiguous situations, e.g. because of two versions of the same artifact?

Again, this sounds nice.
But then someone comes along and starts talking about dependencies that are only required at compile time but optional at runtime.
This brings us back to square one, where the JVM could not verify whether the program seems to have everything it needs to run.

These are just two examples and the list goes on.
And this is not just a figure of speech, there's [an actual list of open issues](openjdk.java.net/projects/jigsaw/spec/issues/)!

Now, I'm sure that not all items on that list will receive the same level of attention as the two I discussed above.
But for these, substantial changes to the existing implementation have been proposed by the Jigsaw team and they are only now making it into the [early access builds](https://jdk9.java.net/jigsaw/).
The project simply needs more time to kick ideas and feedback back and forth between the Oracle developers and us users.
Hence the additional delay.

(If you ask me, I'm not sure whether we will actually download JDK 9.0 in July 2017 or whether it has to be delayed even longer.)

### Want More?

This rabbit hole is deep, though, and a newsletter is not the best medium to explore it.
What about this: I'll write more about it if you're interested.
[Retweet this](https://twitter.com/nipafx/status/779069666199298048) to show your enthusiasm!

## What Else Is Going On?

Again, there is an obvious one-word answer: JavaOne.
The next newsletter will cover it in-depth but you should check [the YouTube channel](https://www.youtube.com/channel/UCdDhYMT2USoLdh4SZIsu_1g/videos) if you can't wait that long.
If there's something you liked, feel free to recommend it to me via [mail](mailto:nicolai.parlog@sitepoint.com) or [Twitter](https://twitter.com/nipafx).

On SitePoint, our Java channel is slowly gaining speed.
We started to write about JPA and Hibernate, giving you [5 reasons to use it](https://www.sitepoint.com/5-reasons-to-use-jpa-hibernate/) (Thorben Janssen), which should be interesting even if you're already working with it, and [a hands-on introduction](https://www.sitepoint.com/hibernate-introduction-persisting-java-objects/) (Alejandro Ugarte).
Pretty soon Vlad Mihaelca will explain schema migration with Hibernate and FlywayDB.
But this is a vast topic and there will be more about it.

If you're into Web development, we'll have you covered as well.
Articles about plain old servlets as well as about hot shit like [Dropwizard](http://www.dropwizard.io) and [Ratpack](http://ratpack.io/) are in the pipeline.

By the way, did you know that you can get an RSS feed for pretty much everything on SitePoint by appending `/feed` to the URL?
For channels ([Java, obviously](https://www.sitepoint.com/java/feed)), tags (since we were talking about it, what about [Hibernate](https://www.sitepoint.com/tag/hibernate/feed)?), and even authors ([yours truly](https://www.sitepoint.com/author/nicolaip/feed)).
Give it a shot!

## Wrapping Things Up

Let me leave you with a couple of articles I think you might find interesting:

-   [Java EE 8 Delayed Until End of 2017, Oracle Announces at JavaOne](https://www.infoq.com/news/2016/09/java-ee-delayed-2017)
-   [Private methods in interfaces in Java 9](http://blog.joda.org/2016/09/private-methods-in-interfaces-in-java-9.html)
-   [TDD for PL/SQL Developement](https://blog.disy.net/tdd-for-plsql-with-junit/)
-   [Advice on removing javac lint warnings](https://blogs.oracle.com/darcy/entry/warnings_removal_advice)
-   [How PayPal Scaled to Billions of Transactions Daily Using Just 8VMs](http://highscalability.com/blog/2016/8/15/how-paypal-scaled-to-billions-of-transactions-daily-using-ju.html)

I wish you a great time!

so long ... Nicolai
