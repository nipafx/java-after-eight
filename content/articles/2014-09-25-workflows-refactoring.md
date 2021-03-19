---
title: "Impulse: \"Workflows of Refactoring\""
tags: [impulse, techniques]
date: 2014-09-25
slug: workflows-refactoring
description: "Discussing the keynote \"Workflows of Refactoring\" by Martin Fowler at OOP 2014, where he categorizes different reasons for and ways of refactoring."
searchKeywords: "Refactoring"
featuredImage: workflows-of-refactoring
---

In February [Martin Fowler held the keynote at OOP 2014](http://www.oop-konferenz.de/nc/oop2014/oop2014-eng/conference/english-sessions/conference-detail/software-design-in-the-21st-century.html).
Under the resourceful title _Software Design in the 21st Century_ he gave a two-part talk.
The first part is called _Workflows of Refactoring_ and is the first installment of my [Impulse](tag:impulse) series.

In this loose collection of posts I recommend to watch a video, read a blog post, listen to a podcast, get a book, tweet, ... or use whatever other technology is available to waste some time on studying software development.
Besides the prominent embedded media or link, each article will usually consist of a short summary.

## The Talk

Enjoy:

<contentvideo slug="workflows-refactoring"></contentvideo>

You can find [the slides on Martin Fowler's page](http://martinfowler.com/articles/workflowsOfRefactoring/).

## The Gist

Fowler describes the refactoring mindset, presents the metaphor of the two hats and tries to capture and categorize the different reasons to refactor.
In the end he shortly explains why and how it is best justified.

### The Refactoring Mindset

Fowler emphasizes that refactoring should only in the rare case be a specially planned activity.
Instead he describes the *refactoring mindset* as follows:

> When you see something yucky, you should go in and fix it.

and

> I see a mess, I immediately have got to do something to make it cleaner.

and finally

> It is really important that you treat refactoring as an integral part of your regular work.

So keeping the code clean by constantly improving its internal structure is a continually ongoing activity.
It keeps the process simple whereas planned refactoring usually leads to big discussions without much getting done.

### The Two Hats

Fowler points out that it is important not to mix the work modes of *Adding Function* and *Refactoring*.
(He mentions other modes but for the sake of brevity does not consider them in the rest of his talk.) The modes have a different goal.
The first changes the behavior of the system by fixing a bug or implementing a feature.
The second wants to preserve the behavior and only change the internal workings.
He recommends not to do both at once.

The rhythms are different, too.
If you're doing TDD, adding function has the goal to make a test pass which didn't before.
You usually run that test when you think your implementation passes it.
Refactoring has no special test associated with it.
You just want to keep them all passing and preferably check that after each change, no matter how minor.

> You must always be concious to what mode you're in.

To help with the distinction, he presents Kent Beck's metaphor of having two different hats.
One for adding function (he uses a construction worker hat) and another for refactoring (is it a fedora?).

You can only ever wear one but can frequently switch between them.
This has to be a deliberate decision, though.
First, you should quickly decide [whether this is the right moment](https://twitter.com/codeclimate/status/421389300547465216) to refactor.
If it is, make sure to have all of your tests pass before starting.

### Refactoring Workflows

Fowler presents a categorization of the different reasons to refactor and while they might differ, the actual process is mostly identical.

He also stresses how to achieve quality:

> The key to good refactoring is to remember the essence of it is small steps.

#### TDD

In test-driven development, refactoring occurs as part of [Red, Green, Refactor](http://agileinaflash.blogspot.de/2009/02/red-green-refactor.html).
It cleans the code which was written just a minute before and is an integral part to the whole development process.
It aims at making all the other reasons to refactor occur less often.

#### Litter Pickup

Sometimes code just makes you swear!
If you go into a class or module and "Yuck!" ([or something less friendly](http://www.osnews.com/story/19266/WTFs_m)) is your reaction, you should consider some litter pickup refactoring.
If this is the right time, switch your hats and of you go.

It is not necessary to clean up the whole mess at once.
Like the boyscout rule says:

> Always leave the ~~campground~~ code cleaner than you found it.

This will lead to a higher quality in incremental steps.

#### Comprehension

If it's not "Yuck!" but "What?", you're in a similar situation and the code needs to be reworked.
But in this scenario it might not even be that terrible, only hard to understand.

> Figuring out how something complicated works, that's a good thing in a detective novel, it's a bad thing in code.

So when you finally do comprehend it, move your understanding of the design out of your head and into the code.
(But be quick about it, that moment of clarity is fragile!)

Otherwise it is very similar to litter pickup refactoring and everything said above applies.

#### Preparatory

In this case the code is fine.
Until you want to change it.

If you feel that the code needs a different structure to accommodate your change, do some preparatory refactoring first.
After that you can work in the new feature more easily than if you tried to stuff it someplace it doesn't fit.

Fowler emphasizes that this is one of the few situations where refactoring does not need time to pay off.
The positive effect is immediate and making the change will be faster than without refactoring.

#### Planned

This is when the team sits down and officially puts refactoring on the development plan.
That might be problematic to justify, so it is best to avoid this situation.

> If you have to do this, you're not doing enough of the other.

#### Long Term

Some changes are just too big to be done by one person in a day.
So when more resources are needed, you might want to switch to planned refactoring.

Fowler recommends not to do that.
Instead the team should sit down and decide on a long term goal.
What should the code look like to be considered clean?
Then:

> Orient every work in the affected area along that goal and do little incremental refactorings over the course of weeks, even months.

So remember the refactoring mindset: Small incremental steps will eventually lead you all the way.

### Justification

Constant refactoring improves the code quality and leads to clean code.
It's just the right thing to do for any self respecting professional!

While that might be true, these reasons are also totally worthless.
Fowler stresses that arguments along this line get shot down immediately by managers because there is never enough money/time to waste on such peripheral considerations.

Instead he presents his [design stamina hypothesis](http://www.martinfowler.com/bliki/DesignStaminaHypothesis.html).
It comes down to the observation that having no design (or letting the design erode by not constantly cleaning the code) makes development slower and slower until its speed approaches zero.
Against this natural tendency of increasing entropy, refactoring is an important force.

So he urges us to argument with economics: A constantly refactored and clean code base just lets you deliver new features faster and with less bugs.
What kind of manager wouldn't want that?

His final advice if everything else doesn't work: Do it in secret!
After all, it's part of your professional responsibility to keep your code base clean and hence refactoring is embedded in your regular workflow.
So why even mention it?

## Reflection

Besides the good arguments in favor of refactoring (professional and economic ones) the most important thing to take away is the refactoring mindset.
Always leave the code cleaner than you found it!

And when you find yourself in a situation where you consider refactoring, make it a deliberate decision:

* Consider the impact on your current work and decide whether this is [the right time to refactor](http://blog.codeclimate.com/blog/2014/01/09/when-is-it-time-to-refactor/).
* Remember the two hats and consciously switch between the two.
* Reflect on the reason to refactor to make your goal clearer.

Any ideas on refactoring?
Do you have a story to share?
