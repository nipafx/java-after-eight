---
title: "Jakarta EE, javax, And A Week Of Turmoil"
tags: [community, jakarta]
date: 2019-05-13
slug: jakarta-ee-javax-and-a-week-of-turmoil
description: "During a week of turmoil, many people have written about Jakarta EE and javax. This post summarizes the community's opinions and gives you plenty of links."
intro: "During a week of turmoil, many people have written about Jakarta EE and javax. This post summarizes the community's opinions and gives you plenty of links to follow up."
searchKeywords: "Jakarta"
featuredImage: jakarta-ee-turmoil
---

By now, most of you will already know that JEE - formerly Java EE, now Jakarta EE - hit a little roadblock with the `javax` package name.
Well, a big roadblock.
Maybe even an unyielding roadblock, although I don't think it's that bad.
During a week of turmoil, many people have written about the matter and this post summarizes the community's opinions and gives you plenty of links to follow up.

*By the way, I have originally written this as a section in my occasional newsletter.
[Subscribe](news) if you want to read stuff like this in your inbox.*

## Java EE to Jakarta EE

About two years ago, Oracle decided to get rid of Java EE and after some time agreed with the Eclipse Foundation to hand it over to them.
What does it mean to transfer such a project, though?
It's obviously a little more complex than just forking a repo.
Here are some of the things that need to be settled:

-   under what process should the new project be governed?
-   what about the proprietary (and highly confidential) Java EE TCK?
(needed to verify that implementations as conform to the JEE standard)
-   what about copyrights of essential intellectual property, e.g. documents, standards, specifications?
-   what about naming rights, i.e.
the *Java* trademark?

Almost everything was settled - it was the last item that proved to be insurmountable.

## No touching javax

After 18 months of negotiations, no solution for the trademark issue was found.
Oracle was adamant that it wanted to keep sole ownership of *Java* and that nobody outside of its sphere of influence should be able to publish anything under that name.

