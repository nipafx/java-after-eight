---
title: "First Release of JDeps Maven Plugin"
tags: [java-9, jdeps, tools, project-jigsaw]
date: 2015-05-11
slug: jdeps-maven-plugin-0-1
description: "The JDeps Maven Plugin will break a project's build if it contains dependencies on JDK-internal APIs. This helps to prepare for Java 9."
intro: "The JDeps Maven Plugin will break a project's build if it contains dependencies on JDK-internal APIs. This helps to prepare for Java 9, where these dependencies will be unaccessible."
searchKeywords: "JDeps Maven Plugin"
featuredImage: jdeps-mvn-motor
repo: jdeps-maven-plugin
---

Two weeks ago I wrote about [how Java 9 may break your code](how-java-9-and-project-jigsaw-may-break-your-code).
A substantial obstacle for the transition to Java 9 can be a project's dependencies on JDK-internal APIs.
These will be unaccessible in the new Java version and code containing them will not compile.
It is hence important to weed them out in the remaining time.
(Btw, Java 9 is [scheduled for September 2016](http://mail.openjdk.java.net/pipermail/jdk9-dev/2015-May/002172.html).)

To help our projects (and maybe yours) with that, I created the [*JDeps Maven Plugin*](https://github.com/nipafx/JDeps-Maven-Plugin).
It breaks the build if the code contains any problematic dependencies.

## JDeps

To help identify problematic dependencies the JDK 8 contains the [Java Dependency Analysis tool *jdeps*](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jdeps.html).
Run against a jar, a folder or a single class it will analyze and output dependencies.
Analysis and output can be configured with several command line options.

Run as `jdeps -jdkinternals` it will only list the dependencies on JDK-internal API.
Exactly these dependencies are the ones that would break if compiled against Java 9.
It is hence the basis of the Maven plugin.

## JDeps Maven Plugin

The plugin runs `jdeps -jdkinternals` against the compiled classes, parses the output and breaks the build if it contained any dependencies.

<contentimage slug="JDeps-Maven-Plugin-v0.1" options="sidebar"></contentimage>

### Configuration

To use it in a project include this in its pom:

```xml
<build>
	<plugins>
		...
		<plugin>
			<groupId>org.codefx.maven.plugin</groupId>
			<artifactId>jdeps-maven-plugin</artifactId>
			<version>0.1</version>
			<executions>
				<execution>
					<goals>
					    <goal>jdkinternals</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
		...
	</plugins>
</build>
```

The plugin is extremely simple and the current version neither allows nor requires any further configuration.

### Execution

The plugin will be executed during [the verify phase](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).
It will hence run in a full build with `mvn verify` (or any later phase).
With `mvn jdeps:jdkinternals` it can be run directly.

If your project contains any internal dependencies the build will fail with a message like this:

```
[ERROR] Failed to execute goal
		org.codefx.maven.plugin:jdeps-maven-plugin:0.1:jdkinternals
		(default-cli) on project MavenLab:
[ERROR] Some classes contain dependencies on JDK-internal API:
[ERROR] .       org.codefx.lab.ExampleClass
[ERROR] .                -> sun.misc.BASE64Decoder [JDK internal API, rt.jar]
[ERROR] .                -> sun.misc.Unsafe [JDK internal API, rt.jar]
[ERROR] .       org.codefx.lab.AnotherExampleClass
[ERROR] .                -> sun.misc.Unsafe [JDK internal API, rt.jar]
[ERROR] .       org.codefx.lab.foo.ExampleClassInAnotherPackage
[ERROR] .                -> sun.misc.BASE64Decoder [JDK internal API, rt.jar]
```

### Roadmap

The plugin fulfills the minimum requirements to be useful: it breaks the build if JDeps discovers any dependencies on JDK-internal APIs.
But this may be inconvenient for larger projects which might already contain some.
It would be nice to be able to configure the plugin so that it ignores certain known dependencies.

This is the next step.
A simple configuration consisting of elements of the form `org.codefx.lab.ExampleClass -> sun.misc.*` will allow to ignore certain dependencies.
The plugin can hence be included in any build and be configured such that it only breaks when new dependencies are introduced.

You can track the progress in [this issue](https://github.com/nipafx/JDeps-Maven-Plugin/issues/1).

## Existing Plugins

There are (at least) two Maven plugins which allow to use JDeps.

**[Maven JDeps by Philippe Marschall](https://github.com/marschall/jdeps-maven-plugin)**:
Runs JDeps against the compiled classes and either prints the output or creates a report from it.
Has no consequences for the build.

**[Apache Maven JDeps](http://maven.apache.org/plugins-archives/maven-jdeps-plugin-LATEST/maven-jdeps-plugin/)**:
In development.
Seems to be aimed at breaking the build when discovering internal dependencies but this does currently not work.

I wanted fast results and full control over where this is going for my projects.
I hence decided to reimplement parts of the functionality.

If this JDeps Maven plugin proves to be useful I will reach out and try to get some code included in either of those plugins (most likely the official one from Apache).
