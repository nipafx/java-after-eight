---
title: "Multiple Return Statements"
tags: [clean-code, java-basics]
date: 2014-12-03
slug: java-multiple-return-statements
description: "An argument for using multiple return statements in a method (as opposed to adhering to the single return myth)."
searchKeywords: "multiple return statements"
featuredImage: multiple-return-statements
---

I once heard that in the past people strived for methods to have a single exit point.
I understood this was an outdated approach and never considered it especially noteworthy.
But lately I've come in contact with some developers who still adhere to that idea (the last time was [here](http://www.yegor256.com/2014/10/26/hacker-vs-programmer-mentality.html#comment-1698685823) and it got me thinking.

So for the first time, I really sat down and compared the two approaches.

The first part of this post repeats the arguments for and against multiple return statements.
It also identifies the critical role clean code plays in assessing these arguments.
The second part categorizes the situations which benefit from returning early.

## The Discussion

I'm discussing whether a method should always run to its last line, from where it returns its result, or can have multiple return statements and "return early".

This is no new discussion of course.
See, for example, [Wikipedia](http://en.wikipedia.org/wiki/Return_statement#Multiple_return_statements), [Hacker Chick](http://www.hackerchick.com/2009/02/religious-war-48293-single-vs-multiple.html "Appy Fichtner on Single Vs.
Multiple Return Statements") or [StackOverflow](http://stackoverflow.com/q/36707/2525313 "Should a function have only one return statement?
at StackOverflow").

### Structured Programming

The idea that a single return statement is desirable stems from the paradigm of [structured programming](https://en.wikipedia.org/wiki/Structured_programming), developed in the 1960s.
Regarding subroutines, it promotes that they have a single entry and a single exit point.
While modern programming languages guarantee the former, the latter is somewhat outdated for several reasons.

The main problem the single exit point solved were memory or resource leaks.
These occurred when a return statement somewhere inside a method prevented the execution of some cleanup code which was located at its end.
Today, much of that is handled by the language runtime (e.g. garbage collection) and explicit cleanup blocks can be written with try-catch-finally.
So now the discussion mainly revolves around readability.

### Readability

Sticking to a single return statement can lead to increased nesting and require additional variables (e.g. to break loops).
On the other hand, having a method return from multiple points can lead to confusion as to its control flow and thus make it less maintainable.
It is important to notice that these two sides behave very differently with respect to the overall quality of the code.

Consider a method which adheres to clean coding guidelines: it is short and to the point with a clear name and an intention revealing structure.
The relative loss in readability by introducing more nesting and more variables is very noticeable and might muddy the clean structure.
But since the method can be easily understood due to its brevity and form, there is no big risk of overlooking any return statement.
So even in the presence of more than one, the control flow remains obvious.

Contrast this with a longer method, maybe part of a complicated or optimized algorithm.
Now the situation is reversed.
The method already contains a number of variables and likely some levels of nesting.
Introducing more has little relative cost in readability.
But the risk of overlooking one of several returns and thus misunderstanding the control flow is very real.

So it comes down to the question whether methods are short and readable.
If they are, multiple return statements will generally be an improvement.
If they aren't, a single return statement is preferable.

### Other Factors

Readability might not be the only factor, though.

Another aspect of this discussion can be logging.
In case you want to log return values but do not resort to aspect oriented programming, you have to manually insert logging statements at the methods' exit point(s).
Doing this with multiple return statements is tedious and forgetting one is easy.

Similarly, you might want to prefer a single exit point if you want to assert certain properties of your results before returning from the method.

## Situations For Multiple Returns Statements

There are several kinds of situations in which a method can profit from multiple return statements.
I tried to categorize them here but make no claim to have a complete list.
(If you come up with another recurring situation, leave a comment and I will include it.)

Every situation will come with a code sample.
Note that these are shortened to bring the point across and can be improved in several ways.

### Guard Clauses

[Guard clauses](http://c2.com/cgi/wiki?GuardClause) stand at the beginning of a method.
They check its arguments and for certain special cases immediately return a result.

```java
private Set<T> intersection(Collection<T> first, Collection<T> second) {
	// intersection with an empty collection is empty
	if (isNullOrEmpty(first) || isNullOrEmpty(second))
		return new HashSet<>();

	return first.stream()
			.filter(second::contains)
			.collect(Collectors.toSet());
}
```

Excluding edge cases at the beginning has several advantages:

-   it cleanly separates handling of special cases and regular cases, which improves readability
-   it provides a default location for additional checks, which preserves readability
-   it makes implementing the regular cases less error prone
-   it might improve performance for those special cases (though this is rarely relevant)

Basically all methods for which this pattern is applicable will benefit from its use.

A noteworthy proponent of guard clauses is Martin Fowler, although I would consider [his example](http://refactoring.com/catalog/replaceNestedConditionalWithGuardClauses.html) on the edge of branching (see below).

### Branching

Some methods' responsibilities demand to branch into one of several, often specialized subroutines.
It is usually best to implement these subroutines as methods in their own right.
The original method is then left with the only responsibility to evaluate some conditions and call the correct routine.

```java
public Offer makeOffer(Customer customer) {
	boolean isSucker = isSucker(customer);
	boolean canAffordLawSuit = customer.canAfford(
			legalDepartment.estimateLawSuitCost());

	if (isSucker) {
		if (canAffordLawSuit)
			return getBigBucksButStayLegal(customer);
		else
			return takeToTheCleaners(customer);
	} else {
		if (canAffordLawSuit)
			return getRid(customer);
		else
			return getSomeMoney(customer);
	}
}
```

(I know that I could leave out all `else`-lines.
Someday I might write a post explaining why in cases like this, I don't.)

Using multiple return statements has several advantages over a result variable and a single return:

-   the method more clearly expresses its intend to branch to a subroutine and simply return its result
-   in any sane language, the method does not compile if the branches do not cover all possibilities (in Java, this can also be achieved with a single return if the variable is not initialized to a default value)
-   there is no additional variable for the result, which would span almost the whole method
-   the result of the called method can not be manipulated before being returned (in Java, this can also be achieved with a single return if the variable is `final` and its class immutable; the latter is not obvious to the reader, though)
-   if [a switch statement](https://www.sitepoint.com/javas-switch-statement/) is used in a language with *fall through* (like Java), immediate return statements save a line per case because no `break` is needed, which reduces boilerplate and improves readability

This pattern should only be applied to methods which do little else than branching.
It is especially important that the branches cover all possibilities.
This implies that there is no code below the branching statements.
If there were, it would take much more effort to reason about all paths through the method.
If a method fulfills these conditions, it will be small and cohesive, which makes it easy to understand.

### Cascading Checks

Sometimes a method's behavior mainly consists of multiple checks where each check's outcome might make further checks unnecessary.
In that case, it is best to return as soon as possible (maybe after each check).

```java
private Element getAnchorAncestor(Node node) {
	// if there is no node, there can be no anchor,
	// so return null
	if (node == null)
		return null;

	// only elements can be anchors,
	// so if the node is no element, recurse to its parent
	boolean nodeIsNoElement = !(node instanceof Element);
	if (nodeIsNoElement)
		return getAnchorAncestor(node.getParentNode());

	// since the node is an element, it might be an anchor
	Element element = (Element) node;
	boolean isAnchor = element.getTagName().equalsIgnoreCase("a");
	if (isAnchor)
		return element;

	// if the element is no anchor, recurse to its parent
	return getAnchorAncestor(element.getParentNode());
}
```

Other examples of this are the usual implementations of `equals` or `compareTo` in Java.
They also usually consist of a cascade of checks where each check might determine the method's result.
If it does, the value is immediately returned, otherwise the method continues with the next check.

Compared to a single return statement, this pattern does not require you to jump through hoops to prevent ever deeper indentation.
It also makes it straight forward to add new checks and place comments before a check-and-return block.

As with branching, multiple return statements should only be applied to methods which are short and do little else.
The cascading checks should be their central, or better yet, their only content (besides input validation).
If a check or the computation of the return value needs more than two or three lines, it should be refactored into a separate method.

### Searching

Where there are data structures, there are items with special conditions to be found in them.
Methods which search for them often look similar.
If such a method encounters the item it was searching for, it is often easiest to immediately return it.

```java
private <T> T findFirstIncreaseElement(
		Iterable<T> items, Comparator<? super T> comparator) {
	T lastItem = null;
	for (T currentItem : items) {
		boolean increase = increase(lastItem, currentItem, comparator);
		lastItem = currentItem;

		if (increase) {
			return currentItem;
		}
	}

	return null;
}
```

Compared to a single return statement, this saves us from finding a way to get out of the loop.
This has the following advantages:

-   there is no additional boolean variable to break the loop
-   there is no additional condition for the loop, which is easily overlooked (especially in for loops) and thus fosters bugs
-   the last two points together keep the loop much easier to understand
-   there is most likely no additional variable for the result, which would span almost the whole method

Like most patterns which use multiple return statements, this also requires clean code.
The method should be small and have no other responsibility but searching.
Nontrivial checks and result computations should have their own methods.

## Reflection

We have seen the arguments for and against multiple returns statements and the critical role clean code plays.
The categorization should help to identify recurring situations in which a method will benefit from returning early.
