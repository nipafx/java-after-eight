---
title: "JavaFX, Project Jigsaw and JEP 253"
tags: [java-next, java-9, javafx, project-jigsaw, patterns]
date: 2015-05-18
slug: javafx-project-jigsaw-jep-253
description: "JEP253 aims to prepare JavaFX for Project Jigsaw by defining public APIs for functionality that will become inaccessible due to modularization."
searchKeywords: "JavaFX Project Jigsaw"
featuredImage: jep253-jigsaw
---

So [Java 9 may break your code](how-java-9-and-project-jigsaw-may-break-your-code)...

This is particularly likely if your project uses JavaFX because many customizations and home-made controls require the use of internal APIs.
With [Project Jigsaw](http://openjdk.java.net/projects/jigsaw/) these will be unaccessible in Java 9.
Fortunately, [Oracle announced](http://mail.openjdk.java.net/pipermail/openjfx-dev/2015-May/017242.html) [JEP 253](http://openjdk.java.net/jeps/253) a couple of days ago.
Its goal:

> Define public APIs for the JavaFX UI controls and CSS functionality that is presently only available via internal APIs and will hence become inaccessible due to modularization.
>
> [JEP 253 - May 14 2015](http://openjdk.java.net/projects/jigsaw/)

Let's have a look at how JavaFX, Project Jigsaw and JEP 253 interact.

To better understand the role internal APIs play in JavaFX, it is helpful to know its control architecture, so we will start with that.
We will then look at why internal APIs are frequently used when working with JavaFX.
This will help put the new JEP in context.

Because I am familiar with it I will often refer to [ControlsFX](http://controlsfx.org/) as an example.
I assume that similar libraries (e.g. [JFXtras](http://jfxtras.org/)) as well as other projects which customize JavaFX are in the same situation.

## JavaFX Control Architecture

### Model-View-Controller

[JavaFX controls are implemented according to model-view-controller](https://wiki.openjdk.java.net/display/OpenJFX/UI+Controls+Architecture).
Without going into too much detail, let's have a quick look at how this is done.
(A great and more detailed explanation can be found [at the GuiGarage](http://www.guigarage.com/2012/11/custom-ui-controls-with-javafx-part-1/).)

All official controls extend the abstract class [`Control`](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Control.html).
This is MVC's model.

The control defines a [`skinProperty`](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Control.html#skinProperty), which contains a [`Skin`](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/Skin.html) implementation.
It visualizes the control's current state, i.e.
it is MVC's view.
By default, it is also in charge of capturing and executing user interaction, which in MVC is the controller's task.

The skin is most often implemented by extending `BehaviorSkinBase`.
It creates an implementation of `BehaviorBase` to which it delegates all user interaction and which updates the model accordingly.
So here we have MVC's controller.

### Key Bindings

It is also noteworthy how controls resolve user input.
In order to link an action to an input (e.g. "open new tab in background" for "CTRL + mouse click"), they create a list of `KeyBinding`s.
Input events are then compared to all created bindings and the correct action is called.

## Internal APIs in JavaFX

When working with JavaFX, it is common to rely on internal API.
This is done to create new controls, tweak existing ones or to fix bugs.

### Creating New Controls

While `Control`, `Skin` and even `SkinBase` are all public API the frequently used `BehaviorSkinBase` and `BehaviorBase` are not.
With Project Jigsaw, they will be unaccessible.

This API is heavily used, though.
ControlsFX contains about two dozen controls and roughly half of them require implementations of either of these classes.

Similarly, `KeyBinding`s are not published so creating them to manage user interaction adds another problematic dependency.

### Tweaking Existing Controls

Customizing an existing control usually happens to either change the visualization or to tweak the behavior for certain user interactions.

For the former it is often easiest to simply extend and modify the existing Skin.
Unfortunately all skins of existing controls live in `com.sun.javafx.scene.control.skin`.
When they become unaccessible, many customized controls will no longer compile.

To change a control's reaction to user interaction it is necessary to interfere with the behavior defined in `BehaviorBase`.
This is analog to creating a new control as it is often done by extending `BehaviorSkinBase` and `BehaviorBase` and creating new `KeyBinding`s.

### Making Controls Styleable Via CSS

In JavaFX controls can be implemented so that they are styleable via CSS.
All official controls come with this feature and some of those provided by other projects as well.

~~A central step in styling a control is to convert the attributes' textual representations from the CSS file to instances of `Number`, `Paint`, an enum, ... so they can be assigned to properties.
To ensure uniform, high quality conversion JavaFX provides an API for this.
Unfortunately it lives in `com.sun.javafx.css.converters`.~~

**Update (11th of June 2015)**:
*[As pointed out](javafx-project-jigsaw-jep-253)<!-- comment-2038193283 --> by [Michael](https://disqus.com/by/michaelennen/), it is not necessary to create the converters directly.
Instead the static factory methods on the published [`StyleConverter`](https://docs.oracle.com/javase/8/javafx/api/javafx/css/StyleConverter.html) should be used.
This makes the above paragraph moot.*

Advanced styling requirements must be implemented with help of the `StyleManager`, which, you guessed it, is also not published.

### Working Around Bugs

JavaFX is comparatively young and still contains some bugs which are not too hard to come in contact with.
Often the only work around is to hack into a control's inner workings and thus use private APIs.
(Examples for such cases can be found on the OpenJFX mailing list, e.g. in these mails by [Robert Krüger](http://mail.openjdk.java.net/pipermail/openjfx-dev/2015-April/017045.html), [Stefan Fuchs](http://mail.openjdk.java.net/pipermail/openjfx-dev/2015-April/017063.html) and [Tom Schindl](http://mail.openjdk.java.net/pipermail/openjfx-dev/2015-April/017043.html).)

Such workarounds will fail in Java 9.
Since it seems unlikely that they become unnecessary because all bugs are fixed, concerns like the following are understandable:

> Of course, in theory, if all of [those bugs] get fixed in [Java] 9 I am fine, but if there is a period of time where half of them are fixed in 9 and the other half can only be worked around on 8, what do I do with my product?
>
> [Robert Krüger - April 9 2015](http://mail.openjdk.java.net/pipermail/openjfx-dev/2015-April/017046.html)

## JEP 253

We have seen why the use of internal APIs is ubiquitous when working with JavaFX.
So how is [JEP 253](http://openjdk.java.net/jeps/253) going to solve this?

(Unless otherwise noted all quotes in this section are taken from the JEP.)

### Goals, Non-Goals and Success Metrics

The proposal addresses precisely the problem described up to this point.
And it recognizes that "\[i\]n many cases, to achieve a desired result, developers have no choice but to use these internal APIs".
So "\[t\]he goal of this JEP is to define public APIs for the functionality presently offered by the internal APIs".

(Note that this still entails compile errors while developers move their code from the internal and now unaccessible to the new public API.)

At the same time this JEP plans neither breaking changes nor enhancements to existing, published code: "All other existing APIs that are not impacted by modularization will remain the same."

Two success metrics are defined:

-   "Projects that depend on JavaFX internal APIs, in particular Scene Builder, ControlsFX, and JFXtras, continue to work after updating to the new API with no loss of functionality."
-   "Ultimately, if all works to plan, third-party controls should be buildable without any dependency upon internal APIs."

### Three Projects

The JEP is split into three projects:

**Project One: Make UI control skins into public APIs**:
Skins of existing controls will be moved from `com.sun.javafx.scene.control.skin` to `javafx.scene.control.skin`.
This will make them published API.
(Note that this does not include the behavior classes.)

**Project Two: Improve support for input mapping**:
Behavior will be definable by input mapping.
This allows to alter a control's behavior at runtime without requiring to extend any specific (and unpublished) classes.

**Project Three: Review and make public relevant CSS APIs**:
CSS API which is currently available in `com.sun.*` packages will be reviewed and published.

The proposal goes into more detail and describes the current state of each project as well as some risks and assumptions.

The projects address three out of the four use cases described above.
It is reasonable to assume that these can be fulfilled and that in Java 9 it will be possible to properly create, tweak and skin controls even though internal APIs are unaccessible.

What about working around bugs?
At least some of them seem to be solvable with the same tools (e.g. extending an existing skin).
But I can not say whether this is true for all of them and how crucial the ones which are left without a workaround are.

### Schedule

If you want to try out the new APIs, you'll have to be patient for a while.
In [a tweet](https://twitter.com/JonathanGiles/status/599671529786384384) Jonathan Giles, Oracle tech lead in the JavaFX UI controls team and owner of JEP 253, states that he "probably won't merge into the repo for a few months yet...".

On the other hand, since [feature completeness for Java 9 is scheduled for December](http://mail.openjdk.java.net/pipermail/jdk9-dev/2015-May/002172.html), it must be available within the next seven months.

## Reflection

We have seen that working with JavaFX often entails the use of private API.
This happens in four largely distinct areas:

-   Creating new controls according to the control architecture (MVC).
-   Tweaking existing controls by extending their skin or altering key bindings.
-   Making controls styleable via CSS.
-   Working around bugs.

JEP 253 is split into three projects which address the first three areas.
Whether they will suffice to enable working around bugs with only public API is unclear (to me).
