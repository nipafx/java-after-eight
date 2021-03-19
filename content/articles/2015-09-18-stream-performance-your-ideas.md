---
title: "Stream Performance - Your Ideas"
tags: [java-8, performance, streams]
date: 2015-09-18
slug: java-stream-performance-your-ideas
description: "Another post about stream performance - this one implements your ideas about how else to approach the topic."
intro: "After my last post you had some ideas about how else to benchmark stream performance. I implemented them and here are the results."
searchKeywords: "stream performance"
featuredImage: stream-performance-your-ideas
repo: benchmarks
source: "[This Google spreadsheet](https://docs.google.com/spreadsheets/d/1K-y44zFrBWpZXkdaBI80-g_MqJiuphmuZAP6gg6zz_4/edit#gid=1205798000) contains the updated data."
---

Last week I presented some benchmark results regarding [the performance of streams in Java 8](java-stream-performance).
You guys and gals were interested enough to leave some ideas what else could be profiled.

So that's what I did and here are the results.

The [last post's prologue](java-stream-performance#prologue) applies here as well.
Read it to find out why all numbers lie, how I came up with them, and how you can reproduce them.

## Impact Of Comparisons

> Nice.
> Been saying for a long time writing java to being Ansi C like is faster (arrays not lists).
>
> The next step down the rabbit hole is...
>
> try { for(int i = 0;;) do stuff; } catch (Exception ex) { blah blah; }
>
> Don't check for the loop at all and just catch the exception, nice for HD pixel processing.
>
> [Chaoslab](https://www.reddit.com/r/java/comments/3k2s7j/java_8_stream_performance_compared_to_for_loops/cuv03it)

WAT?
People are doing that?

```java
public int array_max_forWithException() {
	int m = Integer.MIN_VALUE;
	try {
		for (int i = 0; ; i++)
			if (intArray[i] > m)
				m = intArray[i];
	} catch (ArrayIndexOutOfBoundsException ex) {
		return m;
	}
}
```

Maybe they should stop because it looks like it doesn't improve performance:

<table class="center">
  <tr>
    <th></th>
    <th colspan="6">runtime in ms normalized to 1'000'000 elements</th>
  </tr>
  <tr>
    <th></th>
    <th>50'000</th>
    <th>500'000</th>
    <th>1'000'000</th>
    <th>5'000'000</th>
    <th>10'000'000</th>
    <th>50'000'000</th>
  </tr>
  <tr>
    <td><code>array_max_for</code></td>
    <td>0.261</td>
    <td>0.261</td>
    <td>0.277</td>
    <td>0.362</td>
    <td>0.347</td>
    <td>0.380</td>
  </tr>
  <tr>
    <td><code>array_max_forWithException</code></td>
    <td>0.265</td>
    <td>0.265</td>
    <td>0.273</td>
    <td>0.358</td>
    <td>0.347</td>
    <td>0.386</td>
  </tr>
</table>

Looks like the mechanism used to break the loop has no measurable impact.
This makes sense as [loop unrolling](https://en.wikipedia.org/wiki/Loop_unrolling) can avoid most of the comparisons and the cost of throwing an exception is in the area of [a handful of microseconds](http://java-performance.info/throwing-an-exception-in-java-is-very-slow/) and thus orders of magnitude smaller than what happens here.

And this assumes that the compiler does have even more tricks up its sleeve.
Maybe it understands loops on a much more profound level and JIT compiles both methods to the same instructions.

On a side note: See how `array_max_forWithException` does not have a `return` statement after the loop?

Turns out that [the Java compiler recognizes simple infinite loops](http://stackoverflow.com/q/1958563/2525313 "Does Java recognize infinite loops?
- StackOverflow").
Wow!
So it knows that every code path with a finite computation returns and doesn't care about the infinite ones.

Boiled down, this compiles:

```java
public int infiniteLoop() {
	for(;;);
}
```

You never cease to learn...

## Impact Of Assignments

> \[F\]or the "max" tests I expect there's some drag from updating the local variable on every iteration.
> I'm curious whether finding the minimum value runs in a comparable amount of time.
>
> [b0b0b0b](https://www.reddit.com/r/java/comments/3k2s7j/java_8_stream_performance_compared_to_for_loops/cuuvlsb)

This refers to the fact that all tests were run on arrays or lists whose elements equaled the index within the structure, i.e.
`[0, 1, 2, ..., n-1]`.
So finding the maximum indeed requires `n` assignments.

What about finding the minimum instead, which only takes one assignment?

<table>
  <tr>
    <th></th>
    <th colspan="6">runtime in ms normalized to 1'000'000 elements</th>
  </tr>
  <tr>
    <th></th>
    <th>50'000</th>
    <th>500'000</th>
    <th>1'000'000</th>
    <th>5'000'000</th>
    <th>10'000'000</th>
    <th>50'000'000</th>
  </tr>
  <tr>
    <td><code>array_max_for</code></td>
    <td>0.261</td>
    <td>0.261</td>
    <td>0.277</td>
    <td>0.362</td>
    <td>0.347</td>
    <td>0.380</td>
  </tr>
  <tr>
    <td><code>array_min_for</code></td>
    <td>0.264</td>
    <td>0.260</td>
    <td>0.280</td>
    <td>0.353</td>
    <td>0.348</td>
    <td>0.359</td>
  </tr>
</table>

Nope, no difference.
My guess is that due to [pipelining](https://en.wikipedia.org/wiki/Instruction_pipeline), the assignment is effectively free.

## Impact Of Boxing

There were two comments regarding boxing.

> It would also be nice to see the Integer\[\] implementation, to confirm the suspicion about boxing.
>
> [ickysticky](https://www.reddit.com/r/java/comments/3k2s7j/java_8_stream_performance_compared_to_for_loops/cuucdj4)

Ok, let's do that.
The following numbers show a for loop and a for-each loop over an `int[]`, an `Integer[]`, and a `List<Integer>`:

<table>
  <tr>
    <th></th>
    <th colspan="6">runtime in ms normalized to 1'000'000 elements</th>
  </tr>
  <tr>
    <th></th>
    <th>50'000</th>
    <th>500'000</th>
    <th>1'000'000</th>
    <th>5'000'000</th>
    <th>10'000'000</th>
    <th>50'000'000</th>
  </tr>
  <tr>
    <td><code>array_max_for</code></td>
    <td>0.261</td>
    <td>0.261</td>
    <td>0.277</td>
    <td>0.362</td>
    <td>0.347</td>
    <td>0.380</td>
  </tr>
  <tr>
    <td><code>array_max_forEach</code></td>
    <td>0.269</td>
    <td>0.262</td>
    <td>0.271</td>
    <td>0.349</td>
    <td>0.349</td>
    <td>0.356</td>
  </tr>
  <tr>
    <td><code>boxedArray_max_for</code></td>
    <td>0.804</td>
    <td>1.180</td>
    <td>1.355</td>
    <td>1.387</td>
    <td>1.306</td>
    <td>1.476</td>
  </tr>
  <tr>
    <td><code>boxedArray_max_forEach</code></td>
    <td>0.805</td>
    <td>1.195</td>
    <td>1.338</td>
    <td>1.405</td>
    <td>1.292</td>
    <td>1.421</td>
  </tr>
  <tr>
    <td><code>list_max_for</code></td>
    <td>0.921</td>
    <td>1.306</td>
    <td>1.436</td>
    <td>1.644</td>
    <td>1.509</td>
    <td>1.604</td>
  </tr>
  <tr>
    <td><code>list_max_forEach</code></td>
    <td>1.042</td>
    <td>1.472</td>
    <td>1.579</td>
    <td>1.704</td>
    <td>1.561</td>
    <td>1.629</td>
  </tr>
</table>

We can see clearly that the dominating indicator for the runtime is whether the data structure contains primitives or Objects.
But wrapping the Integer array into a list causes an additional slowdown.

Yann Le Tallec also commented on boxing:

> intList.stream().max(Math::max); incurs more unboxing than is necessary.
>
> intList.stream().mapToInt(x -&gt; x).max(); is about twice as fast and close to the array version.
>
> [Yann Le Tallec](java-stream-performance)<!-- comment-2244249020 -->

This claim is in line with what we deduced in the last post: Unboxing a stream as soon as possible may improve performance.

Just to check again:

<table>
  <tr>
    <th></th>
    <th colspan="6">runtime in ms normalized to 1'000'000 elements (error in %)</th>
  </tr>
  <tr>
    <th></th>
    <th>50'000</th>
    <th>500'000</th>
    <th>1'000'000</th>
    <th>5'000'000</th>
    <th>10'000'000</th>
    <th>50'000'000</th>
  </tr>
  <tr>
    <td><code>boxedArray_max_stream</code></td>
    <td>4.231 (43%)</td>
    <td>5.715 (3%)</td>
    <td>5.004 (27%)</td>
    <td>5.461 (53%)</td>
    <td>5.307 (56%)</td>
    <td>5.507 (54%)</td>
  </tr>
  <tr>
    <td><code>boxedArray_max_stream_unbox</code></td>
    <td>3.367 (&lt;1%)</td>
    <td>3.515 (&lt;1%)</td>
    <td>3.548 (2%)</td>
    <td>3.632 (1%)</td>
    <td>3.547 (1%)</td>
    <td>3.600 (2%)</td>
  </tr>
  <tr>
    <td><code>list_max_stream</code></td>
    <td>7.230 (7%)</td>
    <td>6.492 (&lt;1%)</td>
    <td>5.595 (36%)</td>
    <td>5.619 (48%)</td>
    <td>5.852 (45%)</td>
    <td>5.631 (51%)</td>
  </tr>
  <tr>
    <td><code>list_max_stream_unbox</code></td>
    <td>3.370 (&lt;1%)</td>
    <td>3.515 (1%)</td>
    <td>3.527 (&lt;1%)</td>
    <td>3.668 (3%)</td>
    <td>3.807 (2%)</td>
    <td>3.702 (5%)</td>
  </tr>
</table>

This seems to verify the claim.
But the results look very suspicious because the errors are huge.
Running these benchmarks over and over with different settings revealed a pattern:

-   Two performance levels exist, one at \~3.8 ns/op and one at \~7.5 ns/op.
-   Unboxed streams exclusively perform at the better one.
-   Individual iterations of boxed streams usually run on any of these two levels but rarely clock in at another time.
-   Most often the behavior only changes from fork to fork (i.e.
from one set of iterations to the next).

This all smells suspiciously of problems with my test setup.
I would be very interesting to hear from someone with any idea what is going on.

<admonition type="update">

Yann indeed [had an idea](java-stream-performance-your-ideas)<!-- comment-2260028885 --> and pointed to [this interesting question and great answer](http://stackoverflow.com/q/25847397/2525313) on StackOverflow.
Now my best guess is that boxed streams *can* perform on the level of unboxed ones but might fall pray to accidental deoptimizations.

</admonition>

## Impact Of Hardware

Redditor [robi2106](https://www.reddit.com/user/robi2106) ran the suite for 500'000 elements on his "i5-4310 @2Ghz w 8GB DDR2".
I added the results to [the spreadsheet](https://docs.google.com/spreadsheets/d/1K-y44zFrBWpZXkdaBI80-g_MqJiuphmuZAP6gg6zz_4/edit#gid=2145492886).

It's hard to draw conclusions from the data.
Robi noted "I didn't stop using my system for these 2.5hrs either", which might explain the massive error bounds.
They are on median 23 and on average 168 times larger than mine.
(On the other hand, I continued to use my system as well but with pretty low load.)

If you squint hard enough, you could deduce that the i5-4310 is slightly faster on simple computations but lags behind on more complex ones.
Parallel performance is generally as you would expect considering that the i7-4800 has twice as many cores.

## Impact of Language

> It would be interesting how this compares to Scala (with @specialized).
>
> [cryptos6](https://www.reddit.com/r/java/comments/3k2s7j/java_8_stream_performance_compared_to_for_loops/cuuf1yn)

I still didn't try Scala and don't feel like working my way into it for a single benchmark.
Maybe someone more experienced or less squeamish can give it a try?

## Reflection

When interpreting these numbers, remember that the iterations executed an extremely cheap operation.
Last time we found out that already simple arithmetic operations cause enough CPU load to [almost completely offset the difference in iteration mechanisms](java-stream-performance#comparing-operations).
So, as usual, don't optimize prematurely!

All in all I'd say: No new discoveries.
But I enjoyed playing around with your ideas and if you have more, leave a comment.
Or even better, try it out yourself and post the results.
