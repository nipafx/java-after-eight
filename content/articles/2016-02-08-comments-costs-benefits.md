---
title: "Costs And Benefits Of Comments"
tags: [clean-comments, documentation]
date: 2016-02-08
slug: comments-costs-benefits
description: "As with most things in software development the ultimate currency for comments is time. This is an analysis of the costs and benefits of comments."
searchKeywords: "comments costs benefits"
featuredImage: costs-benefits-comments
---

<!-- START WITH THIS POST -->

As with most things in software development the ultimate currency for comments is time.
How much do we have to invest and how much do they save us?
Or in other words:

**What are the costs and benefits of comments?**

## Costs

### Initial Composition

Obviously for a comment to be helpful in the future it has to be written at some point.
It is also clear that the sooner the comment is written, the faster that goes and the more useful information will be included as they are fresh in the author's mind.

The complexity (and thus cost) of writing a meaningful comment correlates with the complexity hidden in the commented code and the quality of the resulting text.
If something very simple happens (like getting or setting a field) the comments will be easy to write (and likely less useful but we will discuss this further below).
For more complex code, commenting will be more involved.
The correlation is capped, though, if the code provides a well-designed abstraction.

<pullquote>The cost correlates with the code's complexity and the comment's quality.</pullquote>

<!-- https://twitter.com/mbostock/status/681561150127878144pw -->

In my experience, compared to other costs for comments but also for designing and writing the commented code and its tests, the time required to initially compose a comment is almost negligible.
Right after spending some time on designing, testing, implementing, and refactoring a piece of code it usually takes me only a minute or so to add a comprehensive comment.

### Maintenance

When code changes, comments will incur one of three costs:

-   updating them will cost some time
-   leaving them unchanged (and thus faulty) will cause confusion at some point in the future
-   deleting them will incur the opportunity cost of missing useful comments (that is, if they were useful in the first place)

The act of updating individual comments usually requires even less time than initial composition.
Unless the frequency of comments is fairly high (think line-by-line narrations) the real cost is the effort needed to *find* all relevant spots to update.
This can become a time consuming, error-prone, and pesky task if locality of comments is not upheld.
This cost is of course incurred every time code changes so maintaining comments widens the cost gap between stable and unstable code.

Without proper maintenance, code and comments quickly diverge, which drastically reduces the benefits of any kind of documentation.
So any commenting schema must address maintenance as this is what everything hinges on!

<pullquote>Any commenting schema must address maintenance!</pullquote>

