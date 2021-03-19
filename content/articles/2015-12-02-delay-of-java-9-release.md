---
title: "Six-Month Delay Of Java 9 Release"
tags: [java-next, java-9, project-jigsaw]
date: 2015-12-02
slug: delay-of-java-9-release
description: "Mark Reinhold proposed a six-month delay of JSR 376 / Project Jigsaw and thus of the Java 9 release. According to this JDK 9 would come out in March 2017."
searchKeywords: "Java 9 Release"
featuredImage: delay-of-Java-9-release-proposed
---

Yesterday evening Mark Reinhold, Chief Architect of the Java Platform Group at Oracle and Specification Lead of [JSR 376](https://www.jcp.org/en/jsr/detail?id=376), for which Project Jigsaw is the current prototype, proposed a six-month extension of the schedules [for the JSR](http://mail.openjdk.java.net/pipermail/jpms-spec-observers/2015-December/000233.html) and [for Java 9](http://mail.openjdk.java.net/pipermail/jdk9-dev/2015-December/003149.html) release.

## Proposal

The interleaved schedule proposals for modularity and Java 9 look as follows:

-   **2016-01** — JSR 376: Early Draft Review
-   **2016-05** — JDK 9: Feature Complete
-   **2016-06** — JSR 376: Public Review
-   **2016-08** — JDK 9: Rampdown Start
-   **2016-10** — JDK 9: Zero Bug Bounce
-   **2016-12** — JSR 376: Proposed Final Draft
-   **2016-12** — JDK 9: Rampdown Phase 2
-   **2017-01** — JDK 9: Final Release Candidate
-   **2017-03** — JSR 376: Final Release
-   **2017-03** — JDK 9: General Availability

The definition for the JDK 9 milestones are [the same as for JDK 8](http://openjdk.java.net/projects/jdk8/milestones#definitions) and worth a read.
Especially feature complete, which means that "\[a\]ll features have been implemented and integrated into the master forest, together with unit tests." It does not mean that development stops.
Instead reckless improvements are still possible, at least until rampdown phases start, in which "increasing levels of scrutiny are applied to incoming changes."

The proposals are up for debate until December 8th but I'd be very surprised to not see them become the new schedule.

## Reason

The reasons for this delay clearly are JSR 376 and Jigsaw:

> In the current JDK 9 schedule [\[7\]](http://openjdk.java.net/projects/jdk9/) the Feature Complete milestone is set for 10 December, less than two weeks from today, but Jigsaw needs more time.
The JSR 376 EG has not yet published an Early Draft Review specification, the volume of interest and the high quality of the feedback received over the last two months suggests that there will be much more to come, and we want to ensure that the maintainers of the essential build tools and IDEs have adequate time to design and implement good support for modular development.
>
> [Mark Reinhold - 1 Dec 2015](http://mail.openjdk.java.net/pipermail/jdk9-dev/2015-December/003149.html)

This makes a lot of sense.
And while I think highly of the current prototype there is still lots of work to do.
The additional six months will give the engineers more time to address the various problems and improve migration compatibility.

> As with previous schedule changes, the intent here is not to open the gates to a flood of new features unrelated to Jigsaw, nor to permit the scope of existing features to grow without bound.
It would be best to use the additional time to stabilize, polish, and fine-tune the features that we already have rather than add a bunch of new ones.
The later FC milestone does apply to all features, however, so reasonable proposals to target additional JEPs to JDK 9 will be considered so long as they do not add undue risk to the overall release.
>
> [Mark Reinhold - 1 Dec 2015](http://mail.openjdk.java.net/pipermail/jdk9-dev/2015-December/003149.html)

The additional time might help in convincing some of the more critical members of the community.
As it currently stands there are even members of the JSR 376 expert group which are openly opposing the path Jigsaw took.
The common counter proposal is solely based on class loaders as used by, e.g., OSGi implementations.
