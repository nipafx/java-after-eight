---
title: "Comment Your Fucking Code!"
tags: [clean-code, clean-comments, documentation, rant]
date: 2015-07-15
slug: comment-your-fucking-code
description: "You think your code is so clean that it doesn't need comments? Then this rant is just for you!"
searchKeywords: "comment"
featuredImage: comment-your-fucking-code
---

You're the elite.
You know Clean Code by heart, you dream of SOLID design, and you unit-test every line you write.
Your code is so self-documenting you don't even need to write comments!

Then this rant is just for you!
Because let me tell you something: Without comments, working with your code is still a fucking pain.
No matter how elite you are and how awesome your code is, without some intelligently placed explanations it is still an unusable and unmaintainable pile of shit.

*I never ranted before - not here at least - but I have to do it now or my head will explode!
So please forgive my tone.
After calming down I will write [a more levelheaded post](thoughts-on-comments) about the topic.*

***Note From The Future:** It turns out that we developers do not have a shared understanding of whether API docs, like Javadoc or .NET's XML documentation, are also comments.
This rant is written on the premise that they are, so writing API doc would absolutely fulfill my call to comment some fucking code.*

## Self-Documenting Code

The next time I stumble across an interface or a class of yours which has zero comments, I swear I am going to hunt you down and chew your legs off.

But the code is self-documenting, you say?
It is SOLID, well formatted, uses expressive names, consists of short methods and classes and is fully tested?
It doesn't need comments?

## A Story About Contracts

Let's start with a story...

Imagine you're looking for an insurance to save your family from living on the streets in case you get hit by a truck.
You decided on a policy and request a contract from the company so you can sign it.
But guess what?
They don't do contracts anymore!
Instead they have adopted a "working company over unread contracts" policy.

They will happily invite you over to their headquarters where you are free to admire their crisp organizational structure, take walks around their expertly designed buildings and parks and talk to any of their couple of thousand attentive employees.
Really, you should come, these guys and gals even have great names like Mitch and Dorothee!

They would also like to give you a list of existing customers.
You can call them up and find out how they fared with the company.
Given their personal history and payment options, when something happened to them, then what did the company do?
And by the way, you can even visit *their* homes, too!

So, you ask them, how would this go down?
Easy, just start wiring us a monthly fee and we'll cover you, they answer.

Awesome self-documenting insurance policy!
But would you trust them with your family's well-being?&lt;

## Tests

So let's get back to code.
By now you might be able to spot the difference between agreed-upon and observed behavior.
What about tests?
I hear you ask, don't they define the expected behavior?

You are trolling, right?
Now I'm supposed to not only check your implementation but also a bunch of tests?
So instead of writing code I am now digging through dozens of tests methods, hoping to identify one which reflects my use case?
And all that to deduce whether I can expect not to get null back?

Ah, yeah.
Unfortunately you can't even demonstrate that in a unit test.
Unless you manage to prove a "for all" theorem with examples, which would be truly impressive.
Other things you can't clearly document in a test?
Only negligible details like thread-safety (or lack thereof) and immutability.

Also, if you trust your tests to document your code, you better make sure that people get fired for not achieving 90%+ test coverage.
And in my experience there is quite some overlap of the "don't write tests" and "don't write documentation" developers.

Finally, do you know how many communities will answer questions with RTFT - read the fucking tests?
No?
Well what did I have in mind then?

## Names

But your names are expressive, you say?
They'll save the day!
Really?
This is something I might be looking for when reading documentation:

-   What are the preconditions, especially regarding the arguments I'm passing in?
-   What promises are made regarding the return value?
-   What units are used for arguments and return value?
-   What about thread-safety, mutability, invariants or dependencies?
-   Under what conditions do exceptions get thrown?

Don't tell me that you stuff all of this into your names because you don't.
And don't tell me that it is always obvious because it ain't.
By the way, I am also interested in why you picked the current design.
I wonder how that fits into a variable name.

And how exactly does that expressive naming work for interfaces?
I can tell you how: not at all!
Or do you expect me to comprehend all of a dozen implementations to find out whether they are supposed to be immutable.

I am also really curious how your colleagues are going to implement an undocumented interface.
Because as I see it, they can basically do whatever the hell they want.

## Guessing

So next time you're starting to use a new library, eat your own dog food.
Ignore the documentation!

Just guess by looking at the code what Java's `Map.get` might return if the key is not present.
Take a look at `ConcurrentHashMap` and tell me whether null keys are allowed.
Use Guava's `TypeToken` or `Cache` without any introduction.
Use reflection without a look at the comments.

What do you say?
These are all APIs?
This is totally different?
Seriously?!
How the fuck is this different?
I don't care whether I use *An API®* or call into your code - either it tells me what it does or I go guessing.

Guessing... Because that's what I'll do if you don't tell me what happens.
You really think I will go through all your five implementations and your three digit number of unit tests to find out what a method does?
No!
I will make an educated guess because I'm in a hurry and have to get shit done.

And wasn't that a goal of Agile?
To make developers guess less?

## Speaking Of Agile

We went from "we're not writing documentation because we hate it" to "we don't write documentation because we value working code over comprehensive documentation".
So now we're ignoring documentation like before but we're finally allowed to because our code suddenly became awesome?
Yeah, right...

So please, instead of believing this bullshit, take a note of how the Agile Manifesto does not say "we do not write comments".

## Aging Comments

But comments age, you say?
That's like saying "cars crash".
Yes, they do occasionally.
(By the way, in most cases due to some form of negligence that is punishable by law.) But that does not stop us from using them.
And it does not mean that we are not responsible to try and prevent that from happening.

Also, I am interested to hear about that magic compiler you seem to be using.
The one that prevents aging of package, class, field, method and variable names.
The one which keeps them up to date and accurate as you change the code.

Oh, there isn't such a thing?
Shocker!
You're updating them as you go?
Congratulations!
Now be a responsible little developer and include those comments in that process and we're fine.

## Comment Your Fucking Code!

So please, stop being so full of yourself and just add a useful comment here and there.
Where?
I'll write about that in [my next post](thoughts-on-comments).

In the meantime, to tell others to comment their fucking code!