This became public knowledge on May 3rd when Mike Milinkovich, Executive Director of the Eclipse Foundation, published [*Update on Jakarta EE Rights to Java Trademarks*](https://blogs.eclipse.org/post/mike-milinkovich/update-jakarta-ee-rights-java-trademarks).
If you only click one link in this post, it should be that one, but if you don't, here's the money quote:

> Eclipse and Oracle have agreed that the javax package namespace cannot be evolved by the Jakarta EE community.
> As well, Java trademarks such as the existing specification names cannot be used by Jakarta EE specifications.

That means `javax`, root package name for all Java EE packages, can't be touched.
Jakarta is allowed to publish artifacts that contain the API as-is, but it's *not* allowed to evolve it - no new methods, no removal of classes, no moving things around, nothing.
Not a great place for a code base to be in.

(If you want to peek behind the scenes, have a look at the minutes of the Eclipse Foundation's board of directors meeting on March 26th \[[PDF](https://www.eclipse.org/org/foundation/boardminutes/2019_03_26_Minutes.pdf)\], starting on page 5 with *Status review of all outstanding Jakarta EE-related legal agreements*.)

## What now?

Mike answers many obvious questions in the same blog post and his follow-up from May 6th, [*Frequently Asked Questions About Jakarta EE 8*](https://eclipse-foundation.blog/2019/05/08/jakarta-ee-8-faq/), e.g.:

-   What does it mean for Jakarta EE to not modify the `javax` package namespace?
-   Will there be a Jakarta EE 8?
-   Will Jakarta EE 8 break existing Java EE applications that rely upon `javax` APIs?
-   What will Jakarta EE 8 consist of?
-   When will Jakarta EE 8 be delivered?
-   What happens beyond Jakarta EE 8?

If you only click two links in this post, ... 😜

## Public opinion and interesting insights

Of course Mike's first post sparked immense discussions on Twitter and various blogs.

On Twitter, the opinions ranged from "Oracle killed Java EE" to "Jakarta EE is gonna turn lemons into lemonade".
My impression is that most people with good knowledge of and insights into Jakarta EE tended strongly towards the optimistic view whereas the opinion of the general Java/JEE dev was much more pessimistic.

You could say that "Oracle killed Java EE" is a superficial assessment, driven by general distrust and doomsayery, whereas those who know how this works understand that this is not as bad as it sounds.
*Or* you could say that most of the more knowledgeable people have a skin in the game and desperately want Jakarta EE to succeed, so they're optimistic by necessity, not conviction.
Personally (and without mich insight!), I have a tendency to believe the former, but am wary.

The blogs have mostly been written by the optimistic crowd.

⇝ [*Jakarta EE: A New Hope*](https://www.tomitribe.com/blog/jakarta-ee-a-new-hope/) by David Blevind (May 3rd)

Published very soon after Mark Milinkovich's first post, David describes his initial emotional reaction and the necessity of a new vision.
It basically is *clarity*:

> We were never going to have complete freedom over `javax`.
> This recent change ultimately means that we can no longer stretch the reality of that over 10 years and must deal with it now, immediately.
> In many ways it is a blessing in disguise.
> Some challenges we would have had to have faced are gone.
> We do now have new challenges.

He also links this to the advent of [Quarkus](https://quarkus.io/) in a very interesting way:

> A healthy disruption Quarkus brings to our industry is forcing us all to transition into deploy-time and build-time generation.
> This does require a huge investment, however.
> This impossible-to-avoid `javax` to `jakarta` migration will require vendors to make a big investment in the exact place it needs to make anyway to catch up to Quarkus.
> The reality is this unfortunate legal restriction on `javax` will force the industry to do something it needed to do anyway, invest in deploy-time bytecode enhancement, and very likely bring us more "Quarkuses" 1 to 2 years sooner.

He closes:

> It will be tough, but ultimately we're being forced to free ourselves.
> On the other side, what we do is truly up to us.

⇝ [*Negotiations Failed: How Oracle killed Java EE.*](https://headcrashing.wordpress.com/2019/05/03/negotiations-failed-how-oracle-killed-java-ee/) by Markus Karg (May 3rd)

Not everybody's that optimistic, though.
Markus paints a bleak picture and he clearly blames Oracle for the result:

> The reason simply spoken is, according to the [recent board meeting minutes](https://www.eclipse.org/org/foundation/boardminutes/2019_03_26_Minutes.pdf), that Oracle wanted to have in turn a set of inacceptable demands.
> Some of them would put the existence of the Eclipse Foundation at severe risk.
> Oracle claimed that products distributed by the Eclipse Foundation (like the Eclipse IDE) must *only* be bundled with Java runtimes certified particularly by *Oracle and its licencees* — *not any other vendor's* certification and not any uncertified runtime.
> Hence, the IDE and GlassFish wouldn't be vendor-neutral products anymore.
> [...] But once Eclipse products would be not vendor-neutral anymore, the EFs tax exemption might become void, which would mean a financial fiasco, or possibly mean the end of the organization as a hole.
> Hence, it not only was *inacceptable*, but it was simply *impossible* to agree to Oracle's requests, so the negotiations more or less completely failed.

He closes:

> For me, the glass is not just half-empty anymore: Today it cracked into pieces.
> This is the day when Java EE was killed by Oracle.

Everybody else (whose blog post I discovered) is ok with the required migration from `javax` to `jakarta`, though:

-   [*Jakarta Going Forward*](https://www.agilejava.eu/2019/05/05/jakarta-going-forward/) by Ivar Grimstadt (May 6th, I guess)
-   [*Java EE, Jakarta EE and the Dead "javax"*](http://adambien.blog/roller/abien/entry/java_ee_jakarta_ee_and) by Adam Bien (May 7th)
-   [*Jakarta EE: A Clean Slate*](https://readlearncode.com/jakarta-ee/jakarta-ee-a-clean-slate/) by Alex Theedom (May, 8th)

But how exactly would that go down?
There are a few posts that talk about the technical aspects.

⇝ [*The way forward for JakartaEE packages*](https://struberg.wordpress.com/2019/05/06/the-way-forward-for-jakartaee-packages/) by Mark Struberg (May 6th)

A good overview over the options that are on the table and the tradeoffs between them:

-   **Option A**: Keep `javax` packages alive, extend classes in `jakarta` packages as needed ⇝ not fit for purpose because of existing method signatures and inheritance hierarchies
-   **Option B**: Like **A** but extend all classes now and edit as needed ⇝ same drawbacks
-   **Option C**: Rename `javax` packages to `jakarta` packages

Mark also throws the idea out there that it may be confusing if Jakarta EE 8 used `javax` packages and, say, Jakarta EE 9 renamed them to `jakarta`.
Would it be better to do the rename *before* the release of 8?

I think this proposal is worth thinking about because, without it, the update from Java/Jakarta EE 8 to Jakarta EE 9 contains two sets of changes: The package rename and feature updates.
I generally prefer updates in smaller steps and would like to be able to do one and then the other.

Unfortunately, [Mark Milinkovich's FAQ](https://eclipse-foundation.blog/2019/05/08/jakarta-ee-8-faq/) (published on the same day, so maybe without taking the argument into account) already said this won't happen:

> We expect Jakarta EE 8 to specify the same `javax` namespace

Jeanne Boyarsky, who I'm proud to say was technical editor of [my book in the module system](https://www.manning.com/books/the-java-module-system?a_aid=nipa&a_bid=869915cb), [made an interesting proposal](https://twitter.com/jeanneboyarsky/status/1124836152593342464) that combines both approaches:

> Create the new naming and package both in the same jar to facilitate transition.
> Aka start as soon as possible but also keep support as long as possible

That said, Mark already went ahead and experimented with renaming packages in Apache Tomcat and was successful [as it seems](https://twitter.com/struberg/status/1125871529802252288).

⇝ [*The Future of Jakarta EE in the Wake of JavaEE*](https://martijndashorst.com/blog/2019/05/07/javaee-jakartaee-showdown) by Martijn Dashorst (May 7th)

Martijn quotes a dutch proverb saying "Gentle doctors make stinking wounds" (hah, I like that one!) and also argues for a package rename before the Jakarta EE 8 release.
Interestingly he thinks this may *increase* adoption of Jakarta EE 8 because, without that rename, Java EE 8 and Jakarta EE 8 are identical, so why would anybody make the switch?

> The future of JavaEE is Jakarta EE, might as well make it official with the proper package names.
> This will delay the release of Jakarta EE 8, but I don't think anyone was anxiously to adopt this release as the only change would be a new steward for the standards.

⇝ [*Thoughts on the Jakarta EE package name change*](https://blog.sebastian-daschner.com/entries/thoughts-on-jakarta-package-name) by Sebastian Daschner (May 7th)

Sebastian clearly discerns between the impact on JEE app servers and on JEE API users.
Regarding the servers he writes:

> Any runtimes that know and handle EE APIs, e.g. application servers, have to adapt and switch to the new name.
> They will have to implement some functionality to live with both `javax` and `jakarta`, very likely simultaneously, simply because they have to.
> There's too much code out there that won't be migrated to base on either `javax` or `jakarta` fashion.
> In the real world, there are legacy projects, tons of libraries and dependencies, binaries for which no source exists, and much more.
> We need a way to tell a runtime to just live with both, at least temporarily, or in specific compatibility profiles.

We'll see in the next post how that could be implemented, but before getting there I want to come to Sebastian's second point.
He also advocates for a Jakarta EE release that is identical to Java EE 8 except for the package name, but would give individual projects time until a future release (8.1?, 9?) to update all their dependencies and imports:

> I think a clean cut is to offer the current Java EE APIs, under both Java EE, with `javax`, and Jakarta EE with `jakarta`.
> This would be needed for both the platform (`javaee-api`) and individual specifications such as JAX-RS.
> The projects then have an easy control, via their resolved dependencies, which one to use and can swap their imports accordingly.

So how would the application servers be able to handle both namespaces?

⇝ [*Thoughts about Jakarta EE future without `javax`*](https://dmitrykornilov.net/2019/05/03/thoughts-about-jakarta-ee-future-without-javax/) by Dmitry Kornilov (May 3rd)

Dmitry describes how a big-bang rename may go over well for users who want to run their Java EE applications on new Jakarta application servers:

> A good option is to create a special backwards compatibility profile in Jakarta EE platform.
> This profile should contain a frozen Java EE 8 APIs and will allow to run Java EE 8 applications on future versions of Jakara EE Platform.
> This profile can be optional to allow new potencial Jakarta EE vendors concentrate only on innovations, but I am sure that all big players such Oracle and IBM will support it anyway.
> How the backwards compatibility can be implemented technically?
> [...] Another way is patching application binaries at runtime or build time.
> Runtime solution can be accomplished using JavaAgent and build time via tooling and build plugins.

[Rafael Winterhalter on Twitter](https://twitter.com/rafaelcodes/status/1125032183167688706) already wrote the code for that Java agent proposal:

> There, I fixed it:
>
>     new AgentBuilder.Default()
>       .type(nameStartsWith("javax."))
>       .transform((b,t,cl,m) -> b
>         .name("jakarta." + t .getName().substr(6)))
>       .installOnByteBuddyAgent();

I think now it's just a matter of implementation.
😋

## What can you do?

If you're now wondering what you can do, there are a few options.
Regarding your own projects, you can check your JEE dependencies and look into updating them.
While it looks like you will be able to run Java EE code on Jakarta EE app servers (at least for a while), it's good to have the option to update to the newer versions.
Like with the migration away from Java SE 8, preparing to leave Java EE 8 means bringing dependencies up to date.

If you want to follow the discussion or have formed an opinion and want to make it heard, subscribe to [Eclipse's *jakarta-platform-dev* mailing list](https://accounts.eclipse.org/mailing-list/jakartaee-platform-dev).
On May 6th, David Blevins [posted two proposals](https://www.eclipse.org/lists/jakartaee-platform-dev/msg00029.html) (\#1: Big-bang Jakarta EE 9, Jakarta EE 10 New Features; \#2: Incremental Change in Jakarta EE 9 and beyond) and they are currently being discussed.

## Random quotes

I'll leave you with an intriguing and a cynic tweet on the topic.

> Alrighty, so, hypothetically, can I fork Java and add packages to it?
> Add types to existing packages ?
> As long as I don't call it Java?
> Just the packages stay the same?
> [Josh Long on Twitter](https://twitter.com/starbuxman/status/1125123193692405762)

> Credit where it's due, this javax decision has done the impossible task of unifying the Java EE and Spring communities.
> They both think it's f\*\*ing stupid!
> [Phill Webb on Twitter](https://twitter.com/phillip_webb/status/1125389244778700801)
