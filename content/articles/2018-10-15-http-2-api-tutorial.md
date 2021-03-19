---
title: "Java 11 HTTP/2 API Tutorial"
tags: [java-11, java-basics]
date: 2018-10-15
slug: java-http-2-api-tutorial
description: "Tutorial for Java 11's new HTTP/2 API with HttpClient, HttpRequest, and HttpResponse at its center. Shows synchronous and asynchronous request handling."
intro: "Java 11 ships with a new, fluent HTTP/2 API. This tutorial explains the basic building blocks and how to send requests synchronously and asynchronously."
searchKeywords: "HTTP/2"
featuredImage: http2-basics
repo: java-x-demo
---

Since [Java 11](tag:java-11), the JDK contains a new HTTP API in `java.net.http` with `HttpClient`, `HttpRequest`, and `HttpResponse` as its principal types.
It's a fluent, easy-to-use API that fully supports [HTTP/2](https://en.wikipedia.org/wiki/HTTP/2), allows you to handle responses asynchronously, and can even send and receive bodies in a reactive manner.
In this post, I introduce you to the new API and show you how to send synchronous and asynchronous requests.
Reactive requests and responses are reserved for [the next post](java-reactive-http-2-requests-responses)	.

## The Building Blocks

In a nutshell, sending a request and receiving a response follows these steps:

1. use a builder to create an immutable, reusable `HttpClient`
2. use a builder to create an immutable, reusable `HttpRequest`
3. pass the request to the client to receive an `HttpResponse`

