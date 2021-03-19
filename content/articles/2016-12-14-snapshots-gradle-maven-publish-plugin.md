---
title: "Publishing Snapshots With Gradle's maven-publish Plugin"
tags: [tools]
date: 2016-12-14
slug: snapshots-gradle-maven-publish-plugin
description: "A step by step tutorial how to use Gradle and the maven-publish plugin to publish snapshots to Sonatype's Maven snapshot repo."
intro: "For a new project I decided to use Gradle. Here's how I set it up with the incubating maven-publish plugin to publish snapshots to Sonatype's repository."
searchKeywords: "maven-publish"
featuredImage: gradle-publish-snapshot-maven-publish
---

I've recently started [a new project](https://github.com/junit-pioneer/junit-pioneer) with Gradle and decided to jump straight in - no Gradle experience, no clue about Groovy, no tutorials, just hammer on it until it works.
That went surprisingly well until I decided to publish snapshots to Sonatype's Maven snapshot repository with the incubating [*maven-publish* plugin](https://docs.gradle.org/current/userguide/publishing_maven.html) - that took, ahh, a little convincing.

<admonition type="caveat">

As I said, I'm a noob in both Groovy and Gradle, so don't believe anything I say.
I write this down for myself as much as for you.

</admonition>

The final (but still partial) `build.gradle` file can be found [here](https://gist.github.com/nicolaiparlog/d1850ac402f086e1b4fc42f58f5aa365), the actual variant I used in my project [here](https://github.com/junit-pioneer/junit-pioneer/blob/3ab5fee6dfa2c8b99327d0d198fdaa58f044808e/build.gradle).

As a zeroth step make sure the project's group, id, and version are present.
The first and last can usually be found in the `build.gradle` file, the project name doubles for its id and is defined in `settings.gradle`.

## Activating *maven-publish*

Ok, lets go!
First of all I activated the plugin:

```java
apply plugin: 'maven-publish'
```

To start publishing things I need the following incantation:

```java
publishing {
	publications {
		mavenJava(MavenPublication) {
			from components.java
			// more goes in here
		}
	}
	repositories {
		mavenLocal()
	}
}
```

As you see I begin by publishing to the local repo.
And indeed, running `gradle publish` should now create a JAR and a rudimentary pom in some `.m2` subfolder.
From here on I can add more features step by step.

## Filling the POM

What do I need to publish an artifact?
A full Maven pom.
Since I don't have a `pom.xml`, where do I get it?
I create it with some Gradle XML API.
Obviously.
Why don't I use Maven to get the pom first hand?
Damned if I know...

So inside the `mavenJava` thingy (what is it?
a task, I guess?) I create the pom.
It took me a moment of trying this and that before settling on the following syntax:

```java
pom.withXml {
	asNode().with {
		appendNode('packaging', 'jar')
		appendNode('name', 'PROJECT_NAME')
		appendNode('description', 'PROJECT_DESCRIPTION')
		appendNode('url', 'PROJECT_URL')
		appendNode('scm').with {
			appendNode('url', 'SCM_URL_FOR_PEOPLE')
			appendNode('connection', 'SCM_URL_FOR_SCM')
		}
		appendNode('issueManagement').with {
			appendNode('url', 'ISSUE_TRACKER_URL')
			appendNode('system', 'ISSUE_TRACKER_NAME')
		}
		appendNode('licenses').with {
			appendNode('license').with {
				appendNode('name', 'LICENSE_NAME')
				appendNode('url', 'LICENSE_URL')
			}
		}
		appendNode('organization').with {
			appendNode('name', 'ORG_NAME')
			appendNode('url', 'ORG_URL')
		}
		appendNode('developers').with {
			appendNode('developer').with {
				appendNode('id', 'DEV_HANDLE')
				appendNode('name', 'DEV_NAME')
				appendNode('email', 'DEV_MAIL')
				appendNode('organization', 'ORG_NAME_AGAIN')
				appendNode('organizationUrl', 'ORG_URL_AGAIN')
				appendNode('timezone', 'UTC_OFFSET')
			}
		}
	}
}
```

Ok, there we go.
So much better than that ugly XML, right?
I read somewhere that there are more beautiful APIs I could use here but I didn't feel like going off on another tangent.
Feel free to propose something.

You might've noticed that the project group, id, and version do not need to be repeated.
Running `gradle publish` should now publish a JAR with a complete, albeit somewhat ugly pom.

## License and More

I want to add the project's license to the JAR's `META-INF` folder, so inside `mavenJava` I tell Gradle to include the file in every JAR task (or at least that's how I read it):

```java
tasks.withType(Jar) {
	from(project.projectDir) {
		include 'LICENSE.md'
		into 'META-INF'
	}
}
```

Looking good, `gradle publish` now creates a full pom and a JAR with the project's license.

## Sources and Javadoc JARs

Most projects like to publish more than just the compiled `.class` files, though, namely sources and Javadoc.
For this I add two tasks and reference them from `mavenJava`:

```java
publishing {
	publications {
		mavenJava(MavenPublication) {
			// ...
			artifact sourceJar
			artifact javadocJar
		}
	}
	// ...
}

task sourceJar(type: Jar, dependsOn: classes) {
	classifier 'sources'
	from sourceSets.main.allSource
}

task javadocJar(type: Jar, dependsOn: javadoc) {
	classifier = 'javadoc'
	from javadoc.destinationDir
}
```

Nice, now I get a full pom, an artifact for the project's classes and license, and JARs for sources and Javadoc.
Time to take the last step: publish to the snapshot repo!

## Publish To Snapshot Repository

For that I'll replace `mavenLocal()` with the actual repository.
Besides the URL I also need to specify my credentials:

```java
repositories {
	maven {
		url 'https://oss.sonatype.org/content/repositories/snapshots/'
		credentials {
			username 'user'
			password '123456'
		}
	}
}
```

Of course I wasn't planning to commit my password to source control so I went looking for an alternative.
I found one - not sure whether it's the best but, hey, it works.

You can define new project properties on the command line with the `-P` option.
So given a command like this...

```java
gradle publish -P snapshotRepoPass=123456
```

... I can then access `project.snapshotRepoPass` in the credentials:

```java
credentials {
	username 'user'
	password project.snapshotRepoPass
}
```

Sweet.

Until I realized that now all other tasks fail because the `credentials` object is always created and thus requires the property `snapshotRepoPass` to exist.
Something that is not the case for other tasks than *publish* because I see no reason to pass the repo password to, for example, a test run.
Soooo, I decided to define the property in the build file *if* it was not already defined due to the command line option:

```java
ext {
	// the password needs to be specified via command line
	snapshotRepoPass = project.hasProperty('snapshotRepoPass')
			? project.getProperty('snapshotRepoPass')
			: ''
	// it looks like the ternary operator can not actually be
	// split across lines; I do it here for artistic purposes
}
```

I could've put the same `hasProperty`/`getProperty` check into `credentials` but decided to create a separate spot where I implement this behavior.

With all of that done, I can indeed publish my project's current state to the Sonatype Maven snapshot repository.
Wohoo!

## Reflection

All in all it wasn't actually that bad.
The documentation was a little sparse and building an XML file in an API that made that even more verbose felt ridiculous but other than that it reads fairly straight forward.
It wasn't at the time but now it works so I should stop complaining.

Here's what I did:

-   Activate the plugin with `apply plugin: 'maven-publish'` and add a `publishing` node to `build.gradle`.
-   Fill the pom with those beautiful `asNode.appendNode` calls.
-   Include the license by appending the copy step to each JAR related task
-   Create tasks for source and Javadoc JARs and reference them from the `publications` node.
-   Specify the repository URL and add your credentials.

As I said before, you can check out two versions of the resulting `build.gradle` file: [an exemplary one](https://gist.github.com/nicolaiparlog/d1850ac402f086e1b4fc42f58f5aa365) consisting of exactly what we build here and [the real deal](https://github.com/junit-pioneer/junit-pioneer/blob/3ab5fee6dfa2c8b99327d0d198fdaa58f044808e/build.gradle).

I also managed to set up Travis CI to publish each successful build and will soon try to publish actual versions.
I'll write about both...
