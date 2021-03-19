---
title: "Thoughts On Comments"
tags: [clean-code, clean-comments, documentation]
date: 2015-07-31
slug: thoughts-on-comments
description: "My rant to comment your fucking code sparked some interesting conversations. Here we discuss some of your and my thoughts on the topic of comments."
searchKeywords: "comments"
featuredImage: thoughts-on-comments
---

Wow, telling people to [comment their fucking code](comment-your-fucking-code) really hit a nerve.
The reactions covered the whole spectrum from "[just read Clean Code, dude](https://www.reddit.com/r/java/comments/3dhf9d/comment_your_fucking_code/ct5kk0x)" to "[maybe some comments but just a little](comment-your-fucking-code)<!-- comment-2140304517 -->" to "[OMG yes](comment-your-fucking-code)<!-- comment-2137448557 -->".

Now that I cooled down, I'll tackle the topic again with less furor.
I planned to write about my perspective on high quality comments but last week's discussions surfaced so many interesting view points and facts, that I wanted to cover those first.

What follows are some of your and my thoughts on comments.
But this post can't cover all there is to say about the topic.
So there are more (check the _series_ entry in the nav box.)

## Comments Versus ...

### ... Documentation

> Bitch please!
> You don't know the fucking difference between comments and documentation and you rant about this!!
>
> [Moons](comment-your-fucking-code)<!-- comment-2139800467 -->

It turned out that we lack a shared understanding of whether API docs, like Javadoc or .NET’s XML documentation, are also comments.
In the past week this sabotaged several interesting conversations and by now I must assume that it is a source of major misunderstandings in any discussion on the topic.

