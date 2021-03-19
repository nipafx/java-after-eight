---
title: "Definitive Guide To Text Blocks In Java 13"
tags: [java-13, java-basics]
date: 2019-06-19
slug: java-13-text-blocks
description: "Java 13 introduces text blocks: string literals that span multiple lines. Learn about syntax, indentation, escape sequences, and formatting."
intro: "Java 13 introduces text blocks: string literals that span multiple lines. Here's all you need to know about syntax, indentation, escape sequences, formatting, and more."
searchKeywords: "text blocks"
featuredImage: text-blocks
repo: java-x-demo
---

New version, new feature!
Java 13 [previews](java-12-guide#preview-features) *text blocks*, string literals that can span multiple lines:

```java
System.out.println("""
	Hello,
	multiline
	text blocks!""");
// prints:
// > Hello,
// > multiline
// > text blocks!
// wait, where did the indentation go?
```

A much cooler example is embedding another language, say JSON:

```java
String phrase = """
	{
		greeting: "hello",
		audience: "text blocks",
		punctuation: "!"
	}
	""";
```

Text blocks are a straightforward feature (introduced by [JEP 355](https://openjdk.java.net/jeps/355)) without any bells and whistles: no raw strings and no variable or even expression interpolation - all we get now are literals that span several lines.
Raw strings are on the table, though, and thanks to [the fast releases](https://medium.com/codefx-weekly/radical-new-plans-for-java-5f237ab05b0) we may see them as early as 2020.

But that's for another post - in this one we'll dive into text blocks.
If you know similar features from other languages, channel your inner Duke and ignore them for a moment to free your mind for Java's variant.

## Text Block Syntax

Let's start with getting text blocks past the compiler:

-   accepted in the exact same places where a string literal `"like this one"` is accepted
-   begins with three double quotation marks `"""` *and a newline* (that's the *opening delimiter*)
-   ends with three double quotation marks `"""` (that's the *closing delimiter*) - these can be on the last line of content or on their own line, *which makes a difference*

We'll come to placing the closing `"""` in a second.
First, here are a few examples:

```java
String hello = """
	Hello, multiline text blocks!""";
// > Hello, multiline text blocks!

// almost the same but different semantics
hello = """
	Hello, multiline text blocks!
	""";
// > Hello, multiline text blocks!
// >
// (yes, there's a newline at the end)

// newline at the start
hello = """

	Hello, multiline text blocks!
	""";
// >
// > Hello, multiline text blocks!
// >

// compile error: no newline after `"""`
hello = """Hello, multiline text blocks!""";
// compile error: no closing `"""`
hello = """Hello, multiline text blocks!";
```

Because a text block starts with `"""` plus a newline, the newline itself does of course not show up in the output.
But you can already see how the closing delimiter's position changes the string.
Let's look into that!

## Delimiter Semantics ...

As we've seen, starting a text block is trivial.
Just one thing to note: The JEP's examples show and its text even assumes an alignment of content with opening delimiter:

```java
System.out.println("""
				   Hello, multiline text blocks!""");
```

But... why?
Most Java code bases indent continuing lines of a statement with two more indents (I use only one in the blog to conserve space) and I see no reason to change that here.
Quite the opposite, as a follower of *The One True Indentation*, this would require me to mix tabs and spaces.
🤢 That aside, this alignments either breaks when changing the opening line or requires to change the indentation of the entire text block.
Once again: why?
Just don't.

Unlike beginning a text block, ending it seems to require a semantically meaningful decision.
Have a look at this:

```java
System.out.println("""
	Hello, multiline text blocks!""");
// > Hello, multiline text blocks!

System.out.println("""
	Hello, multiline text blocks!
	""");
// > Hello, multiline text blocks!
// >

System.out.println("""
	Hello, multiline text blocks!
""");
// >    Hello, multiline text blocks!
// >

System.out.println("""
		Hello, multiline text blocks!
	""");
// >    Hello, multiline text blocks!
// >
```

The second example shows that putting a text block's closing delimiter on its own line appends a newline to the end of the resulting string.
The last two examples are a little less obvious.
It looks like moving the closing delimiter to the left or the content to the right has the same effect: additional indentation of the final string.
That's indeed the case - let's see why (and how).

<pullquote>Putting the closing delimiter on its own line, appends a newline</pullquote>

## ... and Indentation

Text blocks will usually be indented according to the surrounding code and that indentation is meaningless (or *incidental*) for the resulting string.
At the same time, the developer may add additional, meaningful (or *essential*) white space like in this JSON example:

```java
String phrase = """
	{
		greeting: "hello",
		audience: "text blocks",
		punctuation: "!"
	}
	""";
```

The first indent (a tab in my editor, four spaces in the blog) is an artifact of code formatting, but the second indent on the three property lines is meant to be there.
And so the compiler sets out to determine incidental white space and remove it without touching on essential white space.

### Adding Essential White Space

As we've seen, indentation of only some of the lines is considered essential and thus preserved but indentation shared by all lines is removed.
But some of the examples already showed that there are two ways to indent all lines:

-   moving the closing delimiter to the left
-   moving the content to the right

```java
String hello = """
	Hello, text blocks!
	"""
// > Hello, text blocks!
// >

String hello = """
	Hello, text blocks!
"""
// >    Hello, text blocks!
// >

String hello = """
		Hello, text blocks!
	"""
// >    Hello, text blocks!
// >
```

JEP 355 seems to suggest moving the closing delimiter to change the string's indentation.
Want to indent the string?
Unindent the `"""`.
Want to unindent the string?
Indent the `"""`.
Not exactly intuitive.

Instead I recommend to let your formatter place the closing `"""` as it usually does for continued statements (commonly two more indents) and treat it as fixed in place.
Now, if you want to change the string's indentation, you have to change the lines you want to indent.
Much more intuitive I'd say.

<pullquote>Treat the closing delimiter as fixed in place and change the content's indentation</pullquote>

So far we've glossed over how exactly the compiler determines essential white space, though.
It doesn't do that directly - instead it removes incidental white space and considers everything else essential.

### Removing Incidental White Space

The compiler removes indentation in a fairly interesting and non-trivial algorithm that deserves its own blog post, but the gist is:

-   all trailing whitespace is removed (and good riddance!)
-   for leading white space:
	-   check all non-blank lines (i.e.
lines that aren't just white space)
	-   count the number of leading white space characters in each (the exact character doesn't matter, i.e.
a space counts exactly as much as a tab)
	-   take the smallest of those numbers and remove that many white space characters from each line (once again ignoring the exact kind of character)
	-   the result is that at least one of the lines has no leading white space
-   in what's called a *significant trailing line policy* the line containing the closing `"""` is always included in that check (even though it is blank if `"""` is on its own line!)

The second point leads to the removal of shared leading white space while keeping indentation within the string intact:

```java
String phrase = """
	{
		greeting: "hello",
		audience: "text blocks",
		punctuation: "!"
	}
	""";
```

The compiler has six lines to look at (opening and closing curly braces, three property lines, and closing delimiter line) and determines that there's a tab (four spaces) in front of each of them, so they get removed.
The property lines' additional indentation remains untoched:

```json
{
	greeting: "hello",
	audience: "text blocks",
	punctuation: "!"
}
```

So far, so good.
Now, let's look at the third point.
It's the one that allows us to add leading white space to all lines by positioning the content relative to the closing delimiter.
Let's start here:

```java
String hello = """
	Hello, multiline text blocks!
	""";
```

The block contains a single line of content and so incidental indentation is determined based on it *and* the line with the closing `"""`.
Both have the same indentation (one tab / four spaces) and so it gets removed entirely.
The result is `"Hello, multiline text blocks!\n"`.

Now we move the content to the right:

```java
String hello = """
		Hello, multiline text blocks!
	""";
```

The common white space is still one tab (or four spaces) and so the other half of the content line's indentation is considered essential, which results in `"    Hello, multiline text blocks!\n"`.

If we instead move the closing delimiter to the left...

```java
String hello = """
	Hello, multiline text blocks!
""";
```

... we take a different route (no common white space) to the same result (one tab / four spaces of essential indentation).

Finally, if the closing delimiter is on the last content line ...

```java
String hello = """
	Hello, multiline text blocks!""";
```

... there is no way to mark some of the indentation as essential and so the compiler will always remove all of the white space that all lines share.
That means if you want to indent all lines, you need to put the `"""` on its own line, which adds a newline to the end of your string.
If you don't want that newline, you either:

-   put the closing delimiter on its own line and remove the newline manually
-   put the closing delimiter on the last line of content and add indentation manually

<pullquote>Without closing delimiter on its own line, you can't add indentation</pullquote>

Manually?

### Indenting Methods

There are two methods on `String` that allow you to handle indentation manually.
The first is Java 13's `stripIndent`, which determines and removes incidental white space exactly as the compiler does.
So in case you ever hand-construct, load, or request a string with unknown indentation and want to remove it, `stripIndent` is there for you:

```java
String literalPhrase = ""
	+ " {\n"
	+ "     greeting: \"hello\",\n"
	+ "     audience: \"text blocks\",\n"
	+ "     punctuation: \"!\"\n"
	+ " }\n";
String blockPhrase = """
	{
		greeting: "hello",
		audience: "text blocks",
		punctuation: "!"
	}
	""";
// not equal because the compiler removes
// `blockPhrase`'s indentation
literalPhase.equals(blockPhrase)
// equal because `stripIndent` works like the compiler
literalPhase.stripIndent().equals(blockPhrase)
```

People who bought `stripIndent` also bought `indent` ([since Java 12](https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/lang/String.html#indent(int))):

```java
String indentPhrase = """
	{
		greeting: "hello",
		audience: "text blocks",
		punctuation: "!"
	}
	""".indent(4);
String indentedPhrase = """
		{
			greeting: "hello",
			audience: "text blocks",
			punctuation: "!"
		}
	""";
// this is true in the blog,
// where indents are four spaces
indentPhrase.equals(indentedPhrase);
```

### An Exercise For The Reader

In case you wonder what happens when moving the delimiter further to the right ...

```java
String hello = """
		Hello, multiline text blocks!
			""";
```

... have a look at the bullet points again.
You know everything you need to guess what happens.
Otherwise, try it yourself.
😁

## Odds and ends

As usual, there are a few smaller details to go into, so you can use the feature safely and to full effect...

### Escape Sequences

Because the delimiters are `"""`, you can embed `"` and `""` without having to escape them.
For three quotation marks, you need to escape at least one and I recommend to pick the first:

```java
//
String quotationMarks = """
	one: "
	two: ""
	three: \"""
	""";
```

And since the whole idea behind text blocks are their span across multiple lines, it is of course unnecessary to embed the newline escape sequence `\n` - just add newlines to the source code instead.

That doesn't mean that they don't work, though.
All escape sequences are translated just like in old-school string literals.
This is the final step after indentation was managed as described above, so you can use this to manage horizontal alignment with `\b` or `\t` and vertical alignment with `\n`, `\f,` and `\r` (more on that in a second).
By the way, if you need programmatic access to escape sequence translation, use the new `String::translateEscapes`:

<pullquote>Escape sequences are translated just like in string literals</pullquote>

```java
String tab = "\\t".translateEscapes();
// this is true:
"\t".equals(tab);
```

Of course, `\"` and `\n` also work in text blocks.
It's just that their use is discouraged because you rarely need them.

So in case you're still having trouble squaring text blocks with other language's raw strings (where no special sequences exist), this is your wake-up call!
Text blocks work just like regular Java string literals except that they have a different delimiter (allowing you to forego `\"` in most cases) and can span several lines (making `\n` unnecessary).

### Newline Details

Speaking of newlines... No matter what line-ending policy your source files use, the compiler will always behave the same.
In fact, the first thing it does is normalizing "real" line breaks (i.e.
not those added with escape sequences) to LF (`\n` / `\u000A`).
So no matter whether your files use CR, CRLF (Windows), or LF (Unix), your text blocks will always use LF, i.e.
their lines end in `\n`.

<pullquote>No matter whether your source files use CR, CRLF, or LF, your text blocks always use LF</pullquote>

After the compiler normalized line endings and managed indentation, it expands escape sequences (like discussed earlier) and you can use that to achieve the line endings you need:

```java
String windows = """
	Windows\r
	line\r
	endings\r
	"""
```

### Even More Like Literals

Two more on the topic of *text blocks are like string literals* (I promise, they're the last):

-   whether you create a string with a literal or a text block will not be visible in the resulting bytecode and thus also not at run time, e.g. via reflection
-   literals and text blocks are so much the same, that they can be identical

Regarding the last point, this prints true twice:

```java
String hello = """
	Hello, text blocks!""";
String literal = "Hello, text blocks!";
System.out.println("equal: " + hello.equals(literal));
System.out.println("identical: " + (hello == literal));
```

The reason is that the compiler *interns* strings into to a pool to reduce memory consumption (turns out we use a lot of the same strings all over the place) and since Java 13 this includes text blocks.

### Orthogonality Of "Line-ness" and "Raw-ness"

That literals and text blocks are indistinguishable after compilation has a really interesting and absolutely intended effect: The "multiline-ness" of text blocks (vs the "single-line-ness" of literals) is independent of other `String`-related features.

Take raw strings as an example, which know no escape sequences.
At some point we may get them in Java, say by prefixing `___` to a string (I made that syntax up on the spot - there's zero chance of it becoming reality).
Then you can combine that with both literals or text blocks:

```java
String rawLiteral = ___"\f\o\o";
String rawBlock = ___"""
	\f\o\o
	""";
```

Looks like a flexible way to combine these features.
Let's just hope they'll be fully orthogonal and no surprising connections between "line-ness" and "raw-ness" crop up that we need to know about.

### Interpolation Of Variables And Expressions

The opposite direction of making strings raw, i.e.
less processed, is to give them more processing power, for example by letting them interpolate variables or even expressions:

```java
String greeting = "Hello";
// not a thing
String phrase = "${greeting}, world!"
// even less a thing
String phrase = "${greetingService.getGreeting()}, world!"
```

In string literals that's not too horrible because concatenation is somewhat acceptable.

```java
String adjective = "single-line";
String phrase = "Hello " + adjective + " text blocks!" ;
```

Because of the text block delimiters' reliance on newlines, this is not true for text blocks, though:

```java
String adjective = "multiline";
// ugh!
String phrase = """
	Hello
	""" +
	adjective +
	"""
	text blocks!
	"""
```

In this new context, the approach that was barely acceptable for string literals becomes even less so.
Possible solutions are `MessageFormat::format` and `String::format`.
Or the new instance method `String::formatted`:

```java
String adjective = "multiline";
String phrase = """
	Hello
	%s
	text blocks!
	""".formatted(adjective);
```

Calling `"Value: %s".formatted(value)` is equivalent to `String.format("Value: %s", value)`, but a little more convenient.
I like it!
(And am already looking forward to mass-search-replace `format` with `formatted`.
😁)

## Reflection

Java 13, due in September 2019, contains text blocks as a preview feature.
A text block:

-   begins with `"""` followed by a newline (that newline is of course not part of the resulting string, but additional newlines are)
-   ends with `"""` on the last line of content or on its own line (which adds a `\n` to the end of the string and allows adding indentation)

To manage indentation with the closing `"""`, position it relative to the content lines.
Each indent of the content lines to the right of the `"""` show up in the final string.

JEP 355 and I disagree on how to align delimiters and content:

-   The JEP suggests to align the content with the opening delimiter and move the closing delimiter to the left to add indentation.
-   I strongly recommend to let your formatter place the content and closing delimiter as it usually does for statements that span several lines and then you move the content to the right to add indentation.

Fortunately the JEP's style seems to have been a fluke.
Oracle's official [Programmer's Guide To Text Blocks](http://cr.openjdk.java.net/~jlaskey/Strings/TextBlocksGuide_v9.html) does not endorses the same style as I - and is generally a good source to read up on text blocks.

New delimiters and "multiline-ness" aside, text blocks are just like string literals:

-   escape sequences are translated
	(but `\"` and `\n` are discouraged)
-   the closing delimiter needs to be escaped
	(use `\"""` - the only place to use `\"` at all)
-   strings created from text blocks are interned

Be aware that the compiler normalizes all line breaks in the source file to LF (`\n` / `\u000A`).
It does so before translating escape sequences, which means `\r` can be added manually.

Java 12 and 13 also added a few methods to `String`:

-   `stripIndent` - an instance method that removes incidental indentation like the compiler
-   `indent` - an instance method to add spaces to each line of a string
-   `translateEscapes` - a static method to turn a string `"\\t"` into `"\t"`
-   `formatted` - an instance method behaving exactly like the static `String::format`

