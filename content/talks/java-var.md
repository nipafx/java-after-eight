---
title: "Fun With `var`"
tags: [anonymous-classes, default-methods, generics, lambda, java-10, var]
date: 2018-11-08
slug: talk-java-var
description: "A live-coding talk where I show off all you need to know about `var` in Java. And then some."
searchKeywords: "java var"
featuredImage: java-var
slides: https://slides.nipafx.dev/java-var
videoSlug: java-var-jfall-2018
repo: java-x-demo
---

Since Java 10 you can use `var` to let the compiler infer a local variable's type:

```java
var users = new ArrayList<User>();
```

And that's pretty much it, right?
Surprisingly, no!
There are a lot of details to consider...

* is this JavaScript?!
* how exactly is the type inferred?
* where can I use `var` and what should I look out for?
* won't this lead to unreadable code?

... and a few fun things to do with `var`...

* playing with anonymous classes (don't!)
* faking traits (don't!)
* faking intersection types (do!)

After this live-coding deep dive into `var`, you'll know all about Java 10's flagship feature.
