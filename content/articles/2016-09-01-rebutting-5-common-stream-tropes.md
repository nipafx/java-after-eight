---
title: "Rebutting 5 Common Stream Tropes"
tags: [java-8, rant, streams]
date: 2016-09-01
slug: rebutting-5-common-java-stream-tropes
description: "Articles about Java 8's streams often repeat a bunch of tropes: succinctness, ugly mechanics, anemic pipelines, weak exception handling. This is a rebuttal!"
intro: "Articles about Java streams often repeat a bunch of tropes: streams are for succinctness, ugly mechanics are the norm, anemic pipelines, magic collectors, and weak exception handling. This is a rebuttal to all of them!"
searchKeywords: "stream"
featuredImage: rebutting-stream-tropes
---

I've just finished reading ["1 Exception To The Power of JDK 8 Collectors"](https://www.azul.com/exception-power-collectors-jdk-8/) and I have to say that I am pretty disappointed.
Simon Ritter, [Java champion](https://blogs.oracle.com/java/new-java-champion-simon-ritter), former Java evangelist at Oracle, and now Deputy CTO at [Azul Systems](https://www.azul.com/) (the guys with [the cool JVM](https://www.azul.com/products/zing/)), wrote it so I expected some interesting insights into streams.
Instead the post comes down to:

-   use streams to reduce line count
-   you can do fancy stuff with collectors
-   exceptions in streams suck

Not only is this superficial, the article also employs a handful of substandard development practices.
Now, Simon writes that this is just for a small demo project, so I guess he didn't pour all his expertise into it.
Still, it is sloppy and - and this is worse - many people out there make the same mistakes and repeat the same tropes.

Seeing them being recited in many different places (even if the respective authors might not defend these points when pressed), is surely not helping developers to get a good impression of how to use streams.
So I decided to take this occasion and write a rebuttal - not only to this post but to all that repeat any of the five tropes I found in it.

<admonition type="note">

This post, and especially the introduction above, came out a little harsh.
My intention was not to piss people off and if I were to write it again, I'd try to find a way to make my points just as forcefully but without the harshness.
I considered editing the post but in the end I think it is not that hurtful.
Feel free to judge me or reply in kind.

</admonition>

(Always pointing out that something is my opinion is redundant \[it's my blog, after all\] and tiresome, so I won't do it.
Keep it in mind, though, because I say some things like they were facts even though they're only my point of view.)

## The Problem

There's a lot of explanations of what's going on and why but in the end, it comes down to this: We have a query string from an HTTP POST request and want to parse the parameters into a more convenient data structure.
For example, given a string `a=foo&b=bar&a=fu` we want to get something like `a~>{foo,fu} b~>{bar}`.

We also have some code we found online that already does this:

```java
private void parseQuery(String query, Map parameters)
		throws UnsupportedEncodingException {

	if (query != null) {
		String pairs[] = query.split("[&]");

		for (String pair : pairs) {
			String param[] = pair.split("[=]");
			String key = null;
			String value = null;

			if (param.length > 0) {
				key = URLDecoder.decode(param[0],
					System.getProperty("file.encoding"));
			}

			if (param.length > 1) {
				value = URLDecoder.decode(param[1],
					System.getProperty("file.encoding"));
			}

			if (parameters.containsKey(key)) {
				Object obj = parameters.get(key);

				if(obj instanceof List) {
					List values = (List)obj;
					values.add(value);
				} else if(obj instanceof String) {
					List values = new ArrayList();
					values.add((String)obj);
					values.add(value);
					parameters.put(key, values);
				}
			} else {
				parameters.put(key, value);
			}
		}
	}
}
```

I assume it is kindness that the author's name is not mentioned because this snippet is wrong on so many levels that we will not even discuss it.

## My Beef

From here on, the article explains how to refactor towards streams.
And this is where I start do disagree.

### Streams For Succinctness

This is how the refactoring is motivated:

> Having looked through this I thought I could \[...\] use streams to make it a bit more succinct.

I hate it when people put that down as the first motivation to use streams!
Seriously, we're Java developers, we are used to writing a little extra code if it improves readability.

So streams are not about succinctness.
On the contrary, we're so used to loops that we're often cramming a bunch of operations into the single body line of a loop.
When refactoring towards streams I often split the operations up, thus leading to *more* lines.

<pullquote>Streams are not about succinctness</pullquote>

Instead, the magic of streams is how they support mental pattern matching.
Because they use only a handful of concepts (mainly map/flatMap, filter, reduce/collect/find), I can quickly see what's going and focus on the operations, preferably one by one.

```java
for (Customer customer : customers) {
	if (customer.getAccount().isOverdrawn()) {
		WarningMail mail = WarningMail.createFor(customer.getAccount());
		// do something with mail
	}
}

customers.stream()
	.map(Customer::getAccount)
	.filter(Account::isOverdrawn)
	.map(WarningMail::createFor)
	.forEach(/* do something with mail */ );
```

In code, it is much easier to follow the generic "customers map to accounts filter overdrawn ones map to warning mails", then the convoluted "create a warning mail for an account that you got from a customer but only if it is overdrawn".

But why would this be a reason to complain?
Everybody has his or her own preferences, right?
Yes, but focusing on succinctness leads to bad design decisions.

For example, I often decide to summarize one or more of operations (like successive maps) by creating a method for it and using a method reference.
This can have different benefits like keeping all of the operations in my stream pipeline on the same level of abstraction or simply naming operations that would otherwise be harder to understand (you know, intention revealing names and stuff).
If I focus on succinctness I might not do this.

Aiming for fewer lines of code can also lead to combining several operations into a single lambda just to save a couple of maps or filters.
Again, this defeats the purpose behind streams!

So, when you see some code and think about refactoring it to streams, don't count lines to determine your success!

### Using Ugly Mechanics

The first thing the loop does is also the way to start off the stream: We split the query string along ampersands and operate on the resulting key-value-pairs.
The article does it as follows:

```java
Arrays.stream(query.split("[&]"))
```

Looking good?
Honestly, no.
I know that this is the best way to create the stream but just because we have to *do* it this way does not mean we have to *look* at it.
And what we're doing here (splitting a string along a regex) seems pretty general, too.
So why not push it into a utility function?

```java
public static Stream<String> splitIntoStream(String s, String regex) {
	return Arrays.stream(s.split(regex));
}
```

Then we start the stream with `splitIntoStream(query, "[&]")`.
A simple ["extract method"-refactoring](http://refactoring.com/catalog/extractMethod.html) but so much better.

### Suboptimal Data Structures

Remember what we wanted to do?
Parse something like `a=foo&b=bar&a=fu` to `a~>{foo,fu} b~>{bar}`.
Now, how could we possibly represent the result?
It looks like we're mapping single strings to many strings, so maybe we should try a `Map<String, List<String>>`?

That is definitely a good first guess... But it is by no means the best we can do!
First of all, why is it a list?
Is order really important here?
Do we need duplicated values?
I'd guess no on both counts, so maybe we should try a set?

Anyways, if you ever created a map where values are collections, you know that this is somewhat unpleasant.
There is always this edge case of "is this the first element?" to consider.
Although Java 8 made that a little less cumbersome...

```java
public void addPair(String key, String value) {
	// `map` is a `Map<String, Set<String>>`
	map.computeIfAbsent(key, k -> new HashSet<>())
			.add(value);
}
```

... from an API perspective it is still far from perfect.
For example, iterating or streaming over all values is a two-step process:

```java
private <T> Stream<T> streamValues() {
	// `map` could be a `Map<?, Collection<T>>`
	return map
			.values().stream()
			.flatMap(Collection::stream);
}
```

Bleh!

Long story short, we're shoehorning what we need (a map from keys to many values) into the first thing we came up with (a map from keys to single values).
That's not good design!

Especially since there's a perfect match for our needs: [Guava's Multimap](https://github.com/google/guava/wiki/NewCollectionTypesExplained#multimap).
Maybe there's a good reason not to use it but in that case it should at least be mentioned.
After all, the article’s quest is to find a good way to process and represent the input, so it should do a good job in picking a data structure for the output.

(While this is a recurring theme when it comes to design in general, it is not very stream specific.
I didn't count it into the 5 common tropes but still wanted to mention it because it makes the final result much better.)

### Corny Illustrations

Speaking of common tropes... One is to use a corny photo of a stream to give the post some color.
With this, I am happy to oblige!

<contentimage slug="rebutting-stream-tropes" options="sidebar"></contentimage>

### Anemic Pipelines

Did you ever see a pipeline that does almost nothing but then suddenly crams all functionality into a single operation?
The article's solution to our little parsing problem is a perfect example (I removed some null handling to improve readability):

```java
private Map<String, List<String>> parseQuery(String query) {
	return Arrays.stream(query.split("[&]"))
		.collect(groupingBy(s -> (s.split("[=]"))[0],
				mapping(s -> (s.split("[=]"))[1], toList())));
}
```

Here's my thought process when reading this: "Ok, so we split the query string by ampersands and then, JESUS ON A FUCKING STICK, what's that?!" Then I calm down and realize that there's an abstraction hiding here - it is common not to pursue it but let's be bold and do just that.

In this case we split a request parameter `a=foo` into `[a, foo]` and process both parts separately.
So shouldn't there be a step in the pipeline where the stream contains this pair?

But this is a rarer case.
Far more often the stream's elements are of some type and I want to enrich it with other information.
Maybe I have a stream of customers and want to pair it with the city they live in.
Note that I do not want to *replace* the customers with cities - that's a simple `map` - but need both, for example to map cities to the customers living therein.

What have both cases in common?
They need to represent a pair.
Why don't they?
Because Java has no idiomatic way to do it.
Sure, you can use an array (works well for our request parameters), a [Map.Entry](https://docs.oracle.com/javase/8/docs/api/java/util/Map.Entry.html), some library's tuple class, or even something domain specific.
But few people do, which makes code that *does* do it stand out by being a little surprising.

<pullquote>Properly representing intermediate results is a boon to readability.</pullquote>

Still, I prefer it that way.
Properly representing intermediate results is a boon to readability.
Using `Entry` it looks like this:

```java
private Map<String, List<String>> parseQuery(String query) {
	return splitIntoStream(query, "[&]")
			.map(this::parseParameter)
			.collect(groupingBy(Entry::getKey,
					mapping(Entry::getValue, toList())));
}

private Entry<String, String> parseParameter(String parameterString) {
	String[] split = parameterString.split("[=]");
	// add all kinds of verifications here
	return new SimpleImmutableEntry<>(split[0], split[1]);
}
```

We still have that magic collector to deal with but at least a little less is happening there.

### Collector Magic

Java 8 ships with some crazy [collectors](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html) (particularly those that forward to downstream collectors) and we already saw how they can be misused to create unreadable code.
As I see it, they mostly exist because without tuples, there is no way to prepare complex reductions.
So here's what I do:

-   I try to make the collector as simple as possible by properly preparing the stream's elements (if necessary, I use tuples or domain specific data types for that).
-   If I still have to do something complicated, I stick it into a utility method.

Eating my own dog food, what about this?

```java
private Map<String, List<String>> parseQuery(String query) {
	return splitIntoStream(query, "[&]")
			.map(this::parseParameter)
			.collect(toListMap(Entry::getKey, Entry::getValue));
}

/** Beautiful JavaDoc comment explaining what the collector does. */
public static <T, K, V> Collector<T, ?, Map<K, List<V>>> toListMap(
		Function<T, K> keyMapper, Function<T, V> valueMapper) {
	return groupingBy(keyMapper, mapping(valueMapper, toList()));
}
```

It's still hideous - although less so - but at least I don't have to look at it all the time.
And if I do, the return type and the [contract comment](taxonomy-comments#contracts) will make it much easier to understand what's going on.

Or, if we decided to use the Multimap, we shop around for [a matching collector](http://stackoverflow.com/a/23003630/2525313):

```java
private Multimap<String, String> parseQuery(String query) {
	return splitIntoStream(query, "[&]")
			.map(this::parseParameter)
			.collect(toMultimap(Entry::getKey, Entry::getValue));
}
```

In both cases we could even go one step further and make a special case for streams of entries.
I'll leave that as an exercise to you.
:)

### Exception Handling

The article culminates in the biggest challenge when working with streams: exception handling.
It says:

> Unfortunately, if you go back and look at the original code you will see that I’ve conveniently left out one step: using URLDecoder to convert the parameter strings to their original form.

The problem is that `URLDecoder::decode` throws the checked `UnsupportedEncodingException`, so it is not possible to simply add it to the code.
So which approach to this relevant problem does the article take?
The [ostrich one](https://media.licdn.com/mpr/mpr/p/8/000/27b/04f/39a2a5c.jpg):

> In the end, I decided to keep my first super-slim approach.
Since my web front end wasn’t encoding anything in this case my code would still work.

Eh... Doesn't the article's title mention exceptions?
So shouldn't it spend a little more thought on this?

Anyways, error handling is always tough and streams add some constraints and complexity.
Discussing the different approaches takes time and, ironically, I'm not keen on squeezing it into a post's final sections.
So let's defer a detailed discussion about how to use runtime exceptions, trickery, or monads to address the problem and instead look at the simplest solution.

The simplest thing for an operation to do is to sift out the elements that cause trouble.
So instead of mapping each element to a new one, the operation would map from a single element to either zero or one element.
In our case:

```java
private static Stream<Entry<String, String>> parseParameter(
		String parameterString) {
	try {
		return Stream.of(parseValidParameter(parameterString));
	} catch (IllegalArgumentException | UnsupportedEncodingException ex) {
		// we should probably log the exception here
		return Stream.empty();
	}
}

private static Entry<String, String> parseValidParameter(
		String parameterString)
		throws UnsupportedEncodingException {
	String[] split = parameterString.split("[=]");
	if (split.length != 2) {
		throw new IllegalArgumentException(/* explain what's going on */);
	}
	return new SimpleImmutableEntry<>(
			URLDecoder.decode(split[0], ENCODING),
			URLDecoder.decode(split[1], ENCODING));
}
```

We then use `parseParameter` in a `flatMap` instead of a `map` and get a stream of those entries that could be split and decoded (and a bunch of log messages telling us in which cases things went wrong).

## Showdown

Here's the article's final version:

```java
private Map<String, List> parseQuery(String query) {
	return (query == null) ? null : Arrays.stream(query.split("[&]"))
		.collect(groupingBy(s -> (s.split("[=]"))[0],
				mapping(s -> (s.split("[=]"))[1], toList())));
}
```

The summary says:

> The takeaway from this is that using streams and the flexibility of collectors it is possible to greatly reduce the amount of code required for complex processing.
The drawback is this doesn’t work quite so well when those pesky exceptions rear their ugly head.

Here's mine:

```java
private Multimap<String, String> parseQuery(String query) {
	if (query == null)
		return ArrayListMultimap.create();
	return splitIntoStream(query, "[&]")
			.flatMap(this::parseParameter)
			.collect(toMultimap(Entry::getKey, Entry::getValue));
}

// plus `parseParameter` and `parseValidParameter` as above

// plus the reusable methods `splitIntoStream` and `toMultimap`
```

More lines, yes, but the stream pipeline has much less technical mumbo-jumbo, a full feature-set by URL-decoding the parameters, acceptable (or at least existing) exception handling, proper intermediate results, a sensible collector, and a good result type.
And it comes with two universal utility functions that help other devs improve their pipelines.
I think the few extra lines are worth all that.

So my takeaway is a little different: Use streams to make your code reveal its intentions by using streams' building blocks in a simple and predictable manner.
Take the chance to look for reusable operations (particularly those that create or collect streams) and don't be shy about calling small methods to keep the pipeline readable.
Last but not least: ignore line count.

What do you think?
Am I way off?
A nitpicking asshole?
Or right on target?
Leave a comment and tell me.
If you accidentally agree, you might want to share this post with your friends and followers.

And if you like what I'm writing about, why don't you follow me?

## Post Scriptum

By the way, with [Java 9's enhancements to the stream API](java-9-stream), we don't have to special-case a null query string:

```java
private Multimap<String, String> parseQuery(String query) {
	return Stream.ofNullable(query)
			.flatMap(q -> splitIntoStream(q, "[&]"))
			.flatMap(this::parseParameter)
			.collect(toMultimap(Entry::getKey, Entry::getValue));
}
```

Can't wait!