I am convinced that, yes, API docs are just comments, albeit specially formatted ones.
This is backed by Wikipedia (see articles about [comments](https://en.wikipedia.org/wiki/Comment_%28computer_programming%29) or [documentation generators](https://en.wikipedia.org/wiki/Documentation_generator)), the documentation for [Javadoc](http://www.oracle.com/technetwork/articles/java/index-137868.html) and [.NET's XML documentation](https://msdn.microsoft.com/en-us/library/b2s063f7%28v=vs.140%29.aspx), which both call them *documentation comments*, and the highest rated answers to related questions on [StackOverflow](http://stackoverflow.com/a/209065/2525313) and [Programmers StackExchange](http://programmers.stackexchange.com/a/285795/172170).

Furthermore I consider all comments a form of documentation, even if it is just targeted at other developers, including future me.
Of course, documentation can consist of more than just comments.
Tests, example applications, diagrams, readmes, Wiki articles, and so forth can all be used to document a system.

But regardless of my interpretation of the terms it became obvious to me that any meaningful discussion about the topic must start with an exchange of what exactly we mean when we say comments.

**When I say *comments*, all forms of documentation that lives in source files is included unless I specifically say otherwise.**

### ... Clean Code

> I will take code where the developers just don't use variables all named "ufw", "q", "q1", "ql", "qi", etc vs. over even full Javadoc (at the expense of other factors), any day.
>
> [Various\_Pickles](https://www.reddit.com/r/java/comments/3dhf9d/comment_your_fucking_code/ct61jr4)

I absolutely agree with Pickles.
If it's either clean code or value-adding comments, I prefer clean code as well.
But why would it be either or?

This breaks down to a frequently voiced opinion: It is always better to invest time into cleaning code than into writing comments.
Under the assumption that comments can add value (even if only rarely and only a little), this statement is clearly false.
As the cost for improving code grows to infinity it must be at some point more effective to add comments.

This is especially true for comments which explain *why* a piece of code is written as it is.
Clean code does little to provide such context.

In general I think that "good code without comments" versus "bad code with comments" is a false dichotomy.
Clean code and good comments are complements in understanding a system.
So why not spend time making the code as clean as possible *and then* add comments where they will help the future reader?

> There is this prevalent notion in the software world that good code doesn’t need comments, that it stands on its own.
> Or that comments are a code smell.
> [...]
>
> Proponents of this myth point out that it’s easy for comments to get out of sync with the code and decide that because this approach is not perfect, it should be avoided altogether.
>
> This is a false dichotomy that is easily avoided by making it clear to your teammates that both code and comments need to be reviewed.
>
> [Cedric Beust - Your code doesn’t speak for itself](http://beust.com/weblog/2015/05/02/your-code-doesnt-speak-for-itself/)

## Commenting Schema

In my rant as well as in this post, I have never gone out to say how I would like code to be commented.
I am actually not sure yet but after talking about APIs I will make a very preliminary proposal.

### APIs

> As useful as javadocs are for public APIs, they are anathema to code that is not intended for public consumption.
>
> Robert C. Martin - Clean Code

I consider the qualifier *public* ambiguous at best and misleading at worst.
We should look for other characteristics to identify APIs worthy of documentation.

#### What Is Public?

Possible social interpretations are "the general public", "everyone outside the company", "everyone outside the department" and so on.
More technical interpretations are "everyone calling this code from another system" and "everyone calling this code from another module".

In the extreme case this allows developers to claim that since their code is not called by anyone outside the company, there is no need to write any comments.

More commonly, *public* is understood as "calls from outside the assembly" (e.g. from another JAR).
But then it suddenly makes a difference whether some often used code is living inside another assembly or in its own.
This doesn't make much sense, either.

#### What Is Important?

APIs are the epitome of reusable code.
A popular public API might be used by hundreds of thousands or even millions of developers.
It gets popular by abstracting a substantive problem in an approachable way.
A great enabler of this is a helpful documentation of which contract comments are an integral part.

Back to the puny little APIs we write, be they anti-corruption layers, sets of utility methods, handcrafted UI controls, or any other piece of code we hope will be used throughout our codebase.
While most likely less successful, similar rules apply.
They solve concrete problem domains and provide abstractions for others to use, which users will do more easily if they can find their way around.

So instead of gauging an API by how public it is, we should consider how it will penetrate our codebase.
The more often it will be used, the better it should be documented.

### A Strawman Proposal

Here is a simple set of rules which I would consider a starting point to develop a commenting schema with my team:

-   Briefly explain the central abstraction of a class or interface in an API comment.
Assume that not every developer knows as much about the business logic as you do.
-   If the code creates an abstraction which is supposed to be used throughout the system, provide full Javadoc for the public surface.
Invest time to make the comments meaningful.
-   Don't use inline comments to explain what the code does unless some arcane and unavoidable language mechanism is used that can not be extracted into a properly named class/method/function.
-   Do use inline comments to explain the rationale behind non-obvious design decisions like workarounds for known bugs or complicated business rules.
-   If you change code, look out for comments in the same file which might need updating

Of course all of this goes of course on top of clean code, proper tests, ... Now, rip it apart!
:)

## Social Dynamics

> Typically, developers work in teams.
> Teams are made up of humans.
> Humans are susceptible to stress.
> Humans are pressured by deadlines.
> Office politics.
> Personal issues.
> People fuck up \*all the time\*, and that’s ok.
> Comments can be very useful, but if you rely on them, they \*will\* go stale.
> Code review can alleviate this, but errors will still slip through.
>
> And some people just don’t care.
> I can almost feel your obvious response coming: “Those people should be swiftly fired!”
>
> Right.
> Once again, let’s not forget office politics.
> It’s not uncommon for a development team to include the boss’s inbred nephew.
>
> [Jezen Thomas](comment-your-fucking-code)<!-- comment-2137987814 -->

Considering social dynamics is critical!
A team has to decide for itself which commenting schema fits its needs and then work together to achieve that.
Development techniques like pair programming or code reviews will go a long way in supporting this.

### Catch-22 Of Comment Quality

> You have to teach developers what a "good" comment is before they will see the value in writing comments.
> "//Loop through array and filter out anything bad" vs "//Removing syntactically incorrect records per " etc...
>
> [Mike](comment-your-fucking-code)<!-- comment-2137710954 -->

Yes, continuously reading poorly formulated or ill-conceived comments doesn't motivate to write or maintain high quality ones.
To convince people to adapt new techniques one has to demonstrate the benefits.

That being said, the people claiming comments are generally out-of-date and useless are often also the ones creating the problem by not updating existing comments.
A team which values comments and wants to benefit from them must hence prevent this self-fulfilling prophecy.

### Inclination And Skill

Many developers dislike or even hate writing comments.
Additionally, writing meaningful documentation requires at least a moderate level of writing skills that not all developers achieve.
(This lets them claim that a comment can not add anything that is not clear from the code.) Of course these two aspects amplify each other.

But basing decisions on whether to write comments or not on reluctance and partial incompetence is unprofessional, to say the least.
Just get over it!
If there are good reasons against commenting, great, otherwise just be a professional and do it.

Besides, writing is a skill in which a sufficient level is achieved easier than in many other development-related areas.
As an extra, improving it benefits our life in general, something which can not be said for most of the things we learn to be good developers.

> The difference between a tolerable programmer and a great programmer is not how many programming languages they know, and it's not whether they prefer Python or Java.
> It's whether they can communicate their ideas.
> By persuading other people, they get leverage.
> By writing clear comments and technical specs, they let other programmers understand their code, which means other programmers can use and work with their code instead of rewriting it.
> Absent this, their code is worthless.
>
> [Joel Spolsky - Advice for Computer Science College Students](http://www.joelonsoftware.com/articles/CollegeAdvice.html)

## Miscellaneous Contributions

There were a couple of longer threads.
One, [started by GMNightmare](https://www.reddit.com/r/java/comments/3dhf9d/comment_your_fucking_code/ct5h6n3), contains a good example where a comment saves the day and two insane discussions about how to prevent that.
I largely agree with GMN that this is a situation where anything but an inline comment makes everything more complicated.
[Another such example is presented by Lukas Eder](https://www.reddit.com/r/java/comments/3dhf9d/comment_your_fucking_code/ct5idbt).
We also had [a discussion about a piece of code from Guava](https://www.reddit.com/r/java/comments/3dhf9d/comment_your_fucking_code/ct567bo).

And there was great technical advice by [Sean Reilly](https://twitter.com/seanjreilly):

> You can, and should, unit test for immutability.
> The excellent library [MutabilityDetector](https://github.com/MutabilityDetector/MutabilityDetector) does a great job with this.
> [...]
>
> Similarly, the great [Venkat Subramaniam](https://twitter.com/venkat_s) has been speaking for years about how to test for thread safety.
> Techniques such as “inject your locks” go a long way.
> [...]
>
> [Sean Reilly](comment-your-fucking-code)<!-- comment-2141715007 -->

I can't wait to try out [Mutability Detector](https://github.com/MutabilityDetector/MutabilityDetector)!
Make sure to read the rest of the comment as well.

## Reflection

We discussed some of your comments on my rant and I put a couple of things in perspective.
But a lot of other thought arose from the discussions and there will be more posts about comments in the near future.

> Don't document and make your code unreadable and complicated.
> That way you can't be fired.
> /s
>
> [Adamas\_Mustache](https://www.reddit.com/r/java/comments/3dhf9d/comment_your_fucking_code/ct5dxtf)
