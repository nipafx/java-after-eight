---
title: "Code Reviews At Disy - Observations"
# subtitle
tags: [code-review, techniques]
date: 2016-10-26
slug: code-reviews-disy-part-3
canonicalUrl: https://blog.disy.net/code-reviews-iii/
description: "After reviewing almost all code we wrote for 18 months, completing some 1'500 reviews, we want to share some recommendations."
intro: "After reviewing almost all code we wrote for 18 months, completing some 1'500 reviews, we want to share some recommendations and look at things we'd like to change."
featuredImage: disy-code-reviews-iii
---

Now that you know [how we introduced code reviews](code-reviews-disy-part-1) and [how we do them](code-reviews-disy-part-2) we want to make some recommendations.
These are based on our experience of performing about one and a half thousand reviews in 18 months.

We’ll finish this series off by going meta and review our reviews.

## Recommendations

### Keep Reviews Short, Focused, And Do Them Promptly

The nice people at SmartBear did some research and published

[10 tips for effective reviews](https://smartbear.com/learn/code-review/best-practices-for-peer-code-review/).
The first three are:

-   Review fewer than 400 lines of code at a time.
-   Stay under 500 LOC per hour.
-   Do not review for more than 60 minutes at a time.

Without putting too much emphasis on the numbers, our experience absolutely supports this: Reviews should be short and focused!
(Or [they may be doomed](doomed-code-review).) This gets much easier if commits are small and reviews are created regularly and in parallel to the work in progress instead of as a monolithic block at the end.

We want to add another tip:

-   Open,respond, and resolve code reviews as soon as possible.

If the reviewer starts immediately, the author will appreciate the quick feedback and if both keep at it, the review will be a pleasantly fluent exchange.

Outdrawn reviews, on the other hand, can be very annoying.
It is increasingly hard to motivate yourself to come back to an old review, work to understand the code yet another time, and continue old discussions.

### Split Refactorings And New Features

A good way to prevent complex, outdrawn reviews is to separate refactorings and new features.

Maybe you have heard of [Martin Fowler’s *Two Hats*](workflows-refactoring#the-two-hats): You wear the one when you refactor code and the other when you add a new feature.
You should usually not be doing both at the same time.

This is absolutely true for code reviews as well!
It is much easier to review a (potentially large) refactoring when the only question is “does the code still do the same as before?” When a review contains refactorings *and* new features each behavior change could either be a failed attempt to refactor the code or part of the new feature.
Understanding in which case you are can be tough—and is absolutely unnecessary!

Consciously switching hats as a developer allows you to create new commits and a new review for each “switch of the hat”, thus making your reviewers’ job easier.

### Go Easy With The Checklist

In the beginning we spent some time creating an extensive check list of all the things a reviewer should look for.
We consulted different sources, collected all recommendations, and created our own curated list.
It has about a hundred items covering many different aspects:

-   General (Does the code solve the problem? No more ToDos?)
-   Technical (Proper exception handling? Proper null handling?)
-   Quality (Do names reveal intentions? No magic numbers?)
-   Tests (Is the code testable? Are tests readable, too?)

This seemed super-important but let’s [face the truth](http://www.commitstrip.com/en/2016/07/06/facing-the-truth/): Nobody ever looks at it.
We go through it once or maybe twice and then it justs sits there and rots.
We’ve internalized most of the items anyway but struggle with broadening that internal list.

If we were to do it again, we might follow [a different route](http://www.daedtech.com/creating-code-review-checklist/).

#### Automation Over Review

Some of the things we wanted every reviewer to check were automated by [SonarQube](http://www.sonarqube.org/) soon after they made it onto the checklist.
If the ecosystem you work in has good tooling (and Java most definitely has), then this should be the way to go.
After automation, many review lists can be shortened considerably.

We’re reviewing “after the fact”, i.e.
the code is already in trunk before the review starts.
If you are doing it the other way around, you could even make Sonar’s thumbs-up a prerequisite for the merge.

#### Personal Checklist

To broaden that internal checklist, you can create a much shorter personal one.
Actually, create two.

The first should contain checks that you think are worth performing but have not yet internalized.
Maybe your team just added a new rule to its clean code guide, maybe you just read about an antipattern that you’re sure you’ve seen in your team’s code base—in such cases it can be hard to remember to look for these details.

So put them on a small list—no more than a handful of items.
Then make sure to go through it *before each review* so that you have them in mind and can actively look for them.
Once you’ve internalized one of them, cross it off and make room for a new one.

The other should contain the things other people tend to point out when reviewing *your* code.
If you spot recurring patterns (maybe your variable names tend to be cryptic or test coverage is often sketchy), put them on the list.
Keep it in mind when coding and go through it when you’re creating a review.
Being your own first reviewer and consistently checking known weaknesses will quickly improve your skills.

### Give Reviewers Context

Unless the review consists of three files or less, the reviewer will be happy to find some pointers.
Always include links to the issue, documented team decisions, or any other documentation that might pertain to the changes.
If applicable reference other reviews.

But the most important part is to not simply dump all that as a list of links.
Instead write a paragraph or two that guides the reviewer through those information.
Finish with a section that outlines the code changes and gives pointers towards where to start.

First of all, this obviously helps the reviewer understand the context in which the changes were made.
Instead of trying to cobble together the intent from a hundred diff lines spread across a dozen files and *then* decide on whether they solve the problem she can go the other way around.

But there’s another, more subtle advantage.
Formulating the path from problem to solution and through the proposed changes forces the author to revisit his decisions.
More than once, this new perspective led to changes - often small, sometimes large - improving quality before the review even started.

### Have A Code Of Conduct

At Disy we have a very collaborative and supportive development culture.
While we were not expecting that code reviews would change that, we were aware that this was still a possibility.
After all, inviting developers to criticize each other’s code surely sounds like something that might lead to trouble.

To prevent conflicts we wanted to agree on some ground rules.
And if push came to shove, we wanted to be prepared and not be forced to make up rules on the fly.
So we decided to define a code of conduct.

Discussing it in detail could easily fill another post, so I’ll be brief here and just mention the most important aspects.

#### Formulate Goals

Formulate or reference your teams’ goals for reviewing code.
This defines the direction the team wants to take and makes it easier to see whether behavior and rules stay on course.

Some of the things we put down:

-   Reviews are a cooperative game, not an antagonistic one.
-   Reviews are a great way to learn but not a good tool to teach a topic.
If necessary, have an offline teaching sessions and come back later.
-   Reviews are not there to find the best solution.
A good solution that fulfills all requirements and code standards suffices.

#### Appropriate Behavior

Define appropriate behavior.
Some things we thought were important:

-   It’s about the code, not about the author or the reviewer.
Keep this in mind for every single comment that is read or written.
-   The reviewer should point out problems rather than come up with solutions.
-   Personal preferences can be recommended but must not be pressed home.
Only insist on agreed standards.

#### Inappropriate Behavior

Make very clear what’s not ok.
Again, some of our bullets:

-   The reviewer must never be condescending!
-   The author should not take criticism of the code personal.
-   A code review is not the place to contest the team’s coding style.
-   No bike shedding, no flame wars!

#### Disagreements

Disagreements are absolutely ok.
They are part of a team’s evolution and there is no reason to eschew them.
But it is important that they do not escalate to conflicts!

To reduce that risk we decided to put some guidelines into place.
Some of them are:

-   If neither author nor reviewer budges, quickly stop arguing about it in the review.
-   Maybe get together and discuss it in person.
-   Look for a tie breaker, preferably an expert on what is being discussed or an architect / team lead.
-   Don’t pull rank!
This makes people furious (usually on the inside).

This resolves technical issues.
For interpersonal issues we made a lead with no skin in the coding game a confidant.
We can address him when we’re unhappy with how a conflict was resolved and thus hope to prevent ongoing quarrels or residual discontent.

We’re happy to say that we never needed him so far.

## Meta

We’ve been reviewing almost all code that gets written for 18 months now and it’s time to take stock.

### What We Like

We had [a prioritized list of goals](code-reviews-disy-part-1#goals):

1. Distribution of knowledge
2. Improving code quality
3. Finding bugs

We feel like we reached all of them and SonarQube and JIRA tend to agree.

But what we as developers like most is that there are way more discussions about code.
How to write it, how to make it clean, which language or library features are a good fit for this or that problem.
We are discussing this much more intensely now and good techniques and patterns make the round much more quickly.

We are convinced that we became better coders because of reviews!

### What Could Be Better

There’s one thing that keeps popping up, though.
Reviews are usually created quite late in an issue’s development phase.
This tends to make reviews larger but even if the author thought about splitting the changes into several reviews, another downside remains.

It is often too late to ask hard questions about the design.
When most of the budget is spent and code, tests, and documentation are ready to be shipped, it is tough to argue for a better design that would void much of that work.

Including the code buddy earlier, maybe by opening intermediary reviews, might address this.
I’m sure we’ll try that soon.

### Reviewing Reviews

We’ve put a lot of energy into getting this right.
Particularly because we felt that failing on the wrong accounts - for example when team members were to start fighting - would burn this topic for a long time.
We already have a couple of feedback loops in place (one-on-ones, weekly team meeting) but we felt that launching code reviews demanded more focused attention.

So the group of people who worked this all out decided to keep meeting every other week for a while.
But everything went quite well so there was nothing much to discuss.

By now, code reviews are just another thing we do.
It did indeed become part of our DNA and as such no longer requires special oversight.

## Summary

And with this ends the three-part series about how we do code reviews at Disy:

-   Part I: [Where We Were and What We Wanted](code-reviews-disy-part-1)
-   Part II: [How We Review](code-reviews-disy-part-2)
-   Part III: [Observations](code-reviews-disy-part-3)

No matter whether you wonder how to introduce reviews, would like to define your team’s workflow, or are looking for ways to improve your process, I’m sure you’ve found something to help you along.
