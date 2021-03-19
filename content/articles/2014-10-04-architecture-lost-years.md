---
title: "Impulse: \"Architecture - The Lost Years\""
tags: [architecture, impulse]
date: 2014-10-04
slug: architecture-lost-years
description: "Discussing the talk \"Architecture - The Lost Years\", which Robert C. Martin held on several occasions."
searchKeywords: "Architecture Lost Years"
featuredImage: architecture-lost-years
---

Wow!

There's not much more to say.
The architecture Robert C.
Martin presents in [this talk](http://www.youtube.com/watch?v=WpkDN78P884 "Robert C. Martin at Ruby Midwest 2011") is mind-blowing.
Never mind that it's 20 years old - nobody seems to use it so it counts as new.
(By the way those are the lost years in the title "Architecture - The Lost Years".)

Just go and watch the video!

I'll not even try to properly summarize the talk as I think it should be watched in its entirety.
But just in case you wonder whether you really want to do that, I'll try to make you curious.

## The Talk

([Link to YouTube](http://www.youtube.com/watch?v=WpkDN78P884 "Robert C. Martin at Ruby Midwest 2011"))

Just in case you want to watch it again, use [this link](http://www.youtube.com/watch?v=asLUTiJJqdE "Robert C. Martin at COHAA").
It doesn't have the slides but a fun introduction about lasers.

Unfortunately I couldn't find the slides online.
In case you do, you can link them in the comments.

## Making You Curious

The following summary has a big hole in it where the actual architecture is described.
But the rest is more or less there.

### Folder Structure

Martin starts with showing a standard Ruby on Rails app.
And while the used framework is obvious from the code's top level folders, the actual intent of the program is not.
But shouldn't it be?

> Architecture of an application is all about its intent.

He compares that with building architecture where the ground plan will clearly give away the building's purpose.
For code the ground plan would be the content of the topmost folder, maybe the ones below that.
So, likewise, these folders should clearly show the code's purpose.
And they should do this by listing the use cases so there should be folders like *CreateOrder* and *AddItemToOrder*.

Does your code look like that?
Mine doesn't.

### Details

He also mentions two things which should be an implementation detail in properly architectured systems:

-   the user interface
-   the database

The former not only includes the UI technology but also (and especially) the Web.
That's just a delivery mechanism to get the user in touch with the business logic!
It should in no way be of any importance to the system's architecture.

And the database is also something that dangles off the side of your application.
It's a mere implementation detail.

If all that doesn't make you curious, you're either really good or very unexcitable.

### Use Case Driven Design

So how to achieve all that?
With the [use case driven approach presented by Ivar Jacobson](https://www.amazon.com/dp/0201544350) in 1993.
(There also exists a [slimmed down, updated and free version](http://www.ivarjacobson.com/download.ashx?id=1282) from 2011.)

Martin goes on to explain the concept.

I'm not going to summarize that part.
It is too complex and important to be cramped into a few lines.
I'm currently thinking about writing a small app just to test drive the concept.
If I do, I might make some posts out of my experience, so stay tuned: [⇒ newsletter](news), [⇒ RSS](/feed.xml)

<admonition type="update">

At GeeCON 2014 Sandro Mancuso gave a talk called *Crafted Design*.
It is based on the same observation and problem statement as Martin's talk and comes to a very similar solution.
I covered it in my post [Impulse: "Crafted Design"](crafted-design) and it contains a description of the architecture, which I did not write here.
You should check it out!*

</admonition>

### Effects

Martin mentions two major advantages of such an approach.

#### Good Architecture

If you ban all subsystems which are not part of the central business logic to the outer reaches of your code and abstract them behind interfaces, you can easily substitute them.
This leads to good architecture as Martin defines it:

> A good architecture allows major decisions to be deferred!

> A good architecture maximized the number of decisions not made.

He gives [FitNesse](http://www.fitnesse.org/) as an example.
It was supposed to get a database to persist its content - in fact this was one of the first things the team considered.
But for no special reason they deferred that decision a couple of times.
Eventually, the system was ready and still had no database because, as it turns out, it was not necessary.
And when a customer really needed one, it was plugged in without effort.

#### Testing

Of course no talk by Martin would be complete without him insisting on having a comprehensive and fast test suite.
He quickly outlines why this is so important: If you can't test everything and do it fast, you won't refactor constantly, in which case you're doomed.
(Yes, it's that simple.)

But keeping tests fast is impossible if they typically involve the database or the UI.
Unfortunately, this is the reality for many systems as those subsystems are not sufficiently decoupled to be substituted with mocks during tests.

Jacobson's approach, on the other hand, promotes exactly that kind of decoupling.
If the UI, database, file system, online services and whoknowswhatelse all become plugins, you can mock them easily and have your tests run blazingly fast.

## Reflection

As I didn't cover the main part of the talk, we can't reflect on that.
Instead, I'm going to insist some more:

-   [watch the talk](https://www.youtube.com/watch?v=WpkDN78P884 "Robert C.
Martin at Ruby Midwest 2011")
-   [or this one](https://www.youtube.com/watch?v=asLUTiJJqdE "Robert C.
Martin at COHAA")
-   [or this one](https://www.youtube.com/watch?v=HhNIttd87xs "Robert C.
Martin at Hakka Labs")
-   [or this one](https://www.youtube.com/watch?v=Nltqi7ODZTM "Robert C.
Martin at NDC 2012") (looks like he's been touring)
-   [get the original book by Jacobosn](https://www.amazon.com/dp/0201544350)
-   [get the updated version](http://www.ivarjacobson.com/download.ashx?id=1282)
-   try it out
-   give me your opinion