Not all kinds of comments require the same diligence, though.
[Narrations](taxonomy-comments#narrations) and [contract comments](taxonomy-comments#contracts) must be very up-to-date.
If you fail at maintaining them (regardless of their frequency and initial quality), you are better off just deleting them and be done with it.
Comments providing [technical context](taxonomy-comments#technical-context) require less diligence and [historical comments](taxonomy-comments#historical-context) even less since it's usually "keep it or delete it".

### Confusion

If comments are not maintained, they will likely cause confusion at some point in the future.
But the same can happen if they are of poor quality, e.g. because they are ambiguous or lack details.

Confusion will incur unpredictable but potentially enormous costs when code is developed based on false assumptions.
It also reduces the benefits of other comments by instilling doubt and is generally seen as failure.

<pullquote>Confusion will incur unpredictable but potentially enormous costs.</pullquote>

The highest potential for confusion comes from faulty contracts because they are usually read *instead* of the code.
If narrations and code diverge, it can take some time before figuring this out but in that case, the code is always right.
It is hence common to simply ignore narrations from the outset.
If context comments are recognizable as such, their potential for confusion is limited.

The extent of the problem also depends on the quality of comments, especially locality, and the rigor with which they are maintained.
Due diligence will minimize costs but I'd be surprised if it can be entirely prevented.

It is interesting to note that good development techniques, especially testing, will reduce the cost of confusion as they help to quickly identify the dissonance between claimed and actual behavior.

### Obstruction

Comments require screen space, which is hence not available to show code.
Modern IDEs minimize this problem by allowing to initially collapse block comments.
Usually, API-docs can instead be viewed in on-demand pop-overs or always present second-screen views.

## Benefits

Comments have various benefits but they generally suffer from [diminishing returns](https://en.wikipedia.org/wiki/Diminishing_returns): A few judiciously placed comments can help a lot but discussing every possible angle in detail is, regardless of the associated costs, not linearly more helpful.

### Explaining What Happens

Especially narrations and, to a limited extend, contract comments explain what the code does.
This is of course intrinsically redundant because the code contains the same information, albeit in less readable form if written poorly.

Relying on comments instead of the code itself is of course risky (see [Confusion](#confusion) above) and clean coding techniques strive to make it unnecessary by making the code expressive enough.
This might be hard to do if very unusual language features are used or code is highly optimized, in which case narrations can still add value.

### Keeping Abstractions Intact

Every single unit of code (from methods/functions to classes, packages, modules, libraries ...) should provide an abstraction.
It should do one thing and do it well.
And it should keep the client in the dark about how exactly it does it.
Ideally, it does not require to look past the abstraction.
This is the core of modularizing the solution to any non-trivial problem.

The value of an abstraction is twofold: It prevents a developer from duplicating the functionality *and* from requiring her to fully understand the abstracted problem.

<pullquote>Abstraction is the core of modularizing a solution.</pullquote>

#### Utilizing Existing Code

The first benefit can be lost entirely due to lacking dissemination of knowledge.
A unit might not be discoverable or not be recognized as solving the problem at hand, which will lead to functionality being reimplemented.

A good distribution of knowledge and collaborative work processes (like pair programming) will go a long way in preventing this but comments can play an important role as well.
Documenting a large code unit's (e.g. a package's) central abstraction and the service it provides makes it much easier to localize existing features.

#### Utilizing Existing Understanding

Any work that is required to comprehend an abstraction gradually diminishes its value.
It is incurred every time a developer has to put in effort to understand how the unit is supposed to be used.
This process can of course not be entirely prevented but good [contract comments](taxonomy-comments#contracts) are a potent mechanism in reducing the required time, thus considerably improving the benefit of an abstraction.

Contract comments allow the developer to stay in the context in which she encountered the unit.
Besides expressive naming, no other mechanism has that feature!
When reading the unit's code or tests, the developer has to build an entirely new context, getting to know the subsystem's internals instead of its public surface.
If the unit uses other equally uncommented ones, this can quickly degenerate into a matryoshka doll situation.
In an industry so hell-bent on staying focused, in context, and in flow, this is a considerable downside.

In other words: When clean code and great tests shine, a developer already stepped into the abstraction, thus loosing some of its benefits.

### Top Down Vs Bottom Up

The point above focused on understanding individual units but the same is true when building a mental model of a larger (sub-) system.
In my observation most people are better at understanding from the top down than from the bottom up.

Depending on how far above ground the top is, other kinds of documentation might have to take the lead, e.g. architecture diagrams.
But on the way from the top down contract and context comments can be valuable signposts, keeping the developer on the intended level of abstraction.

<pullquote>Contract and context comments are valuable signposts.</pullquote>

As before: Clean code and tests are great but expecting them to consistently guide the developer through understanding the system by themselves is, in my experience, utterly optimistic because they are forcing a bottom-up approach.

### Documenting Intent

[Technical](taxonomy-comments#technical-context) and [historical context](taxonomy-comments#historical-context) is invaluable when non-trivial code has to be understood, assessed, or changed.

Context can be provided by external documents, issue trackers, code review tools, or version control but each contains only partial information.
Consolidating them can be error-prone and may require considerable effort as several wiki articles, ticket descriptions, comment threads, code reviews, or commits may be relevant for a unit of code.
Unfortunately the results are transient and another developer will have to redo all the work to understand the same unit.

The major advantage of comments is that they are readily available in the source code.
While they can not even come close to covering all the information mentioned above, they can be the second step on the journey to understanding (after the code itself of course).
Context comments will reduce the amount of detective work and thus provide a benefit each time a developer tries to understand that unit of code.

## Conclusions

Let's start with a conclusion that confirms what we already know: [Narrations](taxonomy-comments#narrations) suck!

<pullquote>Narrations suck!</pullquote>

They're easy to write but maintenance is expensive, the risk of confusion is high and obstruction is real as it adds a lot of noise on a line-by-line level.
The only benefit is explaining to developers what the code does, which is exactly what clean code does at least as well in the vast majority of cases.
So they come with high costs and almost no benefits.

Judging [contract comments](taxonomy-comments#contracts) is more nuanced.
If worded properly and used on clean abstractions, they can have substantial benefits by preventing developers from creating a new mental context for the code they are investigating (which clean code generally requires).
But maintenance cost and the potential for confusion weighs heavily if the code is changed frequently.

<pullquote>The larger the intended reuse, the more the scale shifts towards contracts.</pullquote>

So code use and stability should be the guiding stars to how many contracts are put into writing and in what level of detail.
The larger the intended audience, the more the scale shifts towards documentation: The code will be used frequently and there is a strong incentive to keep it stable.
Furthermore, good documentation increases discoverability and adoption.

But even code that has no potential for reuse will be changed and contracts can help facilitate the required understanding.
In this case a high level description of the abstraction (like a paragraph explaining a class' or package's central abstraction) goes a long way.
It still requires diligence during changes but the required effort is minimal.

Context comments, [technical](taxonomy-comments#technical-context) and [historical](taxonomy-comments#historical-context), a are a clear winner.
If worded or formatted in a way that stresses their transient nature, they have almost no costs (maintenance or confusion) but can serve as valuable [bread crumbs](https://en.wikipedia.org/wiki/Hansel_and_Gretel) during bug hunts and refactorings.

<pullquote>Context comments are a clear winner.</pullquote>

## Reflection

Comments have to be composed and maintained and will cause confusion if the latter does not happen properly.
They might be perceived as noisy, which can be considerably reduced by IDE features.
On the plus side, they can help developers understand the code (duh!) by narrating what happens, keeping abstractions intact, and enabling a top-down approach to investigating code.
They are invaluable for documenting intentions.

Comparing the different kinds of comments we have seen that narrations fare badly, which was no surprise, due to adding almost no benefits but requiring high maintenance.
Contracts should be seriously considered but the level of detail should be chosen in relation to the intended reuse.
Last but not least, deliberate context comments are a keeper.
