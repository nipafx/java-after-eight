---
title: "Stream Performance"
tags: [java-8, performance, streams]
date: 2015-09-07
slug: java-stream-performance
description: "A close look at stream performance. How do they compare to for and for-each loops oder arrays and lists. And what role plays boxing?"
intro: "A close look at stream performance. How do they compare to for and for-each loops oder arrays and lists. And what roles play boxing and the complexity of the executed operation?"
searchKeywords: "stream performance"
featuredImage: stream-performance
repo: benchmarks
source: "[This Google spreadsheet](https://docs.google.com/spreadsheets/d/1K-y44zFrBWpZXkdaBI80-g_MqJiuphmuZAP6gg6zz_4/edit#gid=1205798000) contains the resulting data."
---

When I read [Angelika Langer's *Java performance tutorial – How fast are the Java 8 streams?*](https://jaxenter.com/java-performance-tutorial-how-fast-are-the-java-8-streams-118830.html "Java performance tutorial - How fast are the Java 8 streams? - JAXEnter") I couldn't believe that for a specific operation they took about 15 times longer than for loops.
Could stream performance really be that bad?
I had to find out!

Coincidentally, I recently watched [a cool talk about microbenchmarking Java code](https://www.parleys.com/tutorial/java-microbenchmark-harness-the-lesser-two-evils) and I decided to put to work what I learned there.
So lets see whether streams really are that slow.

## Prologue

Unfortunately, I have to start with a dull prologue.

### Disclaimer

This post contains a lot of numbers and numbers are deceitful.
They seem all scientific and precise and stuff, and they lure us into focusing on their interrelation and interpretation.
But we should always pay equal attention to how they came to be!

The numbers I'll present below were produced on my system with very specific test cases.
It is easy to over-generalize them!
I should also add that I have only two day's worth of experience with non-trivial benchmarking techniques (i.e.
ones that are not based on looping and manual `System.currentTimeMillis()`).

Be very careful with incorporating the insights you gained here into your mental performance model.
The devil hiding in the details is the JVM itself and it is a deceitful beast.
It is entirely possible that my benchmarks fell victim to optimizations that skewed the numbers.

### System

-   **CPU**: Intel(R) Core(TM) i7-4800MQ CPU @ 2.70GHz
-   **RAM**: Samsung DDR3 16GB @ 1.60GHz (the tests ran entirely in RAM)
-   **OS**: Ubuntu 15.04. Kernel version 3.19.0-26-generic
-   **Java**: 1.8.0\_60
-   **JMH**: 1.10.5

### Benchmark

#### JMH

The benchmarks were created using the wonderful [Java Microbenchmarking Harness (JMH)](http://openjdk.java.net/projects/code-tools/jmh/), which is developed and used by the JVM performance team itself.
It's thoroughly documented, easy to set up and use, and the [explanation via samples](http://hg.openjdk.java.net/code-tools/jmh/file/tip/jmh-samples/src/main/java/org/openjdk/jmh/samples/) is awesome!

https://twitter.com/nipafx/status/639733181458055168

If you prefer a casual introduction, you might like [Aleksey Shipilev's talk from Devoxx UK 2013](https://www.parleys.com/tutorial/java-microbenchmark-harness-the-lesser-two-evils).

#### Setup

To create somewhat reliable results, benchmarks are run individually and repeatedly.
There is a separate run for each benchmark method that is made up of several [forks](http://hg.openjdk.java.net/code-tools/jmh/file/0879b862a1a3/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_12_Forking.java#l52), each running a number of [warmup](http://hg.openjdk.java.net/code-tools/jmh/file/0879b862a1a3/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_20_Annotations.java#l69) iterations before the actual [measurement](http://hg.openjdk.java.net/code-tools/jmh/file/0879b862a1a3/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_20_Annotations.java#l70) iterations.

I ran separate benchmarks with 50'000, 500'000, 5'000'000, 10'000'000, and 50'000'000 elements.
Except the last one all had two forks, both consisting of five warmup and five measurement iterations, where each iteration was three seconds long.
Parts of the last one were run in one fork, two warmup and three measurement iterations, each 30 seconds long.

Langer's article states that their arrays are populated with random integers.
I compared this to the more pleasant case where each `int` in the array equals its position therein.
[The deviation](https://docs.google.com/spreadsheets/d/1K-y44zFrBWpZXkdaBI80-g_MqJiuphmuZAP6gg6zz_4/edit#gid=1317392388) between the two scenarios averaged 1.2% with the largest difference being 5.4%.

Since creating millions of randomized integers takes considerable time, I opted to execute the majority of the benchmarks on the ordered sequences only, so unless otherwise noted numbers pertain to this scenario.

#### Code

The benchmark code itself is [available on GitHub](https://github.com/nipafx/benchmarks).
To run it, simply go to the command line, build the project, and execute the resulting jar:

```
mvn clean install
java -jar target/benchmarks.jar
```

Some easy tweaks:

-   adding a regular expression at the end of the execution call will only benchmark methods whose fully-qualified name matches that expression; e.g. to only run `ControlStructuresBenchmark` :

	```
	java -jar target/benchmarks.jar Control
	```

-   the annotations on `AbstractIterationBenchmark` govern how often and how long each benchmark is executed
-   the constant `NUMBER_OF_ELEMENTS` defines the length of the array/list that is being iterated over
-   tweak `CREATE_ELEMENTS_RANDOMLY` to switch between an array of ordered or of random numbers

## Stream Performance

### Repeating The Experiment

Let's start with the case that triggered me to write this post: Finding the maximum value in an array of 500'000 random elements.

```java
int m = Integer.MIN_VALUE;
for (int i = 0; i < intArray.length; i++)
	if (intArray[i] > m)
		m = intArray[i];
```

First thing I noticed: My laptop performs much better than the machine used for the JAX article.
This was to be expected as it was described as "outdated hardware (dual core, no dynamic overclocking)" but it made me happy nevertheless since I paid enough for the damn thing.
Instead of 0.36 ms it only took 0.130 ms to loop through the array.

More interesting are the results for using a stream to find the maximum:

```java
// article uses 'reduce' to which 'max' delegates
Arrays.stream(intArray).max();
```

Langer reports a runtime of 5.35 ms for this, which compared to the loop's 0.36 ms yields the reported slowdown by x15.
I consistently measured about 0.560 ms, so I end up with a slowdown of "only" x4.5.
Still a lot, though.

Next, the article compares iterating over lists against streaming them.

```java
// for better comparability with looping over the array
// I do not use a "for each" loop (unlike the Langer's article);
// measurements show that this makes things a little faster
int m = Integer.MIN_VALUE;
for (int i = 0; i < intList.size(); i++)
	if (intList.get(i) > m)
		m = intList.get(i);

// vs

intList.stream().max(Math::max);
```

The results are 6.55 ms for the for loop and 8.33 ms for the stream.
My measurements are 0.700 ms and 3.272 ms.
While this changes their relative performance considerably, it creates the same order:

<table>
  <tr>
    <th></th>
    <th colspan="2">Angelika Langer</th>
    <th colspan="2">Me</th>
  </tr>
  <tr>
    <th className="centered-cell">operation</th>
    <th>time (ms)</th>
    <th>slower</th>
    <th>time (ms)</th>
    <th>slower</th>
  </tr>
  <tr>
    <th><code>array_max_for</code></th>
    <td>0.36</td>
    <td>-</td>
    <td>0.123</td>
    <td>-</td>
  </tr>
  <tr>
    <th><code>array_max_stream</code></th>
    <td>5.35</td>
    <td>14'861%</td>
    <td>0.599</td>
    <td>487%</td>
  </tr>
  <tr>
    <th><code>list_max_for</code></th>
    <td>6.55</td>
    <td>22%</td>
    <td>0.700</td>
    <td>17%</td>
  </tr>
  <tr>
    <th><code>list_max_stream</code></th>
    <td>8.33</td>
    <td>27%</td>
    <td>3.272</td>
    <td>467%</td>
  </tr>
</table>

I ascribe the marked difference between iterations over arrays and lists to boxing; or rather to the resulting indirection.
The primitive array is packed with the values we need but the list is backed by an array of `Integer`s, i.e.
references to the desired values which we must first resolve.

The considerable difference between Langer's and my series of relative changes (+14'861% +22% +27% vs +487% + 17% + 467%) underlines her statement, that "the performance model of streams is not a trivial one".

Bringing this part to a close, her article makes the following observation:

> We just compare two integers, which after JIT compilation is barely more than one assembly instruction.
For this reason, our benchmarks illustrate the cost of element access – which need not necessarily be a typical situation.
The performance figures change substantially if the functionality applied to each element in the sequence is cpu intensive.
You will find that there is no measurable difference any more between for-loop and sequential stream if the functionality is heavily cpu bound.

So let's have a lock at something else than just integer comparison.

### Comparing Operations

I compared the following operations:

* **max**:
Finding the maximum value.
* **sum**:
Computing the sum of all values; aggregated in an `int` ignoring overflows.
* **arithmetic**:
To model a less simple numeric operation I combined the values with a a handful of bit shifts and multiplications.
* **string**:
To model a complex operation that creates new objects I converted the elements to strings and xor'ed them character by character.

These were the results (for 500'000 ordered elements; in milliseconds):

<table>
  <tr>
    <th></th>
    <th colspan="2">max</th>
    <th colspan="2">sum</th>
    <th colspan="2">arithmetic</th>
    <th colspan="2">string</th>
  </tr>
  <tr>
    <th></th>
    <th>array</th>
    <th>list</th>
    <th>array</th>
    <th>list</th>
    <th>array</th>
    <th>list</th>
    <th>array</th>
    <th>list</th>
  </tr>
  <tr>
    <th><code>for</code></th>
    <td>0.123</td>
    <td>0.700</td>
    <td>0.186</td>
    <td>0.714</td>
    <td>4.405</td>
    <td>4.099</td>
    <td>49.533</td>
    <td>49.943</td>
  </tr>
  <tr>
    <th><code>stream</code></th>
    <td>0.559</td>
    <td>3.272</td>
    <td>1.394</td>
    <td>3.584</td>
    <td>4.100</td>
    <td>7.776</td>
    <td>52.236</td>
    <td>64.989</td>
  </tr>
</table>

This underlines how cheap comparison really is, even addition takes a whooping 50% longer.
We can also see how more complex operations bring looping and streaming closer together.
The difference drops from almost 400% to 25%.
Similarly, the difference between arrays and lists is reduced considerably.
Apparently the arithmetic and string operations are CPU bound so that resolving the references had no negative impact.

(Don't ask me why for the arithmetic operation streaming the array's elements is faster than looping over them.
I have been banging my head against that wall for a while.)

So let's fix the operation and have a look at the iteration mechanism.

### Comparing Iteration Mechanisms

There are at least two important variables in accessing the performance of an iteration mechanism: its overhead and whether it causes boxing, which will hurt performance for memory bound operations.
I decided to try and bypass boxing by executing a CPU bound operation.
As we have seen above, the arithmetic operation fulfills this on my machine.

Iteration was implemented with straight forward for and for-each loops.
For streams I made some additional experiments:

```java
@Benchmark
public int array_stream() {
	// implicitly unboxed
	return Arrays
			.stream(intArray)
			.reduce(0, this::arithmeticOperation);
}

@Benchmark
public int array_stream_boxed() {
	// explicitly boxed
	return Arrays
			.stream(intArray)
			.boxed()
			.reduce(0, this::arithmeticOperation);
}

@Benchmark
public int list_stream_unbox() {
	// naively unboxed
	return intList
			.stream()
			.mapToInt(Integer::intValue)
			.reduce(0, this::arithmeticOperation);
}

@Benchmark
public int list_stream() {
	// implicitly boxed
	return intList
			.stream()
			.reduce(0, this::arithmeticOperation);
}
```

Here, boxing and unboxing does not relate to how the data is stored (it's unboxed in the array and boxed in the list) but how the values are processed by the stream.

Note that `boxed` converts the `IntStream`, a specialized implementation of `Stream` that only deals with primitive `int`s, to a `Stream<Integer>`, a stream over objects.
This should have a negative impact on performance but the extent depends on how well [escape analysis](https://en.wikipedia.org/wiki/Escape_analysis#Example_.28Java.29) works.

Since the list is generic (i.e.
no specialized `IntArrayList`), it returns a `Stream<Integer>`.
The last benchmark method calls `mapToInt`, which returns an `IntStream`.
This is a naive attempt to unbox the stream elements.

<table>
  <tr>
    <th></th>
    <th colspan="2">arithmetic</th>
  </tr>
  <tr>
    <th></th>
    <th>array</th>
    <th>list</th>
  </tr>
  <tr>
    <th><code>for</code></th>
    <td>4.405</td>
    <td>4.099</td>
  </tr>
  <tr>
    <th><code>forEach</code></th>
    <td>4.434</td>
    <td>4.707</td>
  </tr>
  <tr>
    <th><code>stream</code> (unboxed)</th>
    <td>4.100</td>
    <td>4.518</td>
  </tr>
  <tr>
    <th><code>stream</code> (boxed)</th>
    <td>7.694</td>
    <td>7.776</td>
  </tr>
</table>

Well, look at that!
Apparently the naive unboxing *does* work (in this case).
I have some vague notions why that might be the case but nothing I am able to express succinctly (or correctly).
Ideas, anyone?

(Btw, all this talk about boxing/unboxing and specialized implementations makes me ever more happy that [Project Valhalla is advancing so well](java-road-to-valhalla).)

The more concrete consequence of these tests is that for CPU bound operations, streaming seems to have no considerable performance costs.
After fearing a considerable disadvantage this is good to hear.

### Comparing Number Of Elements

In general the results are pretty stable across runs with a varying sequence length (from 50'000 to 50'000'000).
To this end I examined the normalized performance per 1'000'000 elements across those runs.

But I was pretty astonished that performance does not automatically improve with longer sequences.
My simple mind assumed, that this would give the JVM the opportunity to apply more optimizations.
Instead there are some notable cases were performance actually dropped:

<table>
  <tr>
    <th colspan="2">From 500'000 to 50'000'000 Elements</th>
  </tr>
  <tr>
    <th className="centered-cell">method</th>
    <th>time</th>
  </tr>
  <tr>
    <td><code>array_max_for</code></td>
    <td>+ 44.3%</td>
  </tr>
  <tr>
    <td><code>array_sum_for</code></td>
    <td>+ 13.4%</td>
  </tr>
  <tr>
    <td><code>list_max_for</code></td>
    <td>+ 12.8%</td>
  </tr>
</table>

Interesting that these are the simplest iteration mechanisms and operations.

Winners are more complex iteration mechanisms over simple operations:

<table>
  <tr>
    <th colspan="2">From 500'000 to 50'000'000 Elements</th>
  </tr>
  <tr>
    <th className="centered-cell">method</th>
    <th>time</th>
  </tr>
  <tr>
    <td><code>array_sum_stream</code></td>
    <td>- 84.9%</td>
  </tr>
  <tr>
    <td><code>list_max_stream</code></td>
    <td>- 13.5%</td>
  </tr>
  <tr>
    <td><code>list_sum_stream</code></td>
    <td>- 7.0%</td>
  </tr>
</table>

This means that the table we have seen above for 500'000 elements looks a little different for 50'000'000 (normalized to 1'000'000 elements; in milliseconds):

<table>
  <tr>
    <th></th>
    <th colspan="2">max</th>
    <th colspan="2">sum</th>
    <th colspan="2">arithmetic</th>
    <th colspan="2">string</th>
  </tr>
  <tr>
    <th></th>
    <th>array</th>
    <th>list</th>
    <th>array</th>
    <th>list</th>
    <th>array</th>
    <th>list</th>
    <th>array</th>
    <th>list</th>
  </tr>
  <tr>
    <th></th>
    <th colspan="8">500'000 elements</th>
  </tr>
  <tr>
    <th><code>for</code></th>
    <td>0.246</td>
    <td>1.400</td>
    <td>0.372</td>
    <td>1.428</td>
    <td>8.810</td>
    <td>8.199</td>
    <td>99.066</td>
    <td>98.650</td>
  </tr>
  <tr>
    <th><code>stream</code></th>
    <td>1.118</td>
    <td>6.544</td>
    <td>2.788</td>
    <td>7.168</td>
    <td>8.200</td>
    <td>15.552</td>
    <td>104.472</td>
    <td>129.978</td>
  </tr>
  <tr>
    <th></th>
    <th colspan="8">50'000'000 elements</th>
  </tr>
  <tr>
    <th><code>for</code></th>
    <td>0.355</td>
    <td>1.579</td>
    <td>0.422</td>
    <td>1.522</td>
    <td>8.884</td>
    <td>8.313</td>
    <td>93.949</td>
    <td>97.900</td>
  </tr>
  <tr>
    <th><code>stream</code></th>
    <td>1.203</td>
    <td>3.954</td>
    <td>0.421</td>
    <td>6.710</td>
    <td>8.408</td>
    <td>15.723</td>
    <td>96.550</td>
    <td>117.690</td>
  </tr>
</table>

We can see that there is almost no change for the **arithmetic** and **string** operations.
But things changes for the simpler **max** and **sum** operations, where more elements brought the field closer together.

## Reflection

All in all I'd say that there were no big revelations.
We have seen that palpable differences between loops and streams exist only with the simplest operations.
It was a bit surprising, though, that the gap is closing when we come into the millions of elements.
So there is little need to fear a considerable slowdown when using streams.

There are still some open questions, though.
The most notable: What about parallel streams?
Then I am curious to find out at which operation complexity I can see the change from iteration dependent (like **sum** and **max**) to iteration independent (like **arithmetic**) performance.
I also wonder about the impact of hardware.
Sure, it will change the numbers, but will there be qualitative differences as well?

<admonition type="update">

You also had some ideas and I benchmarked them for [a follow-up post](java-stream-performance-your-ideas).

</admonition>

Another takeaway for me is that microbenchmarking is not so hard.
Or so I think until someone points out all my errors...
