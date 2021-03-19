---
title: "Running Android Emulator With HAXM On Thinkpad T440p"
tags: [tools]
date: 2015-01-26
slug: android-emulator-haxm-thinkpad-t440p
description: "Quick guide to how to use the Android emulator with HAXM (based on VT-x) on a Thinkpad T440p."
searchKeywords: "HAXM"
featuredImage: haxm
---

Over the weekend, I started to work on [my first Android app](https://github.com/CodeFX-org/privacy-guide).
I had a little trouble getting the [Android emulator](http://developer.android.com/tools/devices/index.html) to run with *HAXM*, Intel's *Hardware Accelerated Execution Manager*, which builds on top of the virtualization hardware *VT-x*.
Maybe others with the same problems find their way here...

These are the steps that worked **for me** - your mileage may vary.
Read them carefully; I might have let my frustration slip into the steps at some point or other.

## Hardware Support

First check whether your processor actually supports virtualization.

1. [go to Intel](http://ark.intel.com/)
2. find your processor
3. check whether *Intel® Virtualization Technology (VT-x)* is listed under *Advanced Technologies*
4. also read the footnote if there is one

Side note: [*VT-x* is included in *VT-d*](https://software.intel.com/en-us/articles/intel-virtualization-technology-for-directed-io-vt-d-enhancing-intel-platforms-for-efficient-virtualization-of-io-devices)

## Download _HAXM_ installer

Now you can download the installer for the emulator accelerator.

1. open the Android SDK Manager
	-   in Android Studio it's under *Tools* \~&gt; *Android* \~&gt; *SDK Manager*
	-   in Eclipse it's under *Window* \~&gt; *Android SDK Manager* (look [here](http://stackoverflow.com/a/13885869/2525313) if it's missing)
2. check *Intel x86 Emulator Accelerator (HAXM installer)* under *Extras* and install packages

## Install _HAXM_

Next step is to actually install HAXM.

1. above the package list the SDK manager shows the path to the SDK
2. go there and then continue to *./extras/intel/Hardware\_Accelerated\_Execution\_Manager*
3. run the *intelhaxm-android* installer

## Fail In Different Ways (optional)

In case the installation doesn't work because the virtualization features are not turned on, there are several ways to waste some time...

1. you can download intel's Processor Identification Utility (for [Windows](https://downloadcenter.intel.com/Detail_Desc.aspx?DwnldID=7838) and [Linux](http://www.intel.com/support/processors/tools/piu/sb/CS-033142.htm)) and see it claim that *Intel (R) Virtualization Technology* is *Yes* (not my wording)
2. you can install [CPU-Z](http://www.cpuid.com/softwares/cpu-z.html) or [i-Nex](http://www.omgubuntu.co.uk/2014/02/nex-cpu-z-hardware-stat-tool-linux) and watch it report *VT-X* (under *instructions*) to be turned on

## Fix It

After that small detour you might realize that the tools are lying to you...

1. enter your BIOS (on my T440p: *Enter* on first boot screen, then *F1*)
2. go to *Config* \~&gt; *CPU* and be frustrated that it's not there...
3. go to *Security* \~&gt; *Virtualization* and turn everything on
4. reboot and install successfully

## Some more links...

-   [Speeding Up the Android Emulator on Intel Architecture - Intel](https://software.intel.com/en-us/android/articles/speeding-up-the-android-emulator-on-intel-architecture)
-   [HAXM Install Will Not Detect Enabled VT-x - thread in Intel forum](https://software.intel.com/en-us/forums/topic/328242)
-   [active *Hyper-V* might lock access to *VT-x*](https://forums.lenovo.com/t5/T400-T500-and-newer-T-series/Intel-VT-x-on-T440-64-bit-Virtual-Machine-Support/m-p/1639600/highlight/true#M100150 "Intel VT-x on T440?
(64-bit Virtual Machine Support) - Lenovo Forum")

Worked?
Didn't work?
Tweet, leave a comment, shoot me a mail...
