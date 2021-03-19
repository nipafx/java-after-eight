---
title: "Maven on Java 9 and later - Six Things You Need To Know"
tags: [java-9, tools]
date: 2017-12-18
slug: maven-on-java-9
description: "How to use the compiler executable, toolchains, and mavenrc to run Maven on Java 9 and how to use mvn/jvm.config and profiles to configure your build."
intro: "Want to build with Maven on Java 9? Here's which versions to pick, how to use the compiler executable, toolchains, and mavenrc to run Maven on Java 9, and how to use mvn/jvm.config and profiles to configure your build for Java 8 and 9."
searchKeywords: "Maven Java 9"
featuredImage: maven-java-9
repo: mvn-java-9
---

You're ready to build your Maven-based project with [Java 9](tag:java-9) or later?
Then you've come to the right place!
Here are six things that you need to know to make the two play nice together.
I'll show you which versions to pick, how to run Maven on a new Java version even though it is not your default JDK, how to apply command line options to your build process, and how to keep your build running on Java 8 *and* and the newer versions.

This post is glued together from different parts of my weekly newsletter, where I covered these topics when I first encountered them.
Check out [past newsletters on Medium](http://medium.com/codefx-weekly) and [subscribe to get it while it's hot](news).
This post was originally written for Java 9, but it applies just the same to building with any younger version, Java 11 for example.

## Maven Version

First things first, you have to pick the right versions.
Maven adopted Java 9 without much ado, so you might not have to make many updates.
Here are the minimum requirements:

-   **Maven itself**: 3.5.0
-   **Maven Compiler Plugin**: 3.7.0 / 3.8.0 for Java 11

With that settled, I'll leave out all `<version>` tags for the configurations I show.

## Running Maven On Java 9

Next you'll want to configure Maven so that it actually uses Java 9.
(You can skip this step if JDK 9 is your default and `mvn -v` shows that Maven runs on it.) I found three different ways to build projects on Java 9:

-   [compiler executable](#executable)
-   [toolchain](#toolchain)
-   [mavenrc](#themavenrcfile)

Each approach uses Java 9 a little more than the last and some are more intrusive while others are more brittle - have a look at the individual solutions for a small discussion of their respective pros and cons.
If you want to try this out yourself, have a look at [my Maven-on-Java-9 demo project](https://github.com/nipafx/mvn-java-9).

### Executable

In the `maven-compiler-plugin`, the executable that is used for compilation [can be explicitly named](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html#executable):

```xml
<build>
	<plugins>
		<!-- target Java 9 -->
		<plugin>
			<artifactId>maven-compiler-plugin</artifactId>
			<configuration>
				<!-- fork compilation and use the
					     specified executable -->
				<fork>true</fork>
				<executable>javac9</executable>
			</configuration>
		</plugin>
	</plugins>
</build>
```

For `executable` to have an effect, [the `fork` option](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html#fork) needs to be set to `true`, which tells Maven to launch the compiler in a separate process.

#### Improvements

In the example above I simply use `javac9`.
That works for me because I symlinked `/bin/javac9` (as well as `java9`, `jar9`, `jdeps9`) to `/opt/jdk-9/bin/javac` and `/opt/jdk-9` to whatever JDK 9 version I am currently using.
Your and your team's setup might differ of course and there are two ways to improve this.

First, if you are just experimenting, you could specify the property on the command line instead (but this *only* works if you do not also set it in `<configuration>`):

```shell
mvn
	-Dmaven.compiler.fork
	-Dmaven.compiler.executable=javac9
#   whatever phase floats your boat
#   (maybe you prefer 'install' instead)
	verify
```

Second, to configure the compiler for all machines, you can use a self-defined user property and ask developers to define it in their `settings.xml`.
See [the Maven docs](https://maven.apache.org/plugins/maven-compiler-plugin/examples/compile-using-different-jdk.html) for more on that.

#### Pros and Cons

To compile with Java 9, for example to quickly check whether your project compiles without errors, the command line flag is a low ceremony approach as it requires no other changes (assuming you do not already specify the executable in `<configration>`).

If you not only want to compile with Java 9 but also use Java 9 features, you also have to update source and target to `9`.
But if you're going down that more permanent road, why not use the toolchain instead?

### Toolchain

With [Maven Toolchains](https://maven.apache.org/guides/mini/guide-using-toolchains.html) it is easy to use Java 9 in the project's `pom.xml` and let every developer specify their path in their local settings.
Every plugin that is aware of this feature will ask the toolchain whenever it needs a Java executable and thus use whatever the developer configured.

This is what the POM looks like:

```xml
<plugin>
	<artifactId>maven-toolchains-plugin</artifactId>
	<configuration>
		<toolchains>
			<jdk>
				<version>1.9</version>
				<vendor>oracle</vendor>
			</jdk>
		</toolchains>
	</configuration>
	<executions>
		<execution>
			<goals>
				<goal>toolchain</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

Next, put this into `~/.m2/toolchains.xml`:

```xml
<toolchains>
	<!-- JDK toolchains -->
	<toolchain>
		<type>jdk</type>
		<provides>
			<version>1.9</version>
			<vendor>oracle</vendor>
		</provides>
		<configuration>
			<jdkHome>/path/to/your/jdk-9</jdkHome>
		</configuration>
	</toolchain>
	<toolchain>
		<type>jdk</type>
		<provides>
			<version>1.8</version>
			<vendor>oracle</vendor>
		</provides>
		<configuration>
			<jdkHome>/path/to/your/jdk-8</jdkHome>
		</configuration>
	</toolchain>
</toolchains>
```

#### Pros and Cons

This is arguably the cleanest approach because it is project-specific and makes obvious where the Java 9 compiler is supposed to come from.
It requires mucking with the POM and repeating a configuration on each developer machine, though, which in large legacy projects might not be your first choice.

More importantly, it does not change the Java version that runs the Maven process.
Many plugins are not forked into their own process and will thus run in the same JVM as Maven.
If that's Java 8, you might run into problems.
(I worked on a project that ran the [RMI compiler](https://docs.oracle.com/javase/7/docs/technotes/tools/windows/rmic.html) during the build, but if that was Java 8 it could not read the Java 9 bytecode created by the forked Java 9 compiler.)

If your entire build has to run on Java 9, but you're not ready to make that your default JVM, have a look at `mavenrc`.

### The mavenrc File

Maven can apparently be configured with the [mostly undocumented](https://issues.apache.org/jira/browse/MNGSITE-246) files `~/.mavenrc` (for current user) and `/etc/mavenrc` (for all users).
In there, environment variables and command line options for the Java command can be configured.

With this, it is easy to set `JAVA_HOME` just for the Maven command, which will lead to it running with the specified version.
Here's the content of that file:

```shell
JAVA_HOME="/path/to/your/jdk-9"
```

Use `mvn -v` to verify that Maven runs on Java 9.

#### Pros and Cons

To compile with Java 9, for example to check whether your project builds without errors, this is a low ceremony approach as it requires no other changes.
It is also fairly easy to switch between building on Java 8 and 9 (have a look [at this tip](#switchingmavenjvmbetweenjava8andjava9) to make it even easier).

If you not only want to compile with Java 9 but also use Java 9 features, you still have to update `<source>` and `<target>` to 9 (or specify `<release>9</release>`).
Note that this puts the POM into an awkward state where it is supposed to use JDK 9 but does not reference where it might come from (unlike the toolchain approach).

Another disadvantage is that the setup must be repeated for every developer on the project.

## Applying Java 9 Flags To Maven Process

If you're running your entire build process on Java 9, either by making it the default JDK or by using the `mavenrc` approach discussed above, you might run into [compatibility problems](java-9-migration-guide).
Some Maven plugins use [internal APIs](java-9-migration-guide#illegal-access-to-internal-apis) or [depend on Java EE modules](java-9-migration-guide#dependencies-on-java-ee-modules), so the JVM running them needs to be [launched with some command line flags](five-command-line-options-hack-java-module-system) like `--add-opens` or `--add-modules`.

That's no problem if they can be forked into their own process and configured on the POM, but that is rarely implemented.
In those cases they run in the Maven process, meaning Maven must be launched with the appropriate flags.

This is possible by creating [a file `.mvn/jvm.config` in the project's folder](https://maven.apache.org/docs/3.3.1/release-notes.html#JVM_and_Command_Line_Options) and putting the options in there.
Here's a simple example:

```shell
--add-modules java.xml.bind
--add-opens java.base/java.lang=ALL-UNNAMED
--illegal-access=deny
```

Unfortunately, the project can then no longer be built with Java 8 because when Maven applies the flags it finds in that file when launching the JVM, version 8 barfs due to unknown command line options.
Read on to find out how to fix that.

## Switching Maven's JVM Between Java 8 And 9

If you indeed defined `JAVA_HOME` in `mavenrc` or needed to create a `.mvn/jvm.config` file, you have to edit these files every time you switch between building on Java 8 and Java 9.
The former, so you build with the correct Java version and the latter because otherwise Java 8 complains about the unknown command line options (I tried putting `-XX:+IgnoreUnrecognizedVMOptions` in there but had no success).That gets annoying quite quickly, so I wrote a little bash script.
It does two things:

-   comments or uncomments `JAVA_HOME` in `mavenrc`
-   renames files from `.mvn/jvm.config` to `.mvn/jvm9.config` and back to move them in or out the way, depending on which version you build on

Here it is:

```shell
#!/bin/bash

java_version=$1

if [ $java_version -eq 8 ]; then
	sed -i -e 's/^JAVA_HOME/#JAVA_HOME/' ~/.mavenrc
	if [ -f $PWD/.mvn/jvm.config ]; then
		mv -f $PWD/.mvn/jvm.config $PWD/.mvn/jvm9.config
	fi
elif [ $java_version -eq 9 ]; then
	sed -i -e 's/#*JAVA_HOME/JAVA_HOME/' ~/.mavenrc
	if [ -f $PWD/.mvn/jvm9.config ]; then
		mv -f $PWD/.mvn/jvm9.config $PWD/.mvn/jvm.config
	fi
else
	echo "Unknown version $java_version - doing nothing"
fi
```

Depending which versions I want to run on, it creates the following configuration:

-   Java 8:
	-   the `JAVA_HOME` line in `.mavenrc` is commented out, starting the process with "system Java"
	-   the project's `.mvn` folder contains no file called `jvm.config` (if it existed it was renamed to `jvm9.config`)
-   Java 9
	-   the `JAVA_HOME` line in `.mavenrc` is not commented out, starting the process with the defined Java, which is 9
	-   the project's `.mvn` folder may contain a file called `jvm.config` (if it existed as `jvm9.config`)

Only caveat: If you have several projects and switch one to Java 8 (for example), another one might still be partially stuck on Java 9 because the `.mvn/jvm.config` is not renamed across subprojects.
If that bothers you, replace the `mv` command with `find ... -exec mv ...` to rename `jvm.config`/`jvm9.config` files in all subfolders.

This script is not only great for developers, it also allows you to easily build the same branch on Java 8 and on Java 9, which I highly recommend for all projects that want to ensure Java 9 compatibility without making it its baseline.
For that you can check in `jvm9.config` files and then simply let the Java 9 build execute the script before calling Maven.
Et voilà, Java 9 build from master/trunk.

Beyond the Maven process you will most likely also need to configure the build by providing Java 8/9 specific settings.
That's next.

## Configuring The Build For Java 8 And Java 9

Most Java-version-specific changes will have to be applied in the POM.
The most obvious example are command line flags that make internal APIs and Java EE modules available during compilation or for testing, but many other details might change between Java versions as well, dependencies for example.

For those cases you should use [Maven profiles](http://maven.apache.org/guides/introduction/introduction-to-profiles.html).
I recommend to keep the Java 8 configuration in the POM's non-profile part and at first only create a Java 9 profile that is automatically activated if the build runs on Java 9:

```xml
<profiles>
	<profile>
		<id>java-9</id>
		<activation>
			<jdk>9</jdk>
		</activation>
		<!-- add Java 9 specific configuration -->
	</profile>
</profiles>
```

Note that while a profile from a parent POM is *activated* on module builds and thus applies its settings, it is not actually *inherited*.
That means you have to repeat the `<activation>` block all over your different POMs when you create specific configurations in your modules (Maven modules that is, not [the Java kind](java-module-system-tutorial)).

### Possible Default Settings

Of you're creating a profile, particularly in a parent POM, you should think about whether there is any default behavior that you want to enforce on Java 9.
Two things that come to mind:

-   if many modules depend on the same set of Java EE modules, consider adding them in the parent POM
-   if you like [strong encapsulation](java-module-system-tutorial#exports-and-accessibility) to know which of your modules and dependencies are naughty, [add the flag `--illegal-access=deny`](five-command-line-options-hack-java-module-system#relying-on-weak-encapsulation) to Surefire and Failsafe.

If you do both (for example with the JAXB API), your profile might look as follows:

```xml
<profiles>
	<profile>
		<id>java-9</id>
		<activation>
			<jdk>9</jdk>
		</activation>
		<build>
			<plugins>
				<plugin>
					<artifactId>maven-compiler-plugin</artifactId>
					<compilerArgs>
					    <arg>--add-modules=java.xml.bind</arg>
					</compilerArgs>
					<!-- without forking compilation happens in the
					    same process, so no arguments are applied -->
					<fork>true</fork>
				</plugin>
				<plugin>
					<artifactId>maven-surefire-plugin</artifactId>
					<configuration>
					    <argLine>
					        --add-modules java.xml.bind
					        --illegal-access=deny
					    </argLine>
					</configuration>
				</plugin>
				<plugin>
					<artifactId>maven-failsafe-plugin</artifactId>
					<configuration>
					    <argLine>
					        --add-modules java.xml.bind
					        --illegal-access=deny
					    </argLine>
					</configuration>
				</plugin>
			</plugins>
		</build>
	</profile>
</profiles>
```

Unfortunately, this ensues some incidental complexity because you have to know in which order Maven applies these configurations, particularly because `<argLine>`s aren't merged but overridden.
That has some consequences:

-   using `<pluginManagement>` in the Java 9 profile is fragile because it gets overridden by the more concrete `<plugins>` configuration in your POM's non-profile (i.e.
non-Java-9) part
-   if your regular build configures command line arguments for these plugins, you have to repeat them in the Java 9 profile
-   any module POM that further configures these plugins' command line arguments in its Java 9 profile needs to repeat the arguments from the parent POM's Java 9 profile

If you're not sure whether a particular `<argLine>` is applied just fill it with nonsense like `--foo` - if you don't get an error during the build, that settings is overridden.

### Dependencies

In rare cases you might have to use different dependencies for Java 8 and 9.
I'd work hard to avoid that because it makes coding and testing that much more complex, but if you can't, then here's the way to go.
Create a Java 8 profile akin to the one above:

```xml
<profiles>
	<profile>
		<id>java-8</id>
		<activation>
			<jdk>1.8</jdk>
		</activation>
		<dependencies>
			<!-- add Java 8 dependencies -->
		</dependencies>
	</profile>
</profiles>
```

Then add dependencies that you only need on Java 8 in that block and those for Java 9 in a `<dependency>` block in the Java 9 profile.

## Arguments For The Maven Compiler Plugin

If you're trying to use some of [the new command line options](five-command-line-options-hack-java-module-system) on the compiler, be aware that the following doesn't work:

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-compiler-plugin</artifactId>
	<configuration>
		<compilerArgs>
			<arg>--add-modules java.xml.bind</arg>
		</compilerArgs>
		<fork>true</fork>
	</configuration>
</plugin>
```

With this configuration I got errors like the following:

```shell
[INFO] -------------------------------------------------------------
[ERROR] COMPILATION ERROR :
[INFO] -------------------------------------------------------------
[ERROR] javac: invalid flag: --add-modules java.xml.bind
Usage: javac <options> <source files>
use --help for a list of possible options
```

Apparently [it's common knowledge](https://twitter.com/rfscholte/status/849969556332437504) that `<arg>` will put arguments that contain a space into quotes before passing them, which lets the compiler interpret it as a single string.
Obvious, right?
🤔 Fortunately, the new command line arguments allow using an equal sign `=` instead of a space, so do the following instead:

```xml
<compilerArgs>
	<arg>--add-modules=java.xml.bind</arg>
</compilerArgs>
```

Another disappointment was that Maven doesn't support argument files for the compiler.
I'm not gonna go into details here (read [this weekly on argument files](https://medium.com/codefx-weekly/java-argument-files-affiliations-and-lego-f5348e361f30#1983) if you're interested), but I wanted to let you know, so you don't waste your time with that.

## Reflection

In summary:

-   make sure to fulfill the version requirements
-   when building your Maven project on Java 9, you can:
	-   just compile with Java 9 by setting the compiler's executable
	-   use the toolchain to execute some steps of your build on Java 9 (particularly compilation and testing)
	-   use `mavenrc` to run the entire build on Java 9
-   use `.mvn/jvm.config` to apply command line flags to your build
-   use a script that renames files to switch between Java 8 and 9
-   use profiles to configure your build for Java 8 and 9, so both work from the same POMs
-   use `=` instead of spaces when passing command line arguments to the compiler

If there's anything I got wrong or you think should be added to the list, let me know in the comments or [on Twitter](https://twitter.com/nipafx).

And that's that!
In the future I might write a post about using Maven to build modules, but I want to get more real-life experience with that before writing about it.
Stay tuned.
Last but not least, if you liked the post, share it with your followers.
