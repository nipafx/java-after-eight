---
title: "A Doomed Code Review"
tags: [code-review, techniques]
date: 2016-04-26
slug: doomed-code-review
canonicalUrl: https://blog.disy.net/doomed-code-review
canonicalText: "I wrote this post for [Disy's Tech Blog](https://blog.disy.net/). [Check out the original here](https://blog.disy.net/doomed-code-review) (with less swear words). There is also some interesting stuff about databases and geographical information system, so if you are interested in these topics, you might want to take a look."
description: "Code reviews should be brief, short, and focused. This is the story of how I fucked up on all those accounts and we still made it work."
intro: "There are a couple of things you should do to make code reviews successful. Chief among them, keep them brief, short, and focused. This is the story of how I fucked up on all these accounts and we still made it work."
searchKeywords: "code review"
featuredImage: doomed-code-review
---

There are a couple of things you should do to [make code reviews successful](https://smartbear.com/learn/code-review/best-practices-for-peer-code-review/).
Chief among them:

-   Keep them brief (no longer than 200 to 400 lines)
-   Keep them short (don't review longer than an hour)
-   Keep them focused (small cohesive increments)

This is the story of how I fucked up on all these accounts and we still made it work.

(We, that's me and my code buddy - he's my designated reviewer for this month.)

## Downfall

There I was - the feature was finally done.
And with that realization the thing that had been scraping against the inside of my skull for a while now suddenly clawed its way out: You still need to get a review.
For this code.
All of it.

And for review standards, there was a shitlot of it!
The core contribution consisted of about 2'500 new lines spread out over 30 classes.
On top of that came about 400 diff-lines in 25 existing classes.

Just thinking about dumping all that on my code buddy made me nauseous.
If someone would do that to me, I would have some choice words and maybe a [nicely wrapped bobcat](https://xkcd.com/325/) for them.

I started justifying what I had done: First there were holidays; I wasn't working, and then my buddy wasn't.
On top of that people kept interrupting me and I was never at a good break point to start an intermittent review.
The issue was just too large.
And the code was obviously right!

Bla, bla, bla, the babble of the guilty.
And it changed jack shit: There I was - still with 3000 lines of code I had to get reviewed.

## Out Of The Dark

What is the first thing you do when you drop the ball?
You own up!

So I sat down with my code buddy and explained what happened.
We checked his schedule to make sure he would be able to go through the review in a reasonable time frame.
More than anything we wanted to keep this from becoming a never-ending quagmire.

After he studied the ticket, I gave him a high-level overview of the code and how I organized things, so we could discuss how to slice and dice the changes into coherent reviews.
And that's what I did next.

I sat down and went trough my 50-or-so commits for that task.
The tendency to meticulously split them up by what code they touch really payed off and turned out to be far more important than their size.
In fact, some commits were rather large but these were the ones adding a bunch of new classes that collectively implement a small feature.
The ones touching code predating my work were typically very small, focused, and not bundled with the new stuff.

Combing through the commits took me about two hours and resulted in nine reviews:

-   three rather large ones covering separate aspects of the new feature
-   two medium sized ones that added functionality to existing APIs
-   three small ones covering minor changes to existing APIs
-   a mostly unrelated refactoring of medium size

Each review contained an explanatory paragraph that put it in relation to the other reviews.

As an added benefit, and as is common, the detailed preparation led to some fine tuning of the underlying code and its [comments](comment-your-fucking-code).

## Into The Light

Now it was time to start the actual review.
We discussed in which order to work through them, opting to start with the ones covering the new feature.
This was done to:

-   get the big stuff out of the way first
-   see the requirements for the API changes before reviewing them

For now there was nothing more I could do, so I let my reviewer get to work.
And work he did!
Over the next hours I watched Crucible's notifications filling up my inbox.
I mulled over his comments, prepared some changes and made some notes.

Commonly I would've replied in Crucible and we would engage in a written conversation.
I see a lot of value in that, especially the asynchronicity and that such discussions are persisted.
But we agreed that in this case it would draw things out enormously, leading to a lot of back and forth between the many, many changes.

Instead we decided to finish with some pair reviewing.
After my buddy was done with his first pass (which took a couple of hours) and I had seen all his comments, we sat down together and went through each of them.
Not only was it a lot of fun and educational for both of us, it also kept us energized and focused after a long day of reviews.
We cleared up misunderstandings, identified areas that warranted further scrutiny, and noted which improvements I was going to make.
Along the way we documented every decision we made as we always do: as comments in Crucible.

With the darkness behind us, it turned into just another review.
Over the next couple of days I would change the code as discussed and he would check off the list of improvements one by one.
There were a couple of details to iron out but nothing big to discuss.

## Reflection

To err is human and so is to learn from one's mistakes.
Here's what we learned from being faced with a potentially enormous code review:

Own up!

:   -   Don't just dump it on your reviewer, neither the feelings nor the feedback will be positive.
(Ok, we didn't learn this now because we didn't do it.
It just seemed obvious to us.)
	-   Talk to your reviewer and make sure to get him or her on board.
	-   Consider spreading the workload across several reviewers if possible.

Create good reviews!

:   -   Try to create as many disjunct reviews as reasonably possible.
	-   Prepare each review with at least a single introductory paragraph, creating context and connecting it to the other reviews.
	-   Decide on an order that respects the code's structure.

Don't draw things out!

:   -   Try to hit the ground running by investing a few hours at once.
	-   Get together for a focused discussion of all remarks.
	-   Make time to promptly implement all changes.
	-   Consider accepting a slightly less intense review to speed things up.

(In fact, many of those suggestions apply to reviews in general.)

All in all, I suspect that the reviews were not as thorough as if I had created smaller ones while working on the feature.
But after that ship had sailed I am convinced we made the best of it.

What about you?
Do you have any horror stories about ginormous code reviews?