Right off the bat, I love the focus on immutability!
You can configure clients and requests wherever you want, keep them around, and reuse them without worrying about negative interactions between different requests or threads.
And even though [I recently went on record badmouthing the builder pattern](https://www.youtube.com/watch?v=2GMp8VuxZnw&list=PL_-IO8LOLuNqUzvXfRCWRRJBswKEbLhgN&index=3), I think this is a great use case for it.

<pullquote>I love the focus on immutability!</pullquote>

Let's quickly go through the steps one by one.

### Configuring An HTTP Client

To create an [`HttpClient`](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html), simply call `HttpClient.newBuilder()`, configure ahead, and finish with `build()`:

```java
HttpClient client = HttpClient.newBuilder()
	// just to show off; HTTP/2 is the default
	.version(HTTP_2)
	.connectTimeout(Duration.ofSeconds(5))
	.followRedirects(SECURE)
	.build();
```

Besides the HTTP version, connection timeout, and redirect policy, you can also configure the proxy, SSL context and parameters, the authenticator, and cookie handler.
There's also an `executor`-method, but I'll deal with that later.

As I mentioned, the client is immutable and thus automatically thread-safe, so feel free to configure once, use everywhere.

### Configuring An HTTP Request

To create an [`HttpRequest`](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpRequest.html), apply the same pattern as with the client - call `HttpRequest.newBuilder()`, configure, `build()`:

```java
HttpRequest request = HttpRequest.newBuilder()
	.GET()
	.uri(URI.create("https://nipafx.dev"))
	.header("Accept-Language", "en-US,en;q=0.5")
	.build();
```

You don't have to set the URL in `uri(URI)` and can instead pass it straight to `newBuilder(URI)`.
I think I prefer it this way, though, because you can so nicely read it as "GET nipafx.dev".

With `header(String, String)`, you add a name/value pair to the request's header.
If you want to override existing values for a header name, use `setHeader`.
If you have many header entries and don't want to repeat `header` a whole lot, give `headers(String...)` a try, where you alternative between names and values:

```java
HttpRequest request = HttpRequest.newBuilder()
	.GET()
	.uri(URI.create("https://nipafx.dev"))
	.headers(
		"Accept-Language", "en-US,en;q=0.5",
		"Accept-Encoding", "gzip, deflate, br")
	.build();
```

Besides headers and more HTTP methods (`PUT`, `POST`, and the generic `method`), you can request a `"100 CONTINUE"` before sending the request body (if there is one), as well as override the client's preferred HTTP version and timeout.

If you send anything else but a `GET`, you need to include a [`BodyPublisher`](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpRequest.BodyPublisher.html) when configuring the HTTP method:

```java
BodyPublisher requestBody = BodyPublishers
	.ofString("{ request body }");
HttpRequest request = HttpRequest.newBuilder()
	.POST(requestBody)
	.uri(URI.create("https://nipafx.dev"))
	.build();
```

What's up with `BodyPublisher`, you ask?
(He rhetorically asked while being the only person around.) It has to do with handling the request body reactively, which, remember, I'll cover in [the next post](java-reactive-http-2-requests-responses).
For now it suffices to say that you can get instances of it from `BodyPublishers` - depending in what form your body comes, you can call these (and a few more) static methods on it:

-   `ofByteArray(byte[])`
-   `ofFile(Path)`
-   `ofString(String)`
-   `ofInputStream(Supplier<InputStream>)`

Pass the returned `BodyPublisher` to the request builder's `PUT`, `POST`, or `method` and you're golden.

### Receiving An HTTP Response

Receiving an [`HttpResponse`](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.html) is as easy as calling `HttpClient.send(...)`.
Well, almost.
You also have to provide a so-called [`BodyHandler<T>`](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.BodyHandler.html), which is in charge of handling the response's bytes as they are being received and transform them into something more usable.
Like with `BodyPublisher`, I'll go into this later.

For now I'll just use `BodyHandlers.ofString()`, which means the incoming bytes will be interpreted as a single string.
This defines the response's generic type as `String`:

```java
HttpResponse<String> response = client.send(
	request,
	BodyHandlers.ofString());
// `HttpResponse<T>.body()` returns a `T`
String respnseBody = response.body();
```

Besides the body, the response also contains the status code, headers, SSL session, a reference to the request, as well as intermediate responses that handled redirection or authentication.

## Synchronous HTTP Request Handling

Let's put things together and search the ten longest Wikipedia articles for a given term.
Since the upcoming experiments all use the same URLs and search term and can also reuse the same client, we can declare them all in static fields:

```java
private static final HttpClient CLIENT = HttpClient.newBuilder().build();

private static final List<URI> URLS = Stream.of(
	"https://en.wikipedia.org/wiki/List_of_compositions_by_Franz_Schubert",
	"https://en.wikipedia.org/wiki/2018_in_American_television",
	"https://en.wikipedia.org/wiki/List_of_compositions_by_Johann_Sebastian_Bach",
	"https://en.wikipedia.org/wiki/List_of_Australian_treaties",
	"https://en.wikipedia.org/wiki/2016%E2%80%9317_Coupe_de_France_Preliminary_Rounds",
	"https://en.wikipedia.org/wiki/Timeline_of_the_war_in_Donbass_(April%E2%80%93June_2018)",
	"https://en.wikipedia.org/wiki/List_of_giant_squid_specimens_and_sightings",
	"https://en.wikipedia.org/wiki/List_of_members_of_the_Lok_Sabha_(1952%E2%80%93present)",
	"https://en.wikipedia.org/wiki/1919_New_Year_Honours",
	"https://en.wikipedia.org/wiki/List_of_International_Organization_for_Standardization_standards"
).map(URI::create).collect(toList());

private static final String SEARCH_TERM = "Foo";
```

With the HTTP client, URLs, and search term ready, we can build our requests (one per URL), send them out, wait for the response to return, and then check the body for the search term:

```java
static void blockingSearch() {
	URLS.forEach(url -> {
		boolean found = blockingSearch(CLIENT, url, SEARCH_TERM);
		System.out.println(
			"Completed " + url + " / found: " + found);
	});
}

static boolean blockingSearch(
		HttpClient client, URI url, String term) {
	try {
		HttpRequest request = HttpRequest
			.newBuilder(url).GET().build();
		HttpResponse<String> response = client.send(
			request, BodyHandlers.ofString());
		return response.body().contains(term);
	} catch (IOException | InterruptedException ex) {
		// to my colleagues: I copy-pasted this code
		// snippet from a blog post and didn't fix the
		// horrible exception handling - punch me!
		return false;
	}
}
```

Depending on my internet connection, running that program takes between 2 and 4 seconds.

That's all fine and dandy, but where's the reactive part?!
The naive implementation above blocks on each of the ten requests, wasting precious time and resources!
There are three places where the code can be changed to become non-blocking:

<pullquote>Each call to `HttpClient::send` blocks, wasting precious time and resources</pullquote>

-   send request asynchronously
-   provide request body as reactive stream
-   process response body as reactive stream

I'm gonna explain the first one here, and leave the other two for later.

## Asynchronous HTTP Request Handling

The most straightforward way to make the calls non-blocking is to send them asynchronously and `HttpClient` has a method just for that: `sendAsync` sends the request and immediately returns a `CompletableFuture<HttpResponse<T>>`.

<pullquote>`HttpClient::sendAsync` immediately returns a `CompletableFuture` for the response</pullquote>

By default, the request is handled by an executor service deep in the JVM's bowels, but if you call `HttpClient.Builder::executor` while building the client, you can define a custom `Executor` for these calls.
Whichever executor takes care of the request/response, you can use your thread to continue with more important stuff.
For example, requesting the next nine Wikipedia pages.
😉

Not so fast, though, first we need to append some computations to the `CompletableFuture`, so when the request returns, we see the expected output:

```java
static CompletableFuture<Void> asyncSearch(
		HttpClient client, URI url, String term) {
	HttpRequest request = HttpRequest
		.newBuilder(url).GET().build();
	return client
		.sendAsync(request, BodyHandlers.ofString())
		.thenApply(HttpResponse::body)
		.thenApply(body -> body.contains(term))
		.exceptionally(__ -> false)
		.thenAccept(found ->
			System.out.println(
				"Completed " + url + " / found: " + found));
}
```

As mentioned, `HttpClient::sendAsync` returns a `CompletableFuture<HttpResponse<T>>` that eventually completes with the response.
(If you don't know the `CompletableFuture` API well, think of `thenApply` as `Optional::map` and `thenAccept` as `Optional::ifPresent`.
For explanations and more processing options, check [the JavaDoc for `CompletableFuture`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html).) We then extract the request body (a `String`), check whether it contains the search term (thus transforming to a `Boolean`) and finally print that to standard out.
We use `exceptionally` to map any errors that may occur while handling the request or response to a "not found" result.

Note that `thenAccept` returns a `CompletableFuture<Void>`:

-   it's `Void` because we are expected to have finished processing the content in the specified `Consumer`
-   it's still a `CompletableFuture`, so we can wait for it to finish

Because, in this demo, that's what we need to do eventually.
The threads running our requests are [daemon threads](https://stackoverflow.com/a/2213348/2525313), which means they don't keep our program alive.
If `main` sends ten asynchronous requests without waiting for them to complete, the program ends immediately after the ten sends and we never see any results.
Hence:

```java
static void asyncSearch() {
	CompletableFuture[] futures = URLS.stream()
		.map(url -> asyncSearch(CLIENT, url, SEARCH_TERM))
		.toArray(CompletableFuture[]::new);
	CompletableFuture.allOf(futures).join();
}
```

This usually takes about 75% of the time of the blocking approach, which, I have to admit, I find surprisingly slow.
This is not a benchmark, though, so never mind.
The principal fact is that our thread is free to do other things while requests are send and responses received in the background.

Handling the request/response lifecycle asynchronously is pretty neat, but it still suffers from a (potential) downside: Both the request's and the response's body have to be processed in one piece.

## Summary

You need two ingredients to send a request:

-   With `HttpClient.newBuilder().$configure().build()` you get an immutable and reusable `HttpClient`.
You can \$configure preferred HTTP version, timeout, proxy, cookie handler, executor for asynchronous requests, and more.
-   With `HttpRequest.newBuilder().$configure().build()` you get an immutable and reusable `HttpRequest`.
You can override the client's HTTP version, timeout, and so forth.
If the request has a body, provide it as a `BodyPublisher`; you will mostly use the factory methods on `BodyPublishers` for that.

With an `HttpClient` and `HttpResponse` in hand, you can call either `send` or `sendAsync` on the former.
You also have to provide a `BodyHandler`, which you can get from `BodyHandlers` - it is in charge of transforming response bytes to something more amenable.

If you use `send`, the method call blocks until the response is complete and then returns an `HttpResponse<T>`.
If you call `sendAsync`, the call immediately returns with a `CompletableFuture<HttpResponse<T>>` that you can then chain further processing steps to.

And that's it!
Next week: [How to process request and response bodies without having to keep them in memory in their entirety.](java-reactive-http-2-requests-responses)
