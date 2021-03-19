---
title: "SPJCN IV: Quo Vadis Scala"
tags: [community, scala]
date: 2017-01-26
slug: spjcn-quo-vadis-scala
canonicalUrl: https://medium.com/sitepoint/quo-vadis-scala-60d86d10019a#.rw1qzhb80
description: "In the fourth issue of SitePoint’s Java Channel Newsletter (from October 21st 2016) I summarize the discussion of Scala's presumable demise."
featuredImage: quo-vadis-scala
---

Moshe Kranc recently published [The Rise and Fall of Scala](https://dzone.com/articles/the-rise-and-fall-of-scala), an article in which he discusses the (perceived?) decline of Scala and the (imagined?) reasons behind it.
This caused some ripples and, of course, a little flame war but discussions were mostly civil and worth looking into.

Now, why am I writing about this?
I never coded a single line of Scala, so what do I know?
Well, I read things and talk to people and, hey, this is my editorial, so I can write about whatever I want!
And with Scala being the major "alternative JVM language", Java developers should have a rough idea of what's going on next door.

## The Rise And Fall Of Scala?

So what's all the fuss about and what can we make of it?

### What Is It About?

To summarize Moshe's line of thought:

-   Scala's TIOBE rank dropped from 13 to 32 and the language is only used by 0.6% of the programmer community.
Even Lightbend, Scala's parent company, is now releasing new frameworks with Java APIs first.
Moshe heard of projects moving away from Scala.
-   He goes over some history of FP, OOP, Scala, and Java, which comes down to "Scala started out with great features but Java 8 largely caught up." He goes on to mention some downsides of working with Scala.
-   He sees two niches for Scala to thrive in: Big Data and DSLs.

Here's his summary:

> Scala played a key role as a catalyst in popularizing functional programming, and it exerted a strong influence on the design of functional programming in Java.
> Scala will probably never become the next big programming language.
> But, it will be around for years to come as the language of choice for niche problem domains such as Big Data programming.

As you can imagine, this ruffled some feathers and not all responses were cordial.
Take [this anonymous reply](https://gist.github.com/anonymous/5df1f1f6c6b6ebbb4dc67b2bc4da4eae), which does make some good arguments but wraps them in personal attacks and a pinch of conspiracy theory.
For heaven's sake, it even contains the "it's open source, so stop whining and fix it" trope!
The arguments, some of them repeated and fleshed out in the Reddit threads [in /r/scala](https://m.reddit.com/r/scala/comments/565ncn/the_rise_and_fall_of_scala_dzone_java/) and [in /r/java](https://www.reddit.com/r/java/comments/5650fy/the_rise_and_fall_of_scala/), are worth looking into, though.

Most notably they discount Moshe's entire premise.
The TIOBE index [apparently](https://gist.github.com/anonymous/5df1f1f6c6b6ebbb4dc67b2bc4da4eae#gistcomment-1891917) never was at 13 and instead seems to be climbing slowly but steadily.(As a side note, people also doubt that the TIOBE index is even useful for judging language popularity, something I tend to agree with.) Yes, Lightbend released [Lagom](https://www.lightbend.com/lagom) with a Java API first but since the project is mainly targeting Java EE developers, that kinda makes sense.
And how did he even come up with the 0.6% and what are these projects he talks about?

People also pointed out a number of misunderstandings about functional programming in general and Scala in particular, the most obvious being his final nail in Scala's coffin that "Java has surpassed Scala as the preeminent functional programming language".

Interestingly enough, though, few disagreed with his final words...

### What To Make Of It?

First of all I agree that Moshe's line of thought is very weak.
The numbers don't add up and imply little but I found the notion of Java as a functional programming language (and even the preeminent one at that) particularly ridiculous.

#### Technical

But I think there are a few more interesting conclusions we can derive from these discussions.
First of all they contain a lot of technical pro and contra about Scala, like its richer type system, case classes, and pattern matching but also long compile times (and whether they're worth it), too easy mutability, and the fact that there are often too many ways to do things.
I can imagine drawing from these information when learning Scala and trying to decide whether to use it on some project or other.

#### Cultural

But the back and forth also reflects on and gives insight into Scala's cultural influence.
While obviously functional it also allows for object oriented programming (you know, being a multi-paradigm language and all) and adds some nice features here as well.
This seduced a lot of Java developers to give it a try as "Java-but-better" (it surely attracts me).
Devs caught in its net learned about FP, either by accident or out of interest, either on their own or from the FP-aficionados on their team who were happy to finally work with a functional programming language on the JVM.
And thus many developers moved from OOP to FP while maintaining an acceptable level of productivity, something that can not be said about, say, moving from Java to Haskell.

This made it a valid choice for companies to invest in Scala knowledge because they could expect their Java team to start churning out functionality from the afternoon of day 1.
As a consequence the community of functional programmers on the JVM exploded, which in turn influenced new languages, including Kotlin, the most recent rising star, and eventually Java itself.
It sounds like a safe bet that Java 8 would've looked very different without other JVM languages exerting functional pressure on the incumbent.
Whatever else happens to Scala, this is an important feat and a huge boon to the entire JVM ecosystem!

As an aside, it is refreshing to find very little of the elitism the FP community is often accused of in these threads.
It is very clear that some functional programmers consider Java a simple language for simple people, something I have once even heard a speaker at a Java conference utter and defend (behind closed doors), but this opinion does not really show up here, which is nice.
Some commentators mention,that they did encounter it when first learning FP, though.

#### Paradigmal

Personally, and I am sure I read this somewhere but can't find the article anymore, I think that a multi-paradigm language has a considerable downside as well.
Imagine a project where half the developers use Scala as "Haskell on the JVM" and the other as "Java but better".
Without a good development process with a lot of feedback loops these groups will create vastly different code, making integrating and maintaining their solutions, err, challenging.
Even with a good process, though, a lot of friction ensues until the team eventually (and hopefully) settles on a shared approach.

This directly touches on the critique that Scala allows too many ways to do things and too few of those approaches are accepted as idiomatic ones, to which developers can happily default.
Apparently the Scala community is busy bike shedding and flame waring this out, though...

It is interesting to realize that a similar problem starts haunting Java as well.
Collections over vectors and arrays, NIO over IO, streams over loops, lambda-enabled APIs over those that are not, soon [modules over JARs](jigsaw-hands-on-guide), [value classes](https://www.sitepoint.com/javaone-2016-nucleus/#javase) and [value types](http://cr.openjdk.java.net/~jrose/values/values-0.html) over verbose [value-based classes](java-value-based-classes), and maybe at some point [reactive APIs](http://openjdk.java.net/jeps/266) over those that are not.
We better get busy coming up with a good way to settle on and communicate idiomatic approaches if we do not want to get into the same situation.

### So Where *Is* Scala Going?

To come back to a remark I made above, nobody really disagreed with Moshe's final words.
Scala is a nice language, it is important to the ecosystem, it has its use cases, and it is here to stay.

Whether just in a few niches or as "the next big programming language" is totally secondary, though.
And I want to add that it would suit us not to view everything through the warped Silicon Valley lens of "the next big thing".
Unless you're an investor, it is not really worth coming to a final conclusion on this.
It is a nice topic to discuss among peers but no reason to spit vitriol.

If you like Scala (or any other language for that matter), that's awesome!
Learn it or stick with it, but most importantly enjoy it!
Because people are at their best when they do something they enjoy.

## What Else Is Going On?

Nothing that I'm willing to make this newsletter even longer for.
;)

## Wrapping Things Up

Let me leave you with a couple of articles I think you might find interesting.

On SitePoint:

-   [Building a Web App with Java Servlet](https://www.sitepoint.com/tutorial-building-web-app-with-java-servlets/) by Alejandro Gervasio
-   [Getting Started with Dropwizard](https://www.sitepoint.com/tutorial-getting-started-dropwizard/) by Indrek Ots
-   [JUnit 5 State Of The Union](https://www.sitepoint.com/junit-5-state-of-the-union/) by me

Other:

-   [Java 9, OSGi and the Future of Modularity (Part 2)](https://www.infoq.com/articles/java9-osgi-future-modularity-part-2)
-   [Java 8 Lambdas - A Peek Under the Hood](https://www.infoq.com/articles/Java-8-Lambdas-A-Peek-Under-the-Hood)
-   [What I wish I knew when I started as a software developer](https://codurance.com/2016/10/03/what-i-wish-i-knew-earlier/)
-   [jOOQ vs.
Hibernate: When to Choose Which](https://blog.jooq.org/2015/03/24/jooq-vs-hibernate-when-to-choose-which/)
-   [Latest Java SE 8 update and security fixes](https://www.voxxed.com/blog/2016/10/latest-java-se-8-security-fixes/)

I wish you a great time!

so long ... Nicolai
