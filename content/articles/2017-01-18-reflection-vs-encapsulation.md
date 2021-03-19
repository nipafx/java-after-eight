---
title: "Reflection vs Encapsulation"
tags: [java-9, j_ms, project-jigsaw, reflection]
date: 2017-01-18
slug: java-modules-reflection-vs-encapsulation
canonicalUrl: https://www.sitepoint.com/reflection-vs-encapsulation-in-the-java-module-system
description: "Reflection wants to break into all code; encapsulation wants to give modules a safe space. How can this stand off be resolved?"
featuredImage: reflection-vs-encapsulation-module-system
repo: jigsaw-encapsulation
---

Historically reflection could be used to break into any code that ran in the same JVM.
With Java 9 this is going to change.
One of the two main [goals of the new module system](http://openjdk.java.net/projects/jigsaw/goals-reqs/) is strong encapsulation; giving modules a safe space into which no code can intrude.
These two techniques are clearly at odds so how can this stand off be resolved?
After considerable discussions it looks like the recent [proposal of open modules](http://mail.openjdk.java.net/pipermail/jpms-spec-experts/2016-October/000430.html) would show a way out.

If you're all down with the module system and what reflection does, you can skip the following back story and jump right into [the stand off](#the-stand-off).

## Setting the Scene

Let me set the scene of how [the module system](http://openjdk.java.net/projects/jigsaw/spec/sotms) implements strong encapsulation and how that clashes with reflection.

### Module Crash Course

The Java Platform Module Saloon (JPMS) is introducing the concept of modules, which in the end are just regular JARs with a module descriptor.
The descriptor is compiled from a `module-info.java` file that defines a module's name, its dependencies on other modules, and the packages it makes available:

```java
module some.module {

	requires some.other.module;
	requires yet.another.module;

	exports some.module.package;
	exports some.module.other.package;

}
```

In the context of encapsulation there are two points to take note of:

-   Only public types, methods, and fields in exported packages are accessible.
-   They are only accessible to modules that require the exporting module.

Here, "being accessible" means that code can be compiled against such elements and that the JVM will allow accessing them at run time.
So if code in module a _user_ depends on code in a module _owner_, all we need to do to make that work is have _user_ require _owner_ and have _owner_ export the packages containing the required types:

```java
module user {
	requires owner;
}

module owner {
	exports owner.api.package;
}
```

This is the common case and apart from making the dependencies and API explicit and known to the module system all works as we're used to.

So far everybody's having fun!
Then, in comes Reflection... conversations halt mid-sentence, the piano player stops his tune.

### Reflection

Before Java 9, reflection was allowed to break into any code.
Aside from some pesky calls to `setAccessible` every type, method, or field in any class could be made available, could be called, could be changed - hell, even final fields were not safe!

```java
Integer two = 2;

Field value = Integer.class.getDeclaredField("value");
value.setAccessible(true);
value.set(two, 3);

if (1 + 1 != two)
	System.out.println("Math died!");
```

This power drives all kinds of frameworks - starting with JPA providers like Hibernate, coming by testing libraries like JUnit and TestNG, to dependency injectors like Guice, and ending with obsessed class path scanners like Spring - which reflect over our application or test code to work their magic.
On the other side we have libraries that need something from the JDK that it would rather not expose (did anybody say `sun.misc.Unsafe`?).
Here as well, reflection was the answer.

So this guy, being used to getting what he wants, now walks into the Module Saloon and the bartender has to tell him no, not this time.

## The Stand Off

Inside the module system (let's drop the saloon, I think you got the joke) reflection could only ever access code in exported packages.
Packages internal to a module were off limits and this already caused [quite a ruckus](http://blog.dripstat.com/removal-of-sun-misc-unsafe-a-disaster-in-the-making/).
But it still allowed to use reflection to access everything else in an exported package, like package-visible classes or private fields and methods - this was called [_deep reflection_](http://mail.openjdk.java.net/pipermail/jpms-spec-experts/2016-October/000431.html).
[In September](http://mail.openjdk.java.net/pipermail/jpms-spec-experts/2016-September/000390.html) rules got even stricter!
Now deep reflection was forbidden as well and reflection was no more powerful than the statically typed code we would write otherwise: Only public types, methods, and fields in exported packages were accessible.

All if this caused a lot of discussions of course - some heated, some amicable but all with the sense of utter importance.

Some (myself included) favor strong encapsulation, arguing that modules need a safe space in which they can organize their internals without the risk of other code easily depending on it.
Examples I like to give are JUnit 4, where one big reason for [the rewrite](https://www.sitepoint.com/junit-5-state-of-the-union/) was that tools depended on implementation details, reflecting down to the level of private fields; and `Unsafe`, whose pending removal put a lot of pressure on a lot of libraries.

Others argue that the flexibility provided by reflection not only enables great usability for the many frameworks relying on it, where annotating some entities and dropping _hibernate.jar_ onto the class path suffices to make things work.
It also gives freedom to library users, who can use their dependencies the way they want, which might not always be the way the maintainers intended to.
Here, `Unsafe` comes in as an example for the other side: Many libraries and frameworks that are now critical to the Java ecosystem were only feasible exactly because some hacks were possible _without_ the JDK team's approval.

Even though I tend towards encapsulation, I see the other arguments' validity as well.
So what to do?
What choices to developers have besides encapsulating their internals and giving up on reflection?

## Choice of Weapons

So let's say we are in a position where we need to make a module's internals available via reflection.
Maybe to expose it to a library or framework module or maybe because we _are_ that other module and want to break into the first one.
In the rest of the article we'll explore all available choices, looking for answers to these questions:

-   What privileges do we need to employ that approach?
-   Who can access the internals?
-   Can they be accessed at compile time as well?

For this exploration we will create two modules.
One is called _owner_ and contains a single class `Owner` (in the package `owner`) with one method per visibility that does nothing.
The other, _intruder_, contains a class `Intruder` that has no compile time dependency on `Owner` but tries to call its methods via reflection.
Its code comes down to this:

```java
Class<?> owner = Class.forName("owner.Owner");
Method owned = owner.getDeclaredMethod(methodName);
owned.setAccessible(true);
owned.invoke(null);
```

The call to `setAccessible` is the critical part here, it succeeds or fails depending on how we decide to create and execute our modules.
In the end we get output as follows:

```{.lang:sh .highlight:0 .decode:true}
public: ✓   protected: ✗   default: ✗   private: ✗
```

(Here only the public method could be accessed.)

All the code I'm using here can be found in [a GitHub repository](https://github.com/nipafx/demo-jigsaw-reflection), including Linux scripts that run it for you.

### Regular Exports

This is the vanilla approach to expose an API: The module _owner_ simply exports the package `owner`.
To do this we need of course be able to change the owning module's descriptor.

```java
module owner {
	exports owner;
}
```

With this we get the following result:

```{.lang:sh .highlight:0 .decode:true}
public: ✓   protected: ✗   default: ✗   private: ✗
```

So far, err... not so good.
First of all, we only reached part of our goal because the intruding module can only access public elements.
And if we do it this way _all_ modules that depend on _owner_ can compile code against it and all modules can reflect over its internals.
Actually, they are no longer internals at all since we properly exported them - the package's public types are now baked into the module's API.

### Qualified Exports

If exports are vanilla, this is cranberry vanilla - a default choice with an interesting twist.
The owning module can export a package _to a specific module_ with what is called a _qualified export_:

```java
module owner {
	exports owner to intruder;
}
```

But the result is the same as with regular exports - the intruding module can only access public elements:

```{.lang:sh .highlight:0 .decode:true}
public: ✓   protected: ✗   default: ✗   private: ✗
```

Again, we reached only part of our goal, and again, we exposed the elements at compile time as well as at run time.
The situation improved in the sense that only the named module, _intruder_ in this case, is granted access but for that we accepted the necessity to actually know the module's name at compile time.

Knowing the intruding module might be amenable in the case of frameworks like Guice but as soon as the implementation hides behind an API (what the JDK team calls an _abstract reflective framework_; think JPA and Hibernate) this approach fails.
Independently of whether it _works_ or not, explicitly naming the intruding module in the owning module's descriptor can be seen as iffy.
On the other hand, chances are the owning module already depends on the intruding one anyways because it needs some annotations or something, in which case we're not making things much worse.

### Open Packages

Now it gets interesting.
A pretty recent addition to the module system is the ability for modules to open up packages at run time only.

```java
module owner {
	opens owner;
}
```

Yielding:

```{.lang:sh .highlight:0 .decode:true}
public: ✓   protected: ✓   default: ✓   private: ✓
```

Neat!
We killed two birds with one stone:

-   The intruding module gained deep access to the whole package, allowing it to use even private elements.
-   This exposure exists at run time only, so code can not be compiled against the package's content.

There is one downside, though: _All_ modules can reflect over the opened package, now.
Still, all in all much better than exports.

### Qualified Open Packages

As with exports and qualified exports, there exists a qualified variant of open packages as well:

```java
module owner {
	opens owner to intruder;
}
```

Running the program we get the same result as before but now only _intruder_ can achieve them:

```{.lang:sh .highlight:0 .decode:true}
public: ✓   protected: ✓   default: ✓   private: ✓
```

This presents us with the same trade-off as between exports and qualified exports and also doesn't work for a separation between API and implementation.
But there's hope!

In November Mark Reinhold [proposed](http://mail.openjdk.java.net/pipermail/jpms-spec-experts/2016-November/000457.html) a mechanism that would allow code in the module to which a package was opened up to transfer that access to a third module.
Coming back to JPA and Hibernate this solves that problem exactly.
Assume the following module descriptor for _owner_:

```java
module owner {
	// the JPA module is called java.persistence
	opens owner to java.persistence;
}
```

In this case the mechanism could be employed as follows (quoted almost verbatim from the proposal):

> A JPA entity manager is created via one of the [`Persistence::createEntityManagerFactory` methods](http://docs.oracle.com/javaee/7/api/javax/persistence/Persistence.html#createEntityManagerFactory-java.lang.String-), which locate and initialize a suitable persistence provider, say Hibernate.
> As part of that process they can use the `addOpens` method on the client module _owner_ to open the `owner` package to the Hibernate module.
> This will work since the `owner` module opens that package to the `java.persistence` module.

There is also a variant for containers to open packages to implementations.
In the current EA build (b146) this feature does not seem to be implemented yet, though, so I couldn't try it out.
But it definitely looks promising!

### Open Modules

If open packages were a scalpel, open modules are a cleaver.
With it a module relinquishes any control over who accesses what at run time and opens up all packages to everybody as if there were an `opens` clause for each of them.

```java
open module owner { }
```

This results in the same access as individually opened packages:

```{.lang:sh .highlight:0 .decode:true}
public: ✓   protected: ✓   default: ✓   private: ✓
```

Open modules can be considered an intermediate step on the migration path from JARs on the class path to full-blown, strongly encapsulating modules.

### Class Path Trickery

Now we're entering less modular ground.
As you might know `java` and `javac` require modules to be on the module path, which is like the class path but for modules.
But the class path is not going away and neither are JARs.
There are two tricks we can employ if we have access to the launching command line _and_ can push the artifact around (so this won't work for JDK modules).

#### Unnamed Module

First, we can drop the owning module onto the class path.

How does the module system react to that?
Since everything needs to be a module the module system simply creates one, the _unnamed module_, and puts everything in it that it finds on the class path.
Inside the unnamed module everything is much like it is today and JAR hell continues to exist.
Because the unnamed module is synthetic, the JPMS has no idea what it might export so it simply exports everything - at compile and at run time.

If any JAR on the class path should accidentally contain a module descriptor, this mechanism will simply ignore it.
Hence, the owning module gets demoted to a regular JAR and its code ends up in a module that exports everything:

```{.lang:sh .highlight:0 .decode:true}
public: ✓   protected: ✓   default: ✓   private: ✓
```

Ta-da!
And without touching the owning module, so we can do this to modules we have no control over.
Small caveat: We can not require the unnamed module so there is no good way to compile against the code in the owning module from other modules.
Well, maybe the caveat is not so small after all...

#### Automatic Module

The second approach is to strip the owning module of its descriptor and _still_ put it on the module path.
For each regular JAR on the module path the JPMS creates a new module, names it automatically based on the file name, and exports all its contents.
Since all is exported, we get the same result as with the unnamed module:

```{.lang:sh .highlight:0 .decode:true}
public: ✓   protected: ✓   default: ✓   private: ✓
```

Nice.
The central advantage of automatic modules over the unnamed module is that modules _can_ require it, so the rest of the application can still depend on and compile against it, while the intruder can use reflection to access its internals.

One downside is that the module's internals become available at run time to every other module in the system.
Unfortunately, the same is true at compile time unless we manage to compile against the proper owning module and then rip out its descriptor on the way to the launch pad.
This is iffy, tricky, and error-prone.

### Command Line Escape Hatches

Since we're fiddling with the command line anyway, there is a cleaner approach (maybe I should've told you about it earlier): Both `javac` and `java` come with a new flag `--add-opens`, which opens additional packages.

```{.lang:sh .highlight:0 .decode:true}
java
	--module-path mods
	--add-modules owner
	--add-opens owner/owner=intruder
	--module intruder
```

This works without changing the owning module and applies to JDK modules as well.
So yeah, much better than the unnamed and automatic module hacks.

### Encapsulation Kill Switch

If all of this seems too cumbersome and you just want to get it working for your class path application, `--permit-illegal-access`, [amicably dubbed the encapsulation "kill switch"](http://mail.openjdk.java.net/pipermail/jigsaw-dev/2017-March/011763.html), is the right command line argument for you.
With it, all code in the unnamed module can access other types regardless of any limitations that strong encapsulation imposes.
In exchange you get warnings on illegal accesses and the stifling feeling of living on burrowed time: The option will only exist in Java 9 to ease migration, so you just bought yourself two, three years.

Here's how to launch with owner on the module and intruder on the class path:

```{.lang:sh .highlight:0 .decode:true}
java
	--class-path mods/intruder.jar
	--module-path mods/owner.jar
	--add-modules owner
	--permit-illegal-access
	intruder.Intruder
```

And here's the result - note how the warnings screw with my carefully created output:

```{.lang:sh .highlight:0 .decode:true}
WARNING: --permit-illegal-access will be removed in the next major release
public:WARNING: Illegal access by intruder.Intruder (file:mods/intruder.jar) to method owner.Owner.publicMethod() (permitted by --permit-illegal-access)
 ✓   protected:WARNING: Illegal access by intruder.Intruder (file:mods/intruder.jar) to method owner.Owner.protectedMethod() (permitted by --permit-illegal-access)
 ✓   default:WARNING: Illegal access by intruder.Intruder (file:mods/intruder.jar) to method owner.Owner.defaultMethod() (permitted by --permit-illegal-access)
 ✓   private:WARNING: Illegal access by intruder.Intruder (file:mods/intruder.jar) to method owner.Owner.privateMethod() (permitted by --permit-illegal-access)
 ✓
```

As I've said, this only works if the intruder comes from the unnamed module.
The rationale behind that is that properly modularized code, meaning code that lives in modules, should not need such hacks.
Indeed, if we put both artifacts on the module path, we can see that the flag is not exercised (no warnings beyond the initial one) and access fails because the internals of `owner` are strongly encapsulated:

```{.lang:sh .highlight:0 .decode:true}
java
	--module-path mods
	--add-modules owner
	--permit-illegal-access
	--module intruder

WARNING: --permit-illegal-access will be removed in the next major release
public: ✗   protected: ✗   default: ✗   private: ✗
```

## Summary

Ookey, still remember everything we did?
No?
Executive summary table to the rescue!

<table style="width: 1500px;">
	<tbody>
		<tr>
			<th style="width: 15%;">mechanism</th>
			<th style="width: 15%;">access</th>
			<th style="width: 20%;">compile access</th>
			<th style="width: 20%;">reflection access</th>
			<th style="width: 30%;">comments</th>
		</tr>
		<tr>
			<td>export</td>
			<td>descriptor</td>
			<td>all code ~&gt; public</td>
			<td>all code ~&gt; public</td>
			<td>makes API public</td>
		</tr>
		<tr>
			<td>qualified export</td>
			<td>descriptor</td>
			<td>specified modules ~&gt; public</td>
			<td>specified modules ~&gt; public</td>
			<td>need to know intruding modules</td>
		</tr>
		<tr>
			<td>open package</td>
			<td>descriptor</td>
			<td>none</td>
			<td>all code ~&gt; private</td>
			<td></td>
		</tr>
		<tr>
			<td>qualified open package</td>
			<td>descriptor</td>
			<td>none</td>
			<td>specified modules ~&gt; private</td>
			<td>can be transfered to implementation modules</td>
		</tr>
		<tr>
			<td>open module</td>
			<td>descriptor</td>
			<td>none</td>
			<td>all code ~&gt; private</td>
			<td>one keyword to open all packages</td>
		</tr>
		<tr>
			<td>unnamed module</td>
			<td>command line</td>
			<td>all non-modules ~&gt; public</td>
			<td>all code ~&gt; private</td>
			<td></td>
		</tr>
		<tr>
			<td>automatic module</td>
			<td>command line and artifact</td>
			<td>all code ~&gt; public</td>
			<td>all code ~&gt; private</td>
			<td>requires fiddling with the artifact</td>
		</tr>
		<tr>
			<td>command line flag</td>
			<td>command line</td>
			<td>none</td>
			<td>all code ~&gt; private</td>
			<td></td>
		</tr>
	</tbody>
</table>

Wow, we really went through quite a number of options!
But now you know what to do if you're faced with the task to break into a module with reflection.
In summary, I think the vast majority of use cases can be covered by answering one question:

Is it your own module?

-   Yes ⇝ Open packages (maybe qualified) or, if there are too many, the entire module.
-   No ⇝ Use the command line flag `--add-opens`.
