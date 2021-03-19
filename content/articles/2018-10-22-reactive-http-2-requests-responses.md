---
title: "Reactive HTTP/2 Requests And Responses In Java 11"
tags: [java-11]
date: 2018-10-22
slug: java-reactive-http-2-requests-responses
description: "With Java 11's new reactive HTTP/2 API, request and response bodies can be handled with reactive streams: you can throttle, stream, and cancel early."
intro: "With Java 11's new reactive HTTP/2 API, request and response bodies can be handled with reactive streams, which gives you full control over the bytes going over the wire: you can throttle, stream, and even cancel early."
searchKeywords: "reactive"
featuredImage: http2-reactive
repo: java-x-demo
---

With [Java 11's new HTTP API](java-http-2-api-tutorial) you can do more than just HTTP/2 and [asynchronous requests](java-http-2-api-tutorial#asynchronous-http-request-handling) - you can also handle request and response bodies in a reactive manner, which gives you full control over the bytes going over the wire: You can throttle, you can stream (to conserve memory), and you can expose a result as soon as you found it (instead of waiting for the entire body to arrive).

In this post we're going to look at streaming request and response bodies and because that requires a working understanding of reactive streams (introduced in Java 9 as *Flow API*), we're going to quickly discuss them as well - if you already know how they work skip ahead to [*Streaming The Request Body*](#streaming-the-request-body).
That section builds a solution in several steps, where individual steps may contain bugs that you should not put into your code!
For a complete picture, please use [the sources on GitHub](https://github.com/nipafx/demo-java-x).

## Reactive Stream Crash Course

The HTTP/2 API uses [reactive streams](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/Flow.html) to handle request and response bodies.
In full force, reactive streams can be used to build pipelines that are similar to [Java 8 streams](tag:stream): Starting from a source, a bunch of operations are defined that process each item the source contains/emits.

There are some important differences, though, most notably how items are moved through the pipeline.
With Java 8 streams, the source *contains* the items and the terminal operation *pulls* them through the pipeline (think of a collection of tweets that you want to process).
In reactive streams, the source *generates* items and would like to *push* them through the pipeline (think of a Twitter API that emits tweets live and you want to process them) - "would like to" because subscribers can push back to make sure they're not overwhelmed.

<pullquote>In reactive streams, the source *generates* items and would like to *push* them through the pipeline</pullquote>

While reactive streams can be used to build powerful pipelines, the HTTP/2 API uses them in a much simpler manner.
Which is good because the JDK only contains the building blocks that you need to connect two steps of a larger pipeline - libraries like [RxJava](https://github.com/ReactiveX/RxJava) or [Project Reactor](https://projectreactor.io/) offer advanced functionality that builds on them.
Here are the three involved types:

-   `Publisher` produces items to consume and can be subscribed to.
(E.g. the HTTP response can publish bytes as they arrive.)
-   `Subscriber` subscribes to a publisher and offers methods `onNext` for new items to consume, `onError` for errors the publisher encounters, and `onComplete` for the publisher to call when it's done.
(E.g. a JSON parser can subscribe to the response.)
-   `Subscription` is the connection between publisher and subscriber and can be used to request items or cancel the subscription

The programmatic flow is as follows:

-   creation and subscription:
	-   you need a `Publisher pub` and a `Subscriber sub`
	-   call `pub.subscribe(sub)`
	-   `pub` creates `Subscription script` and calls `sub.onSubscription(script)`
	-   `sub` stores `script`
-   streaming:
	-   `sub` calls `script.request(n)` (where `n` is a positive `int`)
	-   `pub` calls `sub.onNext(item)` to push items (max `n` times)
	-   this continues for as long as publisher and subscriber want and there is no error
-   cancellation:
	-   `pub` may call `sub.OnError(err)` or `sub.onComplete()`
	-   `sub` may call `script.cancel()`

Got it?
Lets see it in action!

## Streaming The Request Body

If your POST/PUT/... request has a large body, you may not want to load it into memory in its entirety.
And with Java's new reactive HTTP API you don't have to!

When creating a POST request, for example, you need to provide the body, but you don't have to do that in the form of a `String` or `byte[]`.
Formally, you have to hand over a `BodyPublisher`, which is essentially a `Publisher<ByteBuffer>`, i.e.
it publishes blocks of bytes.
The HTTP request will then subscribe to that publisher and request bytes to send over the wire.

We can observe that behavior by creating [decorators](decorator-pattern-default-methods) for the interfaces `BodyPublisher`, `Subscriber`, and `Subscription` [that log to standard out](https://github.com/nipafx/demo-java-x/blob/master/src/main/java/org/codefx/demo/java11/api/http2/ReactivePost.java#L38-L59) and then inject them into the HTTP request builder:

```java
HttpClient client = HttpClient.newBuilder().build();
HttpRequest post = HttpRequest
	.newBuilder(POSTMAN_POST)
	// this is where the magic happens!
	.POST(new LoggingBodyPublisher(BodyPublishers.ofFile(LARGE_JSON)))
	.header("Content-Type", "application/json")
	.build();
HttpResponse<String> response = client
	.send(post, BodyHandlers.ofString());
```

The `POST` call is where the magic happens.
`LARGE_JSON` is a `Path` and `BodyPublishers::ofFile` creates a `BodyPublisher` for it that walks the file as needed.
We wrap it into the logging decorator and pass the request to the client.
Once the streaming starts, you can see the following output:

```shell
# the HTTP client created a subscriber and now registers it with the
# file publisher by calling `Publisher::subscribe`
[subscribe   ] Subscriber registered:
	jdk.internal.net.http.Http1Request$FixedContentSubscriber@70ede696
# the file publisher created the subscription and passes it to the
# HTTP client by calling `Subscriber::onSubscribe`
[onSubscribe ] Subscription registered:
	jdk.internal.net.http.PullPublisher$Subscription@4adbc393
# the "handshake" is complete and the HTTP client starts requesting
# items by calling `Subscription::request`
[request     ] Items requested: 1 ↺ 1
# the file publisher received the request for the first item and
# fulfills it by calling `Subscriber::onNext` on the HTTP client
# with a single `ByteBuffer` instance (of 16kb length)
[onNext      ] Bytes passed: 16384 ↺ 16384
# the `request/onNext` cycle continues
[request     ] Items requested: 1 ↺ 2
[onNext      ] Bytes passed: 16384 ↺ 32768
[request     ] Items requested: 1 ↺ 3
[onNext      ] Bytes passed: 16384 ↺ 49152
[... snip ...]
[request     ] Items requested: 1 ↺ 85
[onNext      ] Bytes passed: 16384 ↺ 1392640
# the file publisher realizes that there are no more bytes to
# publish and calls `Subscriber::onComplete` on the HTTP client
[onComplete  ] Publishing completed
```

The interesting part is that the `BodyPublisher` returned by `BodyPublishers::ofFile` is lazy (it never reads more than it has to fulfill the next request) and that the HTTP client will only request new bytes once the last ones were send over the wire.
That means no matter how large the file, you never need to store more than 16kb of it in memory.

It's easy to integrate with that logic and, as a more elaborate example, create a publisher that connects to a database and uses pagination to, at all times, only hold a little window of the entire result in memory while transforming it to a sensible representation and streaming it as part of a request body.

<pullquote>It’s easy to integrate with that logic</pullquote>

Another great use case for reactive streams is live-processing of the *response* body.
And that's up next!

## Streaming The Response Body

Where a `BodyPublisher` is in charge of publishing bytes that are sent over the wire, a `BodySubscriber<T>` subscribes to the bytes received as part of the response and collects them into an instance of type `T`.
The bytes come in the form of lists of byte buffers, meaning `BodySubscriber` extends `Subscriber<List<ByteBuffer>>`.
Implementing that means extracting bytes from buffers, being aware of charsets, deciding where to split the resulting string, and so forth... in short, it's a pain.
So we're not going to do it.

### Of BodyHandlers and BodySubscribers

Instead we can implement a `Subscriber<String>` and pass it to `BodyHandlers::fromLineSubscriber`:

```java
static CompletableFuture<Void> reactiveSearch(
		HttpClient client, URI url, String term) {
	HttpRequest request = HttpRequest.newBuilder(url).GET().build();
	// TODO: we need a subscriber
	Subscriber<String> stringFinder = // ...
	// TODO: we need to do something with the `CompletableFuture`
	client.sendAsync(request, BodyHandlers.fromLineSubscriber(finder));
	// TODO: we need to get the result out of the `stringFinder`
	//       and return it here as a `CompletableFuture`
	return // ...
}
```

What's a `BodyHandler`, though?
(He once again asked no one in particular, although the other passengers now regard him with a funny look.)

A `BodyHandler<T>` is in charge of evaluating the response's status code, HTTP version, and header lines and to create a `BodySubscriber<T>` for the response's bytes.
The generic type `T` indicates what these bytes will eventually be transformed to and determines the `T` in `HttpResponse<T>` and thus the return type of `HttpResponse::body`.
In [the HTTP/2 tutorial](java-http-2-api-tutorial) as well as in the earlier example for streaming the request body, we called `BodyHandlers::ofString` to get a `BodyHandler<String>`, which represents the entire response body as a single string, and passed it to the client's send methods.

This time around, we're going to call `BodyHandlers.fromLineSubscriber(Subscriber<String>)`, though, which gives us more to do but also more freedom: It wraps our subscriber into a `BodySubscriber<Void>` (Why `Void`?
Later!) that aggregates the lists of byte buffers to strings, takes them apart on newlines, and then expects our subscriber to handle these individual response lines.
In return we don't need to wait for the entire body to arrive before we can process it.

By now you know the protocol a subscriber has to follow, so lets quickly implement a bare-bones variant:

```java
private static class StringFinder implements Subscriber<String> {

	private final String term;
	private Subscription subscription;

	private StringFinder(String term) {
		this.term = term;
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}

	@Override
	public void onNext(String line) {
		// TODO: scan the line and, if found, expose positive result
		subscription.request(1);
	}

	@Override
	public void onError(Throwable ex) {
		// TODO: expose the error
	}

	@Override
	public void onComplete() {
		// entire body was processed, but term was not found;
		// TODO: expose negative result
	}

}
```

As you can see, `StringFinder` implements the reactive subscriber contract by storing the subscription, requesting items (in this case lines; one by one), and processing them.

### Exposing The Result With CompletableFuture

As you can also see, there are a few TODOs left - they all revolve around how to expose the result, which can either be:

-   positive: term found in body
-   negative: term not found in body
-   error: HTTP client reported an exception

We can cover these three use cases with a `CompletableFuture<Boolean>`:

```java
private static class StringFinder implements Subscriber<String> {

	private final CompletableFuture<Boolean> found =
		new CompletableFuture<>()

	// [... other fields, constructor, `onSubscribe` as above...]

	@Override
	public void onNext(String line) {
		if (!found.isDone() && line.contains(term))
			found.complete(true);
		subscription.request(1);
	}

	@Override
	public void onError(Throwable ex) {
		found.completeExceptionally(ex);
	}

	@Override
	public void onComplete() {
		found.complete(false);
	}

	public CompletableFuture<Boolean> found() {
		return found;
	}

}
```

(Remember, check [the demo project](https://github.com/nipafx/demo-java-x) for [the complete example](https://github.com/nipafx/demo-java-x/blob/master/src/main/java/org/codefx/demo/java11/api/http2/Http2Api.java).)

Let's plug `StringFinder` into `reactiveSearch` and see what we've got:

```java
static CompletableFuture<Void> reactiveSearch(
		HttpClient client, URI url, String term) {
	HttpRequest request = HttpRequest.newBuilder(url).GET().build();
	StringFinder finder = new StringFinder(term);
	// DANGER ZONE!
	client.sendAsync(request, BodyHandlers.fromLineSubscriber(finder));
	return finder
		.found()
		.exceptionally(__ -> false)
		.thenAccept(found -> System.out.println(
			"Completed " + url + " / found: " + found));
}
```

We pass the `StringFinder` to `fromLineSubscriber`, which wraps it into a `BodyHandler`, and then return the `CompletableFuture` our finder exposes.
Something's off, though: What's with the future returned by `sendAsync`?
Don't we need that as well?
Kinda... but we have to take a small detour to get there.

There's an overload of `fromLineSubscriber` with the semantics that, once our subscriber processed the entire body, its result is made available as the response's body.
Given a `Function<Subscriber<String>, T>` (called a `finisher`) that extracts the result, it creates a `BodyHandler<T>`, which leads to an `HttpResponse<T>`.

The `fromLineSubscriber` we called has different semantics, tough: The provided subscriber processes the body however it pleases and without having to make it available afterwards.
It hence returns a `BodyHandler<Void>`, leading to an `HttpResponse<Void>`, meaning `sendAsync` returns a `CompletableFuture` that completes when the response is fully processed but never exposes the body.

If that sounds like just another reason to ignore `sendAsync`'s return value, I've successfully led you down the same erroneous line of thought that I followed.
And I may even have published the post this way, would I not have worked on it 35'000 feet above ground without internet connection.

<pullquote>Never ignore sendAsync's return value!</pullquote>

### Handling Errors

While `StringFinder` properly handles errors by exposing them via its `CompletaleFuture`, these aren't all errors that can occur.
On the contrary, these are just the small subset of errors that may happen while the body is streamed from server to client (e.g. loss of connection).

But there are plenty of reasons why we don't even get to streaming the body!
Not being able to establish the connection, for example.
Because you're in the air, for example.
In which case `StringFinder` is never subscribed to anything, its `CompletableFuture` never completes, and waiting for it blocks forever.
Where did we go wrong?
Where do those kinds of errors surface?

Here's where the `CompletableFuture` that `sendAsync` returns comes back in.
It's the thing that exposes such errors!
And so we need to hook into its exception handling and make our finder's future complete with the same exception:

```java
StringFinder finder = new StringFinder(term);
client
	.sendAsync(request, BodyHandlers.fromLineSubscriber(finder))
	// this is a `CompletableFuture<Void>` ...
	.exceptionally(ex -> {
		finder.onError(ex);
		// ... which is why we need to return `null`
		return null;
	});
```

This way, the `CompletableFuture<Boolean>` returned by `StringFinder` surfaces all possible outcomes that can occur while fielding the HTTP request:

-   it will complete with `true` as soon as the term is found
-   it will complete with `false` if the entire body was scanned
-   it will complete with an exception if there is *any* problem (including those that occur before the body is streamed)

Neat!

Remember from [the last post](java-http-2-api-tutorial), that when searching the ten longest Wikipedia articles for "Foo", async calls took about 80% of the time that blocking calls took.
Streaming the body instead of putting it together in its entirety reduces memory footprint, which, as is common, results in longer run time.
Just, barely, though, it's still about 85% of the blocking calls.

## Canceling The Stream

Only one nit remains: We're always streaming the entire body, even after we found the search term.
Can't we abort the stream once we're done?
Technically, yes, and it isn't even complicated - all we need to do is add one line to `StringFinder::onNext`:

```java
@Override
public void onNext(String line) {
	if (line.contains(search.term())) {
		found.complete(true);
		subscription.cancel();
	}
	requestLine();
}
```

By canceling the subscription, we won't receive any more lines and this really shows when measuring run time: This takes about 45% of the time the blocking calls take, i.e.
about 55% of the asynchronous approach.
But keep in mind that this speedup highly depends on how soon the search term is found!
If you search for "Foobar" instead of "Foo", none of the ten sites contains it (what a shame), and performance is back to the runtime without cancellation.

Regarding cancellation, there are two more details that I should mention.
The first one is that canceling the subscription leads to the client calling `onError` with an exception like the following:

```shell
java.util.concurrent.CompletionException:
	java.io.IOException: Stream 47 cancelled
```

Since `onError` calls `found.completeExceptionally`, the future must already have been completed by then (or the result is always an error instead of `true`).
That's why `found.complete(true)` *must* come before `subscription.cancel()`!

Finally, here's what [the JavaDoc for `BodySubscriber` has to say about canceling the subscription](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.BodySubscriber.html):

> Calling `cancel` before exhausting the response body data may cause the underlying HTTP connection to be closed and prevent it from being reused for subsequent operations.

I'm no expert on HTTP and refrain from making any recommendations based on that quote.

## Reflection

With that it's time to wrap it up.
In short:

-   reactive streams have two active players: a publisher and a subscriber, where the former publishes items that the latter consumes
-   to stream a request body, you need a `Publisher<ByteBuffer>`, to which the the HTTP client will subscribe and then send requested bytes over the wire
-   to stream a response body, you will typically implement a `Subscriber<String>` that the client subscribes to the incoming response bytes, which it translates to a string and breaks a part line by line for the subscriber to consume
-   be careful to properly handle errors

You can be proud of yourself - learning about reactive streams and how Java's new HTTP/2 API uses it, is no easy feat.
👍 It becomes clearer if you play around with it yourself, so I want to point you to [the demo](https://github.com/nipafx/demo-java-x) one last time: clone it, play around with it, break it, fix it.
Best way to learn.
