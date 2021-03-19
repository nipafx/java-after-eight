---
title: "Code Reviews At Disy - Where We Were And What We Wanted"
# subtitle
tags: [code-review, techniques]
date: 2016-08-15
slug: code-reviews-disy-part-1
canonicalUrl: https://blog.disy.net/code-reviews/
description: "At Disy we review almost all the code we write. Here, we want to share why that was not always the case and how we started with code reviews."
featuredImage: disy-code-reviews-i
---

At Disy we review almost all the code we write.
Here, we want to share why that was not always the case and how we started with code reviews.

Before I come to the main act, the code reviews themselves, I have to set the stage, though.
To understand our review process it helps to know our team structure, so I’ll start with that.
I’ll then talk about why and how we introduced code reviews, and the goals, assumptions, and principles we decided on.

The next post in this series will go into details and explain how we do it on a daily basis.

## One Team

Disy has a couple of products and we’re doing project work as well.
But by far the biggest and most important code base we’re working on is [Cadenza](http://www.disy.net/en/products/cadenza.html).

While well modularized, many features or fixes cut across various aspects.
Implementing a feature that, for example, allows the creator of a map to specify the color with which selected geometries are highlighted requires changes to Swing, Web, and mobile map components as well as the configuration UI, backend, and persistence.

While we naturally acquire more knowledge in areas we repeatedly work on and might even become local experts, we want to prevent information silos.
Because of that every developer should be able to work on (almost) any issue.
It is then of course possible and even actively encouraged to seek out the help of those local experts.

This perception solidified in 2014.
The individual teams we had before were restructured and the One Team (yes, capitalized!) was formed.
Our numbers grew in the following year and the new hires (me among them) made the team somewhere between 15 and 20 developers strong, depending on how you want to count.

(This puts strain on many practices, particularly agile ones, but we’re disciplined and fare well so far.
Details are interesting but beyond the scope of this article.)

## Introducing Code Reviews

Some of the teams before the One Team used to do code reviews—some on principal and some on demand or when a change called for it.
But it was never in our DNA.
With the restructuring and the influx of new developers, many of which didn’t do reviews before, the practice went under.

Then, in the beginning of 2015, a retrospective revealed the shared interest in code reviews.
Discussions in small and large groups on and off work and some research led to the formulation of our goals, assumptions, and core principles for code reviews.
Note that these were created for this team at this point in time, tailored to our needs.
As so often, your mileage may vary.

It was important for us to write them down so we could ensure we had a shared understanding.
Side benefit: the next part is largely a translation of our Wiki page.
:)

### Goals

There are various goals one might try to achieve with code reviews.
It was important for us to determine which we wanted to pursue and how important each was for us.

This is our prioritized list:

1. Distribution of knowledge
2. Improving code quality
3. Finding bugs

Cadenza is pretty stable and we had / have no irregular amount of bugs.
Hence we decided that there was no reason to put that into focus.

Our code base is not too small and over 15 years old, though, so it needs continuous work if we want it to live another 15.
Especially with many new developers it was important to focus on not slipping in code quality.

Which was also one of the reasons why we put “distribution of knowledge” to the top of the list.
We are convinced that most problems (bad quality, bugs, delays, …) come not from incompetence but from lacking context.
Hence we promote sharing knowledge where possible.
Code reviews seemed like a great instrument for that.

<pullquote>Code reviews are a great instrument for sharing knowledge.</pullquote>

### Assumptions

When creating our code review process we made the following assumptions.

All code can contain bugs or degrade quality.
:   No code is so trivial that a human could not let a mistake slip through.
There are also a lot of non-functional issues that can crop up, like missing tests or erroneous comments.

Every developer can learn.
:   Cadenza is big and the code base includes many technologies, all of them changing constantly, so everybody can always learn.
Teaching also helps the teacher to strengthen their knowledge and gain a new perspective.
We’re sure that whenever two people talk about code, both improve.

Code reviews need resources.
:   There is no free lunch and reviews can not be done here and there.
They require time and attention of every participant.

Code reviews are not without risk.
:   If reviews are arbitrary or inconsistent they deliver unreliable results, which might not justify the invested time.
They also harbor the real risk that the reviewer and the reviewed become attacker and defender, which undermines a constructive collaboration on this and maybe even on future issues.

We have little experience with reviews.
:   While individual team members might have done reviews in smaller teams or at other companies, the team as a whole has no experience with them.

### Principles

Based on our goals and assumptions we came up with a set of core principles.
The review process was then designed so that it matches them.
They are not hard rules, though, and deviation is possible in justified cases.

All code gets reviewed.
:   If all code can contain bugs or degrade quality then all code should be reviewed.
This is in line with our assumptions that everybody learns from each review.
It also addresses our lack of experience because it quickly builds that experience and also removes the question of “should this code be reviewed?”.

Code reviews are peer reviews.
:   If sharing knowledge is more important than finding bugs and even the expert can learn from interacting with the beginner, then there is no reason to make them “expert reviews”, where senior developers review junior ones.
This also prevents those senior developers, often already under a considerable workload, from becoming bottlenecks.

Reviewing code is as important as writing code.
:   Code reviews are not an optional afterthought and can not only be performed if time and budget permit.
They have to be included in estimates, offers, and planning.

Reviews should be structured.
:   Inclusion in our development process, tool support, checklists, a code of conduct; such things can help us achieve a common review style and consistent results.
This is particularly important as the team had little collective experience.

Reviews are fun!
:   Ok, maybe *fun* is not the best word—besides fun can not be prescribed.
But it is important to keep in mind that reviews are a collaborative process to reach a shared goal: better code quality!
To deliver fast but also to keep us sane.
And learning in passing and getting a little positive feedback about our code *can* be fun.

## Performing Code Reviews

[Part II of this miniseries](code-reviews-disy-part-2) will give a detailed insight into how we do reviews on a daily basis.
Stay tuned!
