---
title: "Code Reviews At Disy - How We Review"
# subtitle
tags: [code-review, techniques]
date: 2016-09-26
slug: code-reviews-disy-part-2
canonicalUrl: https://blog.disy.net/code-reviews-ii/
description: "After setting out to create a peer review culture we came up with a workflow and picked a tool (yes, Crucible) that would help us get there."
featuredImage: disy-code-reviews-ii
---

This is the second part in a miniseries of posts presenting why and how we do code reviews.
In [the first part](code-reviews-disy-part-1) we explained why we are one big team with 15 to 20 developers, and how that shaped the goals and assumptions we stated for introducing code reviews.
We finished with a list of fundamental principles:

-   All code gets reviewed.
-   Code reviews are peer reviews.
-   Reviewing code is as important as writing code.
-   Reviews should be structured.
-   Reviews are fun!

Let’s now see what we did to get there.

## Code Buddies

So according to our principles, everybody should review and get reviewed… But how do we do that?
Just open a bunch of reviews and see what happens?

As I described last time, code reviews were not really in our DNA and habits are hard to change.
So we were worried that we would just end up with tons of open reviews and a very uneven distribution of who works through them.
So we opted for a little more strictness.

Each month, we take the names of all developers and put them in random order.
Then everybody becomes the next developer’s **code buddy** for this month.
(So this is not symmetrical.)

Your code buddy will have a special eye on you during stand-ups, will always be there if you need someone to kick some ideas around, and—most importantly—will review your code.

This has several benefits:

-   We have a default answer to “Who reviews which code?” and ensure an even distribution of the review workload.
-   The randomization prevents intentional pairing and thus siloing.
-   The time span enables your buddy to get to know the area you are currently working in.

In case your work is part of a larger project, you might ask your project members to review it.
And if you feel that some code you wrote requires a domain expert or an architect to have a look, you can of course add them to the review as well.
In any case, your buddy will appreciate the additional pair of eyes.

## Backlog

There will always be cases where reviewers are sick, on a business trip, swamped with work, or otherwise not available.
If that happens, it is the task of the developer who wrote the code to realize that her reviews are piling up.
It is then her task to alleviate the situation.

We decided to go this way (as opposed to having the reviewer find someone to fill in) because it didn’t seem promising to have the scarce resource do the reallocation work.

Again we have a default: We usually push the reviews to the reviewer’s code buddy.
Because if the reviewer has no time to read code, chances are he is also not writing too much of it, which means his buddy has some time on his hands.
Or we mark a review as “free for all”, signifying that anybody can step up to the plate.
If push comes to shove, we mention our plight during the stand up and some hero will show up to do the review.

We never had any mentionable delays because of unavailable reviewers.

## Crucible

We’re using [Crucible](https://www.atlassian.com/software/crucible) to do code reviews.
We’re not exactly ecstatic about it but it does the job reasonably well and integrates well with JIRA.

We want to leave a trail for future developers, so we decided to always have a review in Crucible.
It’s fine to do reviews in person but then we have Crucible open and put the relevant findings in there as well.
Besides, the diff view does make reviewing more comfortable.

But not all is well with Crucible.
For one, there are a couple of usability fails.

-   Why does clicking *anywhere* on a line of code open the comment pane?
How am I supposed to mark code, e.g. to copy-paste?
-   And then there’s the scrolling.
I can’t look at the issue title without scrolling to the top of the file I am reviewing?
Seriously, WTF?!

But you learn to live with that.
The following, more fundamental, problems require ugly workarounds, though, and that is an ongoing annoyance.

### Custom Fields

We’re a little underwhelmed with Crucible’s [lacking ability to define custom fields](https://jira.atlassian.com/browse/CRUC-1516), which we would like to use to structure additional information or mark reviews as free for all (we’re currently solving this by having a designated user, *Free4All*, which we can filter by).

### Open Discussions

There is no way to mark open discussions, which is *really* annoying.

Say, during his initial pass, the reviewer makes two dozen comments, which he thinks the code’s author should address—either by changing the code or by explaining why the code should stay the way it is.
In her own time, the author does exactly that.
The reviewer gets notified, finds some changes or replies acceptable, but follows up on others.
This goes on for a while…

Now, at any given moment, how do you know which of these two dozen threads still need your attention?
The unfortunate answer is: you don’t.
This was unacceptable for us and we looked for a workaround.
The only thing you can use to highlight comments is by marking them as defects.
So we do exactly that.

Everything the reviewer expects to be addressed is marked as a defect.
He will only remove the marker once he feels the issue is resolved and requires no further attention.
Defects are shown in the details view and can be filtered by.
This makes it pretty convenient to find out what still needs to worked on and whether the review can be closed.

It will of course also mean that we can not use the built-in features to determine how many actual defects we found but we deemed that trade‑off well worth it.

### Pass Completed

It would be nice if a reviewer could inform the author that he has made his first pass.
There is the *Complete* button but we use it as it seems to be intended: To signal that the reviewer is happy with how the code turned out and thinks that the review can be closed.

So we just use [Rocket.Chat](http://rocket.chat/) to ping the author and inform her about the status.

## Review Workflow

With all of the above settled the workflow becomes straight-forward:

-   At some point during the work on an issue—the latest when all the code is committed to Subversion (yes, yes, make your jokes)—the author creates one or more code reviews in Crucible.
-   She invites her code buddy and anybody else who might need to look at the changes as reviewers.
-   The reviewers go through the code, make comments, and mark those that should be addressed as defects.
-   For each “defect”, the author changes the code or enters a discussion.
-   When a reviewer feels that his concerns have been addressed, he removes the defect marker.
Eventually all the comments he made will be addressed and he marks the review as completed.
-   This keeps going until all defects are gone and all reviewers have completed the review.
-   At this point, the author closes the review and we’re done.

In case the review uncovers a problem that can not be fixed immediately (usually because it takes too much time), a new JIRA issue is created, linking back to the review.
The defect is then removed and the discussion seen as resolved for the scope of the review.

### Branches

We are working across a handful of release branches and changes often have to go into several of them.
Reviews usually only happen on the branch on which the changes were originally developed.

But sometimes the architectural foundation differs considerably from one branch to the next.
In such cases, merges can require additional work, which means they can introduce bugs on their own but are also a new opportunity to share knowledge.

So at her own discretion, the author might decide to open a new review for more involved merges.

## Reflection & Recommendations

In [the final post in this miniseries](code-reviews-disy-part-3) I will reflect our process—the good parts and the bad—and share some recommendations.
