---
title: "LibFX 0.2.0 Released"
tags: [javafx, libfx]
date: 2014-11-23
slug: libfx-0-2-0
description: "Release post for LibFX 0.2.0 including and pointers to GitHub, Feature descriptions, Maven coordinates and the Javadoc."
searchKeywords: "libfx 0.2.0"
featuredImage: libfx-library
repo: libfx
---

Yesterdays I released [**LibFX 0.2.0**](https://github.com/nipafx/LibFX/releases/tag/v0.2.0)!
It has nice new features I've been needing for other projects:

-   [`ControlPropertyListener`](https://github.com/nipafx/LibFX/wiki/ControlPropertyListener): creating listeners for the property map of JavaFX' controls
-   [`ListenerHandle`](https://github.com/nipafx/LibFX/wiki/ListenerHandle): encapsulating an observable and a listener for easier add/remove of the listener
-   [`SerializableOptional`](https://github.com/nipafx/LibFX/wiki/SerializableOptional): serializable wrapper for `Optional`
-   [`WebViewHyperlinkListener`](https://github.com/nipafx/LibFX/wiki/WebViewHyperlinkListener): add hyperlink listeners to JavaFX' `WebView`

And don't forget about [Nestings](https://github.com/nipafx/LibFX/wiki/Nestings): using all the power of JavaFX' properties for nested object aggregations.

## Getting Started

The links above point to the [**LibFX** wiki on GitHub](https://github.com/nipafx/LibFX/wiki).
It has an article for each feature explaining the concept, giving some examples and pointing to the best resource in the code to get started.

Most key features also have self-contained demos, which can be found in [their own source folder](https://github.com/nipafx/LibFX/tree/master/src/demo/java/org/codefx/libfx).

Finally, there's extensive Javadoc under [libfx.codefx.org/javadoc/](http://libfx.codefx.org/javadoc/).

## Getting LibFX 0.2.0

You can get **LibFX 0.2.0** here:

<contentimage slug="LibFX-v0.2.0" options="sidebar"></contentimage>

```xml
<dependency>
	<groupId>org.codefx.libfx</groupId>
	<artifactId>LibFX</artifactId>
	<version>0.2.0</version>
</dependency>
```

```java
compile 'org.codefx.libfx:LibFX:0.2.0'
```

It's licensed under GLP 3.0 but other arrangements can be made - just [shoot me an email](mailto:nicolai@nipafx.dev).
