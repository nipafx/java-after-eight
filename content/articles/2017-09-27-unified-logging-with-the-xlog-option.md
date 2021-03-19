---
title: "Unified Logging Of JVM Messages With The `-Xlog` Option"
tags: [java-9, java-basics]
date: 2017-09-27
slug: java-unified-logging-xlog
description: "Java 9 introduces unified logging, a central mechanism configurable with `-Xlog` to observe class loading, threading, garbage collector, module system, etc."
searchKeywords: "xlog"
featuredImage: unified-logging
inlineCodeLanguage: shell
repo: java-x-demo
---

Java 9 comes with a unified logging architecture ([JEP 158](http://openjdk.java.net/jeps/158)) that pipes a lot of messages that the JVM generates through the same mechanism, which can be configured with the `-Xlog` option.
This gives uniform access to log messages from different subsystems such as class loading, threading, the garbage collector, the module system, or the interaction with the underlying operating system.

The `-Xlog` option can be a bit intimidating, so in this post we will master it step by step, learning how to use it to select which messages and information to show.

## What Is Unified Logging?

The JVM-internal, unified logging infrastructure is very similar to known logging frameworks like Log4j or Logback that you might have used for your application.
It generates textual messages, attaches some meta information like tags (describing the originating subsystem), a log level (describing the importance of the message), and time stamps before printing them somewhere.
The logging output can be configured according to your needs.

<pullquote>The unified logging infrastructure is very similar to known logging frameworks</pullquote>

Logging can be activated with the `java` option `-Xlog`.
This is *the only* flag regarding this mechanism - any further configuration is immediately appended to that option.
Configurable aspects of logging are:

-   which messages to log (by tag and/or by log level)
-   which information to include (for example time stamps and process IDs)
-   which output to use (for example a file)

We'll look at each of them in turn, but before doing that, we can have a look at the kind of messages `-Xlog` produces.
Simply execute `java -Xlog` (maybe append `-version` to get rid of the helpful but long display of command line options) and have a look at the output - of which there is *a lot*.
One of the first messages tells us that the HotSpot virtual machine begins its work:

```shell
$ java -Xlog -version

# truncated a few messages
> [0.002s][info][os       ] HotSpot is running with glibc 2.23, NPTL 2.23
# truncated a lot of messages
```

It shows how long the JVM has been running (2 ms), the message's log level (`info`), its tags (only `os`), and the actual message.
Let's see how to influence these details.

<contentimage slug="unified-logging-diagram" options="bg"></contentimage>

## Defining Which Messages Should Be Shown

The log level and tags can be used to define what exactly the logs should show by defining pairs `<tag-set>=<level>`, which are called *selectors*.
All tags can be selected with `all` and the level is optional and defaults to `info`.
Here's how to use it:

```shell
$ java -Xlog:all=warning -version

# no log messages; great, warning free!
```

Let's try something else:

```shell
$ java -Xlog:logging=debug -version

> [0.034s][info][logging] Log configuration fully initialized.
> [0.034s][debug][logging] Available log levels:
	off, trace, debug, info, warning, error
> [0.034s][debug][logging] Available log decorators: [...]
> [0.034s][debug][logging] Available log tags: [...]
> [0.034s][debug][logging] Described tag combinations:
> [0.034s][debug][logging]  logging: Logging for the log framework itself
> [0.034s][debug][logging] Log output configuration:
> [0.034s][debug][logging] #0: stdout [...]
> [0.034s][debug][logging] #1: stderr [...]]
```

Lucky shot!
I had to truncate the output but trust me, there's a lot of helpful information in those messages.
You don't have to take that route, though, `-Xlog:help` shows the same information but more beautifully formatted (see [below](#puttingthepiecestogether)).

A little surprising (at least at first) is the detail that messages only match a selector if their tags *exactly* match the given ones.
Given *ones*?
Plural?
Yes, a selector can name several tags by concatenating them with `+`.
Still, a message has to contain exactly those to be selected.

Hence, using `gc` (for garbage collection) versus `gc+heap`, for example, should select different messages.
This is indeed the case:

```shell
$ java -Xlog:gc -version

> [0.009s][info][gc] Using G1

$ java -Xlog:gc+heap -version

> [0.006s][info][gc,heap] Heap region size: 1M
```

Several selectors can be defined - they just have to be separated with commas:

```shell
$ java -Xlog:gc,gc+heap -version

> [0.007s][info][gc,heap] Heap region size: 1M
> [0.009s][info][gc     ] Using G1
```

Using this strategy it is very cumbersome to get all messages that contain a certain flag.
Luckily, there is an easier way to do that, namely the wildcard `*`, which can be used with a single tag to define a selector:

```shell
$ java -Xlog:gc*=debug -version

> [0.006s][info][gc,heap] Heap region size: 1M
> [0.006s][debug][gc,heap] Minimum heap 8388608  Initial heap 262144000
	Maximum heap 4192206848
# truncated about two dozen message
> [0.072s][info ][gc,heap,exit         ] Heap
# truncated a few messages showing final GC statistics
```

Using selectors, there are three easy steps to get to know a subsystem of the JVM:

-   Find interesting tags in the output of `java -Xlog:help`.
-   Use them with `-Xlog:tag_1*,tag_2*,tag_n*` to display all `info` messages that were tagged with any of them.
-   Selectively switch to lower log levels with `-Xlog:tag_1*=debug`.

## Defining Where Messages Should Go

Compared to the non-trivial selectors, the output configuration is really simple.
It comes after the selectors (separated by a colon) and has three possible locations:

-   `stdout`: The default output.
On the console that is the terminal window unless redirected, in IDEs it is often shown in a separate tab or view.
-   `stderr`: The default error output.
On the console that is the terminal window unless redirected, in IDEs it is usually shown in the same tab/view as `stdout` but printed red.
-   `file=<filename>`: Defines a file to pipe all messages into.
Putting in `file=` is optional.

Here's how to put all `debug` messages in the file `application.log`:

```shell
$ java -Xlog:all=debug:file=application.log -version
```

More output options are available that allow log file rotation based on file size and number of files to rotate.
While it is not possible to define more than one output option in a single `-Xlog` option, you can repeat the entire option with varied output options (thanks [Bruce](java-unified-logging-xlog)<!-- comment-4496458659 -->, for pointing this out):

```shell
$ java -Xlog:all=debug:stdout -Xlog:all=debug:file=application.log -version
```

Not exactly convenient, but functional.

## Defining What Messages Should Say

As I said in the introduction, each message consist of text and meta-information.
Which of these additional pieces of information will be printed, is configurable by selecting *decorators*, which are listed in the following table.
This happens after the output location and another colon.

| Option | Description |
|--------|:------------|
| `time`			| Current time and date in ISO-8601 format. |
| `uptime`			| Time since the start of the JVM in seconds and milliseconds (e.g., 6.567s). |
| `timemillis`		| The same value as generated by `System.currentTimeMillis()`. |
| `uptimemillis`	| Milliseconds since the JVM started. |
| `timenanos`		| The same value as generated by `System.nanoTime()`. |
| `uptimenanos`		| Nanoseconds since the JVM started. |
| `pid`				| The process identifier. |
| `tid`				| The thread identifier. |
| `level`			| The level associated with the log message. |
| `tags`			| The tag-set associated with the log message. |

Let's say we want to print the time stamp, the uptime in milliseconds, and the thread ID for all garbage collection debug messages to the console.
Here's how to do that:

```shell
$ java -Xlog:gc*=debug:stdout:time,uptimemillis,tid -version

# truncated messages
> [2017-02-01T13:10:59.689+0100][7ms][18607] Heap region size: 1M
```

## Putting The Pieces Together

Formally, the `-Xlog` option has this syntax:

```shell
-Xlog:<selectors>:<output>:<decorators>:<output-options>
```

Each of the parameters following `-Xlog` is optional but if one is used, so have to be all the others that come before it.

-   Selectors are pairs of tag sets and log levels.
This part is also called the *what-expression*, a phrase you will likely encounter when the configuration is not syntactically correct.
-   With `output` the target location for the log messages (in short, the terminal window or a log file) can be defined.
-   Decorators can be used to define what information the messages should include.
-   Yes, annoyingly the output mechanism and further output options are split, with decorators in between.

For more details, have a look at [the online documentation](https://docs.oracle.com/javase/9/tools/java.htm#JSWOR-GUID-BE93ABDC-999C-4CB5-A88B-1994AAAC74D5) or the output of `java -Xlog:help`:

```shell
-Xlog Usage: -Xlog[:[what][:[output][:[decorators][:output-options]]]]
		where 'what' is a combination of tags and levels on the form
			tag1[+tag2...][*][=level][,...]
		Unless wildcard (*) is specified, only log messages tagged with
			exactly the tags specified will be matched.

Available log levels:
	off, trace, debug, info, warning, error

Available log decorators:
	time (t), utctime (utc), uptime (u), timemillis (tm), uptimemillis (um),
	timenanos (tn), uptimenanos (un), hostname (hn), pid (p), tid (ti),
	level (l), tags (tg)
	Decorators can also be specified as 'none' for no decoration.

Available log tags:
	[... many, many tags ... ]
	Specifying 'all' instead of a tag combination matches all tag combinations.

Available log outputs:
	stdout, stderr, file=<filename>
	Specifying %p and/or %t in the filename will expand to the JVM's PID and
	startup timestamp, respectively.

Some examples:
	[... a few helpful examples to get you going ... ]
```

## Reflection

In Java 9, most JVM subsystems use a unified logging mechanism.
This makes it easier than before to configure logging, so you get the exact messages you need.
The `-Xlog` option allows the definition of `<selectors>`, `<output>`, `<decorators>`, and `<output-options>`, which can be used to define precisely what gets logged, where the output shows up, and what it looks like.
