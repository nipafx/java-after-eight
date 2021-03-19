---
title: "LibFX 0.3.0 Released"
tags: [libfx]
date: 2015-05-28
slug: libfx-0-3-0
description: "Release post for LibFX 0.3.0 including pointers to GitHub, feature descriptions, Maven coordinates and the Javadoc."
searchKeywords: "libfx 0.3.0"
featuredImage: libfx-library
repo: libfx
---

I just released [**LibFX 0.3.0**](https://github.com/nipafx/LibFX/releases/tag/v0.3.0)!

## Features

The killer feature are [transforming collections](https://github.com/nipafx/LibFX/wiki/TransformingCollections) about which I already [blogged a few days ago](java-transforming-collections).
I also created an easy way to stream nodes of all kinds of trees.
(I didn't get around to write up a wiki page for it yet but when I do, you will find it [here](https://github.com/nipafx/LibFX/wiki/TreeStreams).)

And don't forget about the other features:

**[ControlPropertyListener](https://github.com/nipafx/LibFX/wiki/ControlPropertyListener)**:
Creating listeners for the property map of JavaFX' controls.

**[ListenerHandle](https://github.com/nipafx/LibFX/wiki/ListenerHandle)**:
Encapsulating an observable and a listener for easier add/remove of the listener ([I blogged about it here](java-listenerhandles)).

**[Nestings](https://github.com/nipafx/LibFX/wiki/Nestings)**:
Using all the power of JavaFX' properties for nested object aggregations.

**[SerializableOptional](https://github.com/nipafx/LibFX/wiki/SerializableOptional)**:
Serializable wrapper for `Optional`.

**[WebViewHyperlinkListener](https://github.com/nipafx/LibFX/wiki/WebViewHyperlinkListener)**:
Add hyperlink listeners to JavaFX' `WebView`.

## Getting Started

The links above point to the [**LibFX** wiki on GitHub](https://github.com/nipafx/LibFX/wiki).
It has an article for each feature explaining the concept, giving some examples and pointing to the best resource in the code to get started.

Most key features also have self-contained demos, which can be found in [their own source folder](https://github.com/nipafx/LibFX/tree/master/src/demo/java/org/codefx/libfx).

Finally, there's extensive Javadoc under [libfx.codefx.org/apidocs/](http://libfx.codefx.org/apidocs/).

## Getting LibFX 0.3.0

You can download the jars from the [GitHub release site](https://github.com/nipafx/LibFX/releases/tag/v0.3.0) or use your favorite dependency management system:

<contentimage slug="LibFX-v0.3.0"></contentimage>

```xml
<dependency>
	<groupId>org.codefx.libfx</groupId>
	<artifactId>LibFX</artifactId>
	<version>0.3.0</version>
</dependency>
```

```groovy
compile 'org.codefx.libfx:LibFX:0.3.0'
```

**LibFX** is licensed under GLP 3.0 but other arrangements can be made - just ping me wherever you find me.
