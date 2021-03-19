---
title: "Building Atom On Gentoo"
tags: [tools]
date: 2016-03-07
slug: atom-on-gentoo
description: "See how to build Atom on Gentoo straight from the sources."
intro: "While not particularly hard, building Atom on Gentoo is a little elusive and poorly documented. Look here for enlightenment!"
searchKeywords: "atom on gentoo"
featuredImage: atom-on-gentoo
---

I've been very happy with my [switch to Gentoo](hello-2016#linux).
One of the cool things is that I can use the package manager for most development tools as [Portage](https://wiki.gentoo.org/wiki/Portage) contains up-to-date versions of most of them.
(Case in point, I just updated Node.js to a version that was released just four days ago.) All the more shocked was I when I found out that [Atom](https://atom.io/) isn't one of them and that information on how to install Atom on Gentoo is elusive.

Maybe because in the end the process isn't that complicated.
Basically I just had to build from source, mostly following the [documentation on how to do that](https://github.com/atom/atom/blob/master/docs/build-instructions/linux.md#linux).
I'll still repeat it here to have it readily available when I have to update.

## Building Atom

These steps worked for [Atom 1.5.3](https://github.com/atom/atom/releases/tag/v1.5.3).

### Requirements

The first three requirements listed in the [documentation](https://github.com/atom/atom/blob/master/docs/build-instructions/linux.md#linux) are a 32/64 bit OS, the C++ tool chain and Git.
Every Gentoo install covers that.

Then come [Node.js](http://nodejs.org/download/) and its package manager [npm](https://www.npmjs.com/), which comes bundled with it.
I [unmasked](https://wiki.gentoo.org/wiki/Knowledge_Base:Unmasking_a_package) all 5.x releases of Node (with `=net-libs/nodejs-5*`) but you should get a sufficiently new version even if you don't do that - make sure to check, though.

Next on the list are the development headers for the [GNOME keyring](https://wiki.gnome.org/Projects/GnomeKeyring), which is the only non-obvious (and generally somewhat peculiar) step.
Emerging `gnome-base/libgnome-keyring-3.12.0` fixed this for me.

### Build & Install

These are just the [install instructions](https://github.com/atom/atom/blob/master/docs/build-instructions/linux.md#instructions):

```shell
git clone https://github.com/atom/atom
cd atom
git checkout v1.5.3
script/build
sudo script/grunt install
```

Have fun!

<contentimage slug="atom-on-gentoo-larry" options="narrow"></contentimage>

## Alternatives

For an install that is better integrated with the OS you might want to use a real [ebuild](https://devmanual.gentoo.org/quickstart/).

There are a couple of [Portage overlays containing Atom](https://gpo.zugaina.org/app-editors/atom), most noteworthy the ones belonging to the Gentoo-based distributions [Sabayon](https://en.wikipedia.org/wiki/Sabayon_Linux) and [Funtoo](https://en.wikipedia.org/wiki/Funtoo_Linux) but also others.
I opted against this solution because using the overlay of another distribution looks like a troublesome move to a novice and using "some guy's overlay" doesn't sit well with my general paranoia.

But you can also do it yourself - or so [Till claims](atom-on-gentoo)<!-- comment-2557921347 --> in the comments.
If I find some time I'll try it and update this post.
