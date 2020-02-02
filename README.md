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
* open _genealogy_ for tests:
	```xml
			<plugin>
				<artifactId>maven-surefire-plugin</artifactId>
				<configuration>
					<argLine>
						--add-opens=org.codefx.java_after_eight.genealogy/org.codefx.java_after_eight=ALL-UNNAMED
						--add-opens=org.codefx.java_after_eight.genealogy/org.codefx.java_after_eight.article=ALL-UNNAMED
						--add-opens=org.codefx.java_after_eight.genealogy/org.codefx.java_after_eight.genealogist=ALL-UNNAMED
						--add-opens=org.codefx.java_after_eight.genealogy/org.codefx.java_after_eight.genealogy=ALL-UNNAMED
						--add-opens=org.codefx.java_after_eight.genealogy/org.codefx.java_after_eight.recommendation=ALL-UNNAMED
					</argLine>
				</configuration>
			</plugin>
	```

#### APIs

* collection factories:
	* search `Arrays.asList` (better because truly immutable)
	* creation of field `weights` in `GenealogyTests` and `RelationTests` (remove constructors)
	* two non-null `weightMap`s in `WeightsTests`
	* in `Weights::allEqual`
* `Stream`:
	* in `ArticleFactory::extractFrontMatter` and `extractContent` use `Stream::dropWhile` and `Stream::takeWhile`
* `Optional`:
	* * `ifPresentOrElse` in `Main::main`
	* `or` in `ProcessDetails::getPid`
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
* `Collectors::toUnmodifiable...`: search `toList()`, `toMap(`, `toSet()`

#### JVM

* Application Class-Data Sharing (see cds.sh):

	```sh
	java -XX:+UnlockDiagnosticVMOptions -Xshare:dump \
		-XX:SharedArchiveFile=cds-jdk.jsa
	./stats-time.sh java -Xlog:class+load:file=cds-jdk.log \
		-XX:+UnlockDiagnosticVMOptions -Xshare:on \
		-XX:SharedArchiveFile=cds-jdk.jsa \
		-cp jars/genealogy.jar:jars/genealogists.jar org.codefx.java_after_eight.Main
	```


### Java 11

* `String::strip` fixes recommendations for _Code-First Java 9 Tutorial_ (compare tags to _Java 9 Resources - Talks, Articles, Repos, Blogs, Books And Courses_):
	* replace all `String::trim` with `strip`
	* replace `String::isEmpty` with `isBlank`
* `Path::of` instead of `Path::get` in Config
* `toArray(String[]::new)` instead of `toArray(new String[0])` in `Config::readConfig`
* use `Predicate::not`:
	* in `Tag::from`
	* in `ArticleFactory::extractFrontmatter` after adding `map(String::strip)`

### Java 12

Need to update ASM dependency of Maven Surefire:

```xml
<dependencies>
	<dependency>
		<groupId>org.ow2.asm</groupId>
		<artifactId>asm</artifactId>
		<version>7.3.1</version>
	</dependency>
</dependencies>
```

* `Collectors::teeing` in `Relation::aggregate`
* `CompletableFuture` in `Config` - instead of `exceptionally`:
	* first `exceptionallyAsync​`
	* then `exceptionallyCompose​`(`Async`)
* CDS archive for JDK classes is included: turn off with `-Xshare:off`

### Java 13

* bump to Java 13
	* add to parent POM
		```xml
		<plugin>
			<artifactId>maven-compiler-plugin</artifactId>
			<configuration>
				<compilerArgs>
					<arg>--enable-preview</arg>
				</compilerArgs>
			</configuration>
		</plugin>
		```
	* add `--enable-preview` to Surefire in genealogy and CDS scripts

* text blocks in `Main::recommendationsToJson`:
	```java
		var frame = """
				[
				$RECOMMENDATIONS
				]
				""";
		var recommendation = """
					{
						"title": "$TITLE",
						"recommendations": [
				$RECOMMENDED_ARTICLES
						]
					}
				""";
		var recommendedArticle = """
				\t\t\t{ "title": "$TITLE" }""";
	```
* use `String::formatted` instead of `String::format` (search for `format(`)
* AppCDS archive automatically generated: create with `-XX:ArchiveClassesAtExit=cds/app.jsa`

### Java 14

* record intro - replace `Map.Entry` in `ArticleFactory::createArticle`

	```java
	private static class FrontMatterLine {

		private final String key;
		private final String value;

		FrontMatterLine(String key, String value) {
			this.key = key;
			this.value = value;
		}

		String key() {
			return key;
		}

		String value() {
			return value;
		}

		// equals, hashCode, toString?

	}
	```

* records:
	* static factories, deprecated constructor:
		* `Description`
		* `Title`
		* `RelationType`
		* `TypedRelation`
	* static factories, non-public customized constructor:
		* `Tag`
		* `Relation`
	* static factories, unsuitable constructor:
		* `Config`
	* customized - `Recommendation`:
		* constructor
		* static factory
		* getter for `recommendedArticles` to create immutable copy
	* customized - `Article`:
		* constructor
		* static factory
		* getter for `tags` to create immutable copy
		* custom `equals`/`hashCode`
	* implements interface
		* `Slug`
	* method-local: `Genealogy::inferTypedRelations`, `Relation::aggregate`
	* counterpoint: `Weights` does not expose its fields
* pattern matching in `Article::equals`
* helpful NPE messages:
	* let `fromRawConfig` return `null`
	* add command line flag `-XX:+ShowCodeDetailsInExceptionMessages`
