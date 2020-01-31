# Java After Eight

Nice Java 8 code base that gets way nicer with Java 9-14.


## Applicable Java 9+ features

* general tip: use [sdkman](https://sdkman.io/)
* \u2001 (" ") is the culprit ~> 2001 - Odyssey in Space

### Java 9

#### Module system

* strong encapsulation:
	* in _genealogy_:
		* export `genealogist` and `articles`
	* in _genealogist_:
		* export nothing
* services:
	* update _genealogy_ and _genealogist_ accordingly
	* in `Main::getGenealogists`, use `ServiceLoader::stream`
	* point out that compiler now checks what kind of service can be provided
* debugging: show module name and versions in stack traces

#### Language

#### APIs

* collection factories:
	* search `Arrays.asList` (better because truly immutable)
	* creation of field `weights` in `GenealogyTests` and `RelationTests` (remove constructors)
	* all four `weightMap`s in `WeightsTests`
	* in `Weights::allEqual`
* `Stream`:
	* in `ArticleFactory::extractFrontMatter` and `extractContent` use `Stream::dropWhile` and `Stream::takeWhile`
* `Optional`:
	* `or` in `ProcessDetails::getPid`
	* * `ifPresentOrElse` in `Main::main`
* OS process API: replace `ProcessDetails::getPid`
* Java version API: replace `ProcessDetails::getMajorJavaVersion`


### Java 10

#### Language

* `var`
	* obvious / inconsequential:
		* `TagGenealogist::infer` for `Set<Tag>`
		* `Description::from`, `Title::from`
		* `Tag::from`, `Recommendation::from`
		* all over `Main`, `TagTests`, `TextParserTests`
		* test methods in `GenealogyTests`, `RelationTests`, `WeightTests`, `RecommenderTests`
	* discussion:
		* `ArticleFactory::createArticle` and `keyValuePairFrom`

#### APIs

* collection factories:
	* in `Weights` constructor use `Map::copyOf` (also remove following null checks)
* `Collectors::toUnmodfiable...`: search `toList()`, `toMap(`, `toSet()`
* `Stream`:
	* in `Tag::from` use `Collectors::toUnmodifiableList`

#### JVM

* Application Class-Data Sharing
	* create CDS
	* create AppCDS

### Java 11

* `String::strip` fixes recommendations for _Code-First Java 9 Tutorial_ (compare tags to _Java 9 Resources - Talks, Articles, Repos, Blogs, Books And Courses_):
	* replace all `String::trim` with `strip`
	* replace `String::isEmpty` with `isBlank`
* `Path::of` instead of `Path::get` in Config
* `toArray(String[]::new)` instead of `toArray(new String[0])` in `Config::readConfig`
* in `Tag::from` stream pipeline use `Predicate::not`

### Java 12

* try `Collectors::teeing` in `Relation::aggregate`
* CDS archive for JDK classes is included
* `CompletableFuture` in `Config` - instead of `exceptionally`:
	* first `exceptionallyAsync​`
	* then `exceptionallyCompose​`(`Async`)

### Java 13

* text blocks in `Main::recommendationsToJson`
* AppCDS archive automatically generated
* use `String::formatted` instead of `String::format`

### Java 14

* records:
	* counterpoint: `Weights` does not expose its fields
	* customized constructor:
		* `Article`
	* static factories, non-public simple constructor:
		* `Description`
		* `Slug`
		* `Title`
		* `RelationType`
	* static factories, customized constructor:
		* `Tag`
		* `Relation`
		* `TypedRelation`
	* custom getter:
		* `Article` for tags
		* Recommendation for recommended articles
	* custom `equals`:
		* `Article` (just slug)
	* class-local:
		* replace `Map.Entry` in `ArticleFactory::createArticle`
		* `Relation.UnfinishedRelation`
	* method-local: `Genealogy::inferTypedRelations`
* pattern matching in equals
* helpful NPE messages:
	* remove description from an article
	* add command line flag


## Additional Java 9+ features

### Java 9

* try with resources
* private interface methods
* `Optional::stream`
* stack walking (TODO: use Log4J 2)
* unified logging
* MR JARs
* String performance improvements

### Java 11

* `String::repeat` and `String::lines`
* `Optional::isEmpty`
* reactive HTTP/2 client (TODO: post results somewhere; project could use OkHttp, we replace)
* launch source files

### Java 12

* switch expressions (TODO: how?)
* `String::indent` and `String::transform`
* `NumberFormat::getCompactNumberInstance` (TODO: log word count per article in short format)

### Java 13

* switch expressions use `yield`
