---
title: "Impulse: \"Agile Architecture\""
tags: [agile, architecture, impulse]
date: 2015-03-26
slug: agile-architecture
description: "A summary of the talk \"Agile Architecture\" given by Molly Dishman and Martin Fowler as the keynote at the O'Reilly Software Architecture Conference."
searchKeywords: "agile architecture"
featuredImage: agile-architecture
---

A software system's architecture is traditionally created in a designated phase by designated people.
Agile projects don't have the former and agile teams not necessarily the latter.
So how can a team make sure that architecture is happening in an agile environment?

Molly Dishman and Martin Fowler answer this and other questions and help with creating an agile architecture.

This post outlines their talk [_Agile Architecture_](http://softwarearchitecturecon.com/sa2015/public/schedule/detail/40388), held as the keynote at the [O'Reilly Software Architecture Conference in March 2015](http://softwarearchitecturecon.com/sa2015).

## The Talk

Here's the talk:

https://www.youtube.com/watch?v=VjKYO6DP3fo

I did not find the slides but they seem to be more illustrative then informative so this is no big loss.

## The Gist

The talk is split into two parts.
[The first](#What-Is-Architecture) is about architecture in general which includes a definition for the term.
[The second](#How-to-Ensure-Architecture-Is-Happening) focuses on how to ensure that architecture happens in an agile environment.

### What Is Architecture?

After giving a definition, Dishman and Fowler outline why agile projects might lack architecture.
They conclude this part with providing a better metaphor than "architecture" for what software design is about.

#### A Definition Based on Complexity

A text book definition says that architecture manages a software system's complexity and deals with core structural elements - but what are those?

According to Dishman and Fowler:

> Architecture is concerned with those aspects of a software system that are hard to change.
Or, even more loosely, "the important stuff", whatever that happens to be.
(There is generally a high correlation between the two properties.)

This hinges on the observation that irreversibility is a core driver of complexity: When something is too hard to change, past decisions can not be reversed and constantly working around the limitations they impose makes new decisions more complex.
That is why "hard to change" is a good approach to identifying architecturally important parts of a system.

As a side note Fowler states that lean manufacturing tries to reduce complexity by making decisions reversible.

#### Architecture in Agile Projects

In a stereotypical waterfall project, architecture/design has its own phase whose result is a plan on how to implement the system, presented as an encompassing set of documents.

The [agile manifesto](http://www.agilemanifesto.org/) contains two points which are relevant in this context:

-   working software over comprehensive documentation
-   responding to change over following a plan

If these are misinterpreted as "working software and no documentation" and "responding to change and no plan", projects run the risk of creating an ad-hoc and implicit architecture which does not work towards any agreed upon goals.
This increases the complexity and risk of the project and must be avoided.

So an agile software project also requires architecture.
It must make sure that it is actually being worked on and, being agile, that this is done in a way which allows to iterate on it and change it.

#### A Better Metaphor for Architecture

In an interesting digression Fowler addresses the dynamic nature of software projects and talks about the metaphor of "architecture" for the activity of working on a software system's core structural elements.

The metaphor obviously comes from the construction and industry and "as typical with software development, we got \[it\] completely wrong".
An architect in construction is focusing on the experience of the people populating the building - in a software project this would be a [user experience designer](https://en.wikipedia.org/wiki/User_experience_design).
The work of a software architect more closely corresponds to that of a [structural engineer](https://en.wikipedia.org/wiki/Structural_engineer).

But a better metaphor for what software architecture is about is [city planning](https://en.wikipedia.org/wiki/Urban_planning).
It deals with a complex and evolving system while trying to keep up a certain level of coherence.
It is impossible to create a fully detailed plan and stick with it in the face of future changes but it is still essential to have some plan and adapt it in order to avoid a chaotic city.
The similarity to the dynamic of software projects are obvious.

### How to Ensure Architecture Is Happening?

The first part of the talk identified what architecture is about and stressed that a lack of it will hurt all teams, including agile ones.
This raises the question of how to ensure that architecture is happening if there is no specialized phase for it.

The answer Dishmann and Fowler give is to look at existing or new activities which are embedded in the development process and make sure to maximize the architectural value they provide.
They continue to give some examples while also discussing some practices to create good architecture.

#### Kick-Off

At a project kick-off meeting (what Dishmann calls an inception) software architects should be present.
Together the group should discuss technical aspects, identify the things that are hard to change and agree on them.

They might also discuss what is easier to change and where change is most likely to occur.

#### Make Decisions Reversible

The traditional project management tries to identify and make the important decisions early.
But getting them right is hard and, as discussed above, irreversible decisions will considerably increase complexity.

In an agile project the focus should instead lie on finding ways to make these decisions reversible, to make these things easy to change.
This is hard as well and may not always be possible but every time you manage to pull it off, you get a very big win.

#### User Stories

Agile projects often employ user stories to break down functionality and deliver it incrementally.
Stories are usually based on user value.

Work on architectural aspects of the project should also be presented with stories and they should show the added value.
This enables to track architecture development, acknowledges that architectural work happens all the time and allows the team to talk about the key parts of the system.

It is also reasonable to do architecture spikes which try to find out how possibly irreversible aspects could be implemented, how hard they would be to change and whether it is possible to make that easier.

#### Where Is the Architecture?

Agile projects value working software over documentation, so where does the architecture live if not in documents?
While these are still useful and should not be shunned, documents are just one of many representations of the architecture (some others will be [mentioned later](#Communication)).

The most important representation is the shared and coherent understanding of the team of how the system fits together.
The architecture must be in the heads of the people creating the system!

Fowler presents a technique by Kent Beck to determine whether a team shares a common understanding: Talk to each team member individually and let each explain the system using only four objects.
The more similar the chosen sets of objects are across developers, the more coherent is their understanding of its architecture.

#### Example Areas of the Code

An often underestimated way of how a system's architecture is manifested are examples.
People often look to examples in the code base for inspiration how to do things.
So to enable a consistent architecture, make sure people look at the good bits and not at half-assed attempts which are not yet fixed up!

This can be done by pointing out areas in the code which are good examples for how to do certain things well.
Those exemplary areas will then become an important driver of architecture.

Another way are show cases, e.g. wiki articles or meetings, which demonstrate how certain aspects of the system work.

#### Engaged Architects

Throughout the talk Fowler repeatedly addresses the role architects should play.

First, and maybe most importantly, he stresses that working on architecture should not be delegated to a designated group of people who do little else.
In agile software development, where there is no separate phase just for design, there should also not be a fixed role for it.
(However, it is still important to designate time to work on it.) It is instead a team activity, where everyone takes some responsibility.
More senior members will naturally gravitate towards more complex or more important decisions (and can thus be called architects).

Analogous to everybody being involved in developing the system's architecture, everybody should be involved in implementing it.
This implies that architects should not be divorced from everyday coding.
Instead, they must stay engaged with the project to see what is actually going on.
This helps them to enforce their ideas where this is helpful but also to identify and learn from errors and change the plan accordingly.

Fowler gives two example practices which might help with this.
To stay engaged an architect could set aside some time per week to triage all recent commits.
He would look for developers which, for very different reasons, could improve their efficacy and then conduct some pair programming sessions with them.
To not be overwhelmed by the time demands of implementing crucial or complicated features an architect could decide to never be directly responsible for any feature.
Instead he could only work on them indirectly, pairing with other people, helping them with design or implementation.

#### Collaboration

If agile architecture is a team effort, all members must participate in creating it.
So when a new decision must be made or the existing design must be changed or enforced, it is important to include the whole team.

To do this the interested members of the team (not necessarily all of them) should come up together with what they want to achieve.
It is then crucial to communicate the decision to the whole team and engage them in moving towards that position.
Changes can be made incrementally but the progress should be checked on and it should be ensured that the goal is eventually reached.

Tools can support this process.
They can help to evaluate the current situation, to monitor change and progress and enforce reached goals to prevent slipping up in the future.

#### Communication

Dishman stresses the importance of communicating about architecture and to convey what is currently happening.

She mentions mob-code-reviews and mob-code-refactorings (unfortunately without going into detail about them) as a way to spark conversations between different parts of the team (separated by locations, specializations, ...).

Since not every detail can (and should) be kept in the team's heads it is necessary to use tools to communicate architecture.
Besides the obvious ways of writing documents or creating diagrams she mentions some other possibilities, like showing code, specifying APIs or doing code analysis.
The [above mentioned example areas](#Example-Areas-of-the-Code) also have this goal.

If the system's architecture is monitored or influenced by people outside of the agile team, they should enable it to make architectural decisions and partake in those activities and communication channels with which architecture is created.

### Make It Happen

Nowadays many things are less hard to change than in the past: Evolving database schemas became a common practice and even migrations from relational to NoSQL databases are supported by tools; infrastructure as a service allows to easily modify hardware resources of a project and environment provisioning enables changing configurations across teams without much hassle.

But it is still necessary to think about the important stuff, to work on architecture.
And project management should make sure senior team members have the time to think about and work on it.
To finish with two quotes from Fowler:

> "In the end the most important thing is the will to do this, that you have people who care and that you give them the space and time to do that."
>
> "Because like so many other things in software, getting a good architecture for your system is primarily a people problem."

## Reflection

In their keynote Dishman and Fowler describe how agile architecture works.
They define architecture as being about the things that are hard to change and emphasize that a team will benefit greatly if they can reduce the number of those things.

Then they go on to explain how an agile team can ensure that architecture is still happening even though there is no designated phase and not necessarily a designated role for it.
The underlying principle to achieve this is to keep the whole team communicating about and engaged with architecture.
