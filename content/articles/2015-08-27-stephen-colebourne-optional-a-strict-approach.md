---
title: "Java 8 SE Optional, a strict approach"
tags: [java-8, optional]
date: 2015-08-27
slug: stephen-colebourne-java-optional-strict-approach
description: "Stephen Colebourne presented his pragmatic approach to using Optional. I argue for a stricter one that gets us further without considerable downsides."
searchKeywords: "Stephen Colebourne Optional"
featuredImage: optional-a-strict-approach
source: "I created three gists, which I present throughout the post: the same example in [Stephen's version](https://gist.github.com/nicolaiparlog/5b2b7c84d71426a368c9), [my basic version](https://gist.github.com/nicolaiparlog/230614b4e0a1d11b89dd), and [my extended version](https://gist.github.com/nicolaiparlog/20d7e831bf3585c3aaca)."
---

About two weeks ago Stephen Colebourne presented [his pragmatic approach](http://blog.joda.org/2015/08/java-se-8-optional-pragmatic-approach.html) to using `Optional`.
If you read it, you might have guessed from [my previous recommendations](intention-revealing-code-java-8-optional) that I don't agree.

All quotes that are not attributed to somebody else are taken from Stephen's post.
While not strictly necessary I recommend to read it first.
But don't forget to come back!

## Disclaimer

Stephen Colebourne is a Java legend.
Quoting Markus Eisele's [Heroes of Java](http://blog.eisele.net/2012/09/the-heroes-of-java-stephen-colebourne.html) post about him:

> Stephen Colebourne is a Member of Technical Staff at OpenGamma.
> He is widely known for his work in open source and his blog.
> He created Joda-Time which is now being further developed as JSR-310/ThreeTen.
> He contributes to debates on the future of Java, including proposals for the diamond operator for generics and FCM closures, both of which are close to the adopted changes in Java 7 and 8.
> Stephen is a frequent conference speaker, JavaOne Rock Star and Java Champion.

I had the pleasure to contribute to Stephen's [Property Alliance](https://github.com/jodastephen/property-alliance) and this reinforced my opinion of him as an extremely competent developer and a very deliberate person.

All of which goes to say that if in doubt, trust him over me.

Then there is the fact, that his approach is rooted in the axiom that `Optional` should solely be used as a return type.
This is absolutely in line with the recommendations of those who introduced the class in the first place.
[Quoting Brian Goetz](https://stackoverflow.com/a/26328555/2525313):

> Of course, people will do what they want.
> But we did have a clear intention when adding this feature, and it was not to be a general purpose Maybe or Some type, as much as many people would have liked us to do so.
> Our intention was to provide a limited mechanism for library method return types where there needed to be a clear way to represent "no result", and using `null` for such was overwhelmingly likely to cause errors.
>
> \[...\] You should almost never use it as a field of something or a method parameter.

So if in doubt, trust his opinion over mine.

## Juxtaposition

Of course, even better than to just trust anyone is to make up your own mind.
So here are my arguments in contrast to Stephen's.

### Basic Points

These are Stephen's five basic points:

> 1. Do not declare any instance variable of type `Optional`.
> 2. Use `null` to indicate optional data within the private scope of a class.
> 3. Use `Optional` for getters that access the optional field.
> 4. Do not use `Optional` in setters or constructors.
> 5. Use `Optional` as a return type for any other business logic methods that have an optional result.

Here are mine:

1. Design your code to avoid optionality wherever feasibly possible.
2. In all remaining cases, prefer `Optional` over `null`.

### Examples

Let's compare examples.
His is:

```java
public class Address {

	private final String addressLine;  // never null
	private final String city;         // never null
	private final String postcode;     // optional, thus may be null

	// constructor ensures non-null fields really are non-null
	// optional field can just be stored directly, as null means optional
	public Address(String addressLine, String city, String postcode) {
		this.addressLine = Preconditions.chckNotNull(addressLine);
		this.city = Preconditions.chckNotNull(city);
		this.postcode = postcode;
	}

	// normal getters
	public String getAddressLine() {
		return addressLine;
	}

	public String getCity() {
		return city;
	}

	// special getter for optional field
	public Optional<String> getPostcode() {
		return Optional.ofNullable(postcode);
	}

	// return optional instead of null for business logic methods that may not find a result
	public static Optional<Address> findAddress(String userInput) {
		return... // find the address, returning Optional.empty() if not found
	}

}
```

I like that no consumer of this class can receive `null`.
I dislike how you still have to deal with it - within the class but also without.

This would be my (basic) version:

```java
public class Address {

	// look ma, no comments required

	private final String addressLine;
	private final String city;
	private final Optional<String> postcode;

	// nobody has to look at this constructor to check which parameters are
	// allowed to be null because of course none are!

	public Address(String addressLine, String city, Optional<String> postcode) {
		this.addressLine = requireNonNull(addressLine,
				"The argument 'addressLine' must not be null.");
		this.city = requireNonNull(city,
				"The argument 'city' must not be null.");
		this.postcode = requireNonNull(postcode,
				"The argument 'postcode' must not be null.");
	}

	// of course methods that might not have a result
	// return 'Optional' instead of null

	public static Optional<Address> findAddress(String userInput) {
		// find the address, returning Optional.empty() if not found
	}

	// getters are straight forward and can be generated

	public String getAddressLine() {
		return addressLine;
	}

	public String getCity() {
		return city;
	}

	// look how the field's type matches the getter's type;
	// nice for bean-based code/tools

	public Optional<String> getPostcode() {
		return postcode;
	}

}
```

There are simply no nulls, here.

### Differences

#### A Constrained Problem

> Within the object, the developer is still forced to think about `null` and manage it using `!= null` checks.
This is reasonable, as the problem of null is constrained.
The code will all be written and tested as a unit (you do write tests don't you?), so nulls will not cause many issues.

Do you see how his constructor allows one of the arguments to be `null`?
And the only way to find out which one requires you to leave what you are doing and look at some other class' code.
This is no big thing but unnecessary nonetheless.

Even leaving this aside, the problem is not as constrained as it should be.
Assuming that [everybody hates comments](comment-your-fucking-code), we have to assume they are not there, which leaves the constructor internals and the getter's return type to tell you that the field is nullable.
Not the best places for this information to jump out at you.

```java
public class Address {

	// look ma, no comments required

	private final String addressLine;
	private final String city;
	private Optional<String> postcode;

	// nobody has to look at these constructors to check which parameters are
	// allowed to be null because of course none are!

	public Address(String addressLine, String city, Optional<String> postcode) {
		this.addressLine = requireNonNull(addressLine,
				"The argument 'addressLine' must not be null.");
		this.city = requireNonNull(city,
				"The argument 'city' must not be null.");
		this.postcode = requireNonNull(postcode,
				"The argument 'postcode' must not be null.");
	}

	public Address(String addressLine, String city, String postcode) {
		// use 'requireNonNull' inside Optional factory method
		// if you prefer a verbose exception message;
		// otherwise 'Optional.of(postcode)' suffices
		this(addressLine, city, Optional.of(
				requireNonNull(postcode,
						"The argument 'postcode' must not be null.")));
	}

	public Address(String addressLine, String city) {
		this(addressLine, city, Optional.empty());
	}

	// now if some method needs to use the postcode,
	// we can not overlook the fact that it is optional

	public int comparePostcode(Address other) {
		// without Optionals we might overlook that the postcode
		// could be missing and do this:
		// return this.postcode.compareTo(other.postcode);

		if (this.postcode.isPresent() && other.postcode.isPresent())
			return this.postcode.get().compareTo(other.postcode.get());
		else if (this.postcode.isPresent())
			return 1;
		else if (other.postcode.isPresent())
			return -1;
		else
			return 0;
	}

	// of course methods that might not have a result
	// return 'Optional' instead of null

	public static Optional<Address> findAddress(String userInput) {
		// find the address, returning Optional.empty() if not found
	}

	// getters are straight forward and can be generated

	public String getAddressLine() {
		return addressLine;
	}

	public String getCity() {
		return city;
	}

	// look how the field's type matches the getter's type;
	// nice for bean-based code/tools

	public Optional<String> getPostcode() {
		return postcode;
	}

	// in case this 'Address' is mutable
	// (which it probably shouldn't be but let's presume it is)
	// you can decide whether you prefer a setter that takes an 'Optional',
	// a pair of methods to set an existing and an empty postcode, or both

	public void setPostcode(Optional<String> postcode) {
		this.postcode = requireNonNull(postcode,
				"The argument 'postcode' must not be null.");
	}

	public void setPostcode(String postcode) {
		// again you might want to use 'requireNonNull'
		// if you prefer a verbose exception message;
		this.postcode = Optional.of(
				requireNonNull(postcode,
						"The argument 'postcode' must not be null."));
	}

	public void setEmptyPostcode() {
		this.postcode = Optional.empty();
	}

}
```

His argument for tests might get crushed by numbers.
If all tests include all fields, each optional field would double the number of tests as each should be run for the null and the non-null case.
I'd prefer having the type system as a first line of defense here.

On the other hand, this pain might convince the developer to maybe find a solution with less optionality within a single class.

#### Performance

Stephen correctly points out that an instance created for a method return value that is then quickly discarded (which is typical for uses of `Optional`) has little to no costs.
Unlike an `Optional` field, which exists for the entire life time of the containing object and adds an additional layer of indirection from that object to the `Optional`'s payload.

For him this is a reason to prefer `null`.

> While it is easy to claim this is "premature optimization", as engineers it is our responsibility to know the limits and capabilities of the system we work with and to choose carefully the point where it should be stressed.

I agree.
But to me part of choosing carefully means to profile first.
And if someone shows me convincing arguments that in his concrete case replacing some `Optional` fields with nullable fields causes a noticeable performance gain, I'd rip them stupid boxes right out.
But in all other cases I stick with the code I consider more maintainable.

By the way, the same argument could be made for using arrays instead of `ArrayList`s or `char`-arrays instead of strings.
I'm sure nobody would follow that advice without considerable performance gains.

This recurring topic in the discussion deserves some attention, though.
I will try to find some time to profile some use cases that I think would be interesting.

#### Serializability

> While it is a minor point, it should be noted that the class could be `Serializable`, something that is not possible if any field is `Optional` (as `Optional` does not implement `Serializable`).

I consider [this to be solved](serialize-java-optional#fields-of-type-optional).
Causes a little extra work, though.

#### Convenience

> \[I\]t is my experience that having `Optional` on a setter or constructor is annoying for the caller, as they typically have the actual object.
Forcing the caller to wrap the parameter in `Optional` is an annoyance I'd prefer not to inflict on users.
(ie.
convenience trumps strictness on input)

While writing annoying code can be fun I see his point.
So don't force users, [overload your methods](http://blog.jooq.org/2015/07/28/java-8s-method-references-put-further-restrictions-on-overloading/):

```java
```

Of course this doesn't scale well with many optional fields.
In that case, the builder pattern will help.

Then there is the fact that if our nullable postcode has a setter, the developer working on some other code must again stop and come looking at this class to determine whether she can pass `null`.
And since she can never be sure, she has to check for other getters, too.
Talking about annoying code...

With a field of type `Optional` the setter could look like this:

```java
public class Address {

	// look ma, no comments required

	private final String addressLine;
	private final String city;
	private Optional<String> postcode;

	// nobody has to look at these constructors to check which parameters are
	// allowed to be null because of course none are!

	public Address(String addressLine, String city, Optional<String> postcode) {
		this.addressLine = requireNonNull(addressLine,
				"The argument 'addressLine' must not be null.");
		this.city = requireNonNull(city,
				"The argument 'city' must not be null.");
		this.postcode = requireNonNull(postcode,
				"The argument 'postcode' must not be null.");
	}

	public Address(String addressLine, String city, String postcode) {
		// use 'requireNonNull' inside Optional factory method
		// if you prefer a verbose exception message;
		// otherwise 'Optional.of(postcode)' suffices
		this(addressLine, city, Optional.of(
				requireNonNull(postcode,
						"The argument 'postcode' must not be null.")));
	}

	public Address(String addressLine, String city) {
		this(addressLine, city, Optional.empty());
	}

	// now if some method needs to use the postcode,
	// we can not overlook the fact that it is optional

	public int comparePostcode(Address other) {
		// without Optionals we might overlook that the postcode
		// could be missing and do this:
		// return this.postcode.compareTo(other.postcode);

		if (this.postcode.isPresent() && other.postcode.isPresent())
			return this.postcode.get().compareTo(other.postcode.get());
		else if (this.postcode.isPresent())
			return 1;
		else if (other.postcode.isPresent())
			return -1;
		else
			return 0;
	}

	// of course methods that might not have a result
	// return 'Optional' instead of null

	public static Optional<Address> findAddress(String userInput) {
		// find the address, returning Optional.empty() if not found
	}

	// getters are straight forward and can be generated

	public String getAddressLine() {
		return addressLine;
	}

	public String getCity() {
		return city;
	}

	// look how the field's type matches the getter's type;
	// nice for bean-based code/tools

	public Optional<String> getPostcode() {
		return postcode;
	}

	// in case this 'Address' is mutable
	// (which it probably shouldn't be but let's presume it is)
	// you can decide whether you prefer a setter that takes an 'Optional',
	// a pair of methods to set an existing and an empty postcode, or both

	public void setPostcode(Optional<String> postcode) {
		this.postcode = requireNonNull(postcode,
				"The argument 'postcode' must not be null.");
	}

	public void setPostcode(String postcode) {
		// again you might want to use 'requireNonNull'
		// if you prefer a verbose exception message;
		this.postcode = Optional.of(
				requireNonNull(postcode,
						"The argument 'postcode' must not be null."));
	}

	public void setEmptyPostcode() {
		this.postcode = Optional.empty();
	}

}
```

Again, all `null` values are immediately answered with an exception.

#### Beans

> On the downside, this approach results in objects that are not beans.

Yep.
Having a field of type `Optional` doesn't suffer from that.

### Commonalities

It should not be overlooked that we're discussing details here.
Our goal is the same and we're proposing similar ways of getting there.

> If adopted widely in an application, the problem of `null` tends to disappear without a big fight.
Since each domain object refuses to return `null`, the application tends to never have `null` passed about.
In my experience, adopting this approach tends to result in code where `null` is never used outside the private scope of a class.
And importantly, this happens naturally, without it being a painful transition.
Over time, you start to write less defensive code, because you are more confident that no variable will actually contain `null`.

This is a great goal to achieve!
And following Stephen's advice will get you most of the way there.
So don't take my disagreement as a reason to not use `Optional` at least that much.

All I'm saying is that I see little reason to stop short of banning `null` even more!

## Reflection

I addressed and hopefully refuted a number of arguments against using `Optional` whenever something is nullable.
I hope to have shown that my stricter approach goes further in exorcising `null`.
This should [free up your mind](intention-revealing-code-java-8-optional#the-effects) to think about more relevant problems.

The price to pay might be a shred of performance.
If someone proves that it is more, we can still return to `null` for those specific cases.
Or throw hardware at the problem.
Or wait for [value types](tag:primitive-classes).

What do you think?
