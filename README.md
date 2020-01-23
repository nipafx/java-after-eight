# Java After Eight

Nice Java 8 code base that gets way nicer with Java 9-14.

## Possible Java 9+ features

A preselection of Java 9-14 features that may be interesting 

### Language features

* records
* text block
	* in tests, maybe XML?
	* String::indent
* switch expressions
	* something with fall-through
		* intentional ~> multiple labels
		* with `break` ~> remove `break`
	* over strings or enums
* `var`
* private interface methods
	* have an interface with default methods
	  that have to share code
* try on effectively final resource

### New APIs

* HTTP/2 client
	* project could use OkHttp, we replace
* version API
* collection factories

### Updates APIs

* String: lines, repeat, transform, formatted
* Stream: flatMap collector, teeingCollector, dropWhile, takeWhile
* NumberFormat.getCompactNumberInstance
* CompletableFuture: error recovery
* Predicate: not
* Optional: or

### Misc

* performance
	* (Graal VM - not specific to 9+)
	* AppCDS
	* log framework that uses StackWalking API
		* microbenchmark a hot loop with a log command
	* string improvements
* debugging
	* helpful NPE
	* module versions in stack traces
