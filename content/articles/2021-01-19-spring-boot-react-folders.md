---
title: "Sorting A React App Into Java's Folder Structure"
tags: [js, libraries, tools]
date: 2021-01-19
slug: java-react-folders
description: "How to use react-app-rewired to sort a React app into a Java folder structure with `package.json` at the root, and sources in `src/{main|test}/js`"
intro: "Have a Java project and want to sort a React app into its folder structure with `package.json` at the root and sources in `src/{main|test}/{java|js}`? Here's how to do that with react-app-rewired."
featuredImage: java-react-folders
inlineCodeLanguage: none
repo: react-calendar
---

I've recently started [a new side project](https://github.com/nipafx/calendar) and decided to go with Spring Boot and - after my positive experience with Gatsby for this very site - React.
Being new to this, I looked up several tutorials but they all had the same shortcoming:
The resulting folder structure was, well, unstructured.
Is it too much to ask to have production code in `src/main/{java|js}`, tests in `src/test/{java|js}`, and `package.json` and (almost) everything else in the root folder?
Where it belongs?

## Actual vs Expected

The tutorials all had one folder for the Java project, another for the React project, and then appeared to roll the dice for what to put where.
I've seen things you wouldn't believe.
An additional top-level folder that contained both projects side by side.
The `frontend` folder dumped unceremoniously into the Java project.
Half-baked efforts to apply Java's default structure by putting the React app into `src/main/js`, which then contained `package.json`, `node_modules`, tests, and other stuff that doesn't belong there.
Attack ships on fire off the shoulder of Orion.

<pullquote>I've seen things you wouldn't believe.</pullquote>

But all I wanted was this:

```
🗀 project_folder
├─ 🗀 node_modules
├─ 🗀 src
   ├─ 🗀 main
      ├─ 🗀 java
      └─ 🗀 js
   └─ 🗀 test
      ├─ 🗀 java
      └─ 🗀 js
├─ 🗀 target
├─ 🗎 package.json
├─ 🗎 pom.xml
└─ ...
```

Here's how to get there.

## Creating Java And React Apps

First, we need a Java app with the classic `src/main/java` folder structure.
You may already have one at hand - I created mine with [spring initializr](https://start.spring.io/).
Either way, the next step is to create the React App.
I used `npx` for that:

```
cd project_folder
npx create-react-app frontend
```

This leaves us with the following folders:

```
🗀 project_folder
├─ 🗀 frontend
   ├─ 🗀 node_modules
   ├─ 🗀 src
   ├─ 🗎 package.json
   └─ ...
├─ 🗀 src
├─ 🗀 target
├─ 🗎 pom.xml
└─ ...
```

Now it's time to shuffle things around.

## Splitting The React App With _react-app-rewired_

Sorting the React app into the Java structure requires three steps:

1. moving things around
2. telling React where things are
3. telling Jest where things are

### Moving Things Around

To create the desired folder structure, we need to move everything out of `frontend`:

* `src/*` (sources) ~> move to `src/main/js`
* `src/*` (tests) ~> move to `src/test/js`
* `src/setupTests.js` ~> move to `src/test/js`
* `public` ~> move to `src/main/static` (not sure whether `static` is a good name - you do you)
* `node_modules` ~> move to root folder
* `.gitignore` ~> append to existing `.gitignore` in the root folder
* `package.json` and `package-lock.json` \
  ~> move to root folder
* `README` ~> read & delete

<pullquote>The Java project's root folder is now the React app's root folder</pullquote>

That should be all files from `frontend`, so you can delete it.

As indicated by the position of `package.json`, this makes the Java project's root folder the React app's root folder.

### Telling React Where Things Are

So far, so good, but now we need to tell React where to find everything.
It took me a bit, but I eventually found _[react-app-rewired](https://github.com/timarney/react-app-rewired)_, which says:

> All the benefits of create-react-app without the limitations of "no config". You can add plugins, loaders whatever you need.

Sounds great!
Install ahead:

```
npm install react-app-rewired --save-dev
```

First we need to rewire the npm scripts, so _rewired_ is actually used:
In `package.json` under `scripts`, replace each mention of `react-scripts` (except for `"eject"`) with `react-app-rewired`:

```js
"scripts": {
	"start": "react-app-rewired start",
	"build": "react-app-rewired build",
	"test": "react-app-rewired test",
	"eject": "react-scripts eject"
},
```

Next, we need to create a file `config-overrides.js` in the app's (new) root folder.
[The _rewired_ documentation](https://github.com/timarney/react-app-rewired#how-to-rewire-your-create-react-app-project) is a bit sparse on how exactly to use it (or maybe I just didn't get it), but after a bit of trial and error, I ended with this file:

```js
// file: config-overrides.js

module.exports = {
	paths: function (paths, env) {

		// use this to check original paths:
		// console.log(paths)

		root = paths.appPath
		paths.appBuild = `${root}/target/classes/public`
		paths.appPublic = `${root}/src/main/static`
		paths.appHtml = `${root}/src/main/static/index.html`
		paths.appIndexJs = `${root}/src/main/js/index.js`
		// paths.appPackageJson = `${root}/package.json`
		paths.appSrc = `${root}/src/main/js`
		// paths.appTsConfig = `${root}/tsconfig.json`
		// paths.appJsConfig = `${root}/jsconfig.json`
		// paths.yarnLockFile = `${root}/yarn.lock`
		paths.testsSetup = `${root}/src/test/js/setupTests.js`
		// paths.proxySetup = `${root}/src/main/js/setupProxy.js`
		// paths.appNodeModules = `${root}/node_modules`
		// paths.swSrc = `${root}/src/main/js/service-worker.js`
		// paths.publicUrlOrPath = '/'
		// paths.ownPath = `${root}/node_modules/react-scripts`
		// paths.ownNodeModules = `${root}/node_modules/react-scripts/node_modules`
		// paths.appTypeDeclarations = `${root}/src/react-app-env.d.ts`
		// paths.ownTypeDeclarations = `${root}/node_modules/react-scripts/lib/react-app.d.ts`
		return paths;
	},

	// more to come below
}
```

To arrive there I...

* logged the `paths` instance given to my function (see commented code)
* copied the output into the file, so I can quickly see all the options
* changed paths of everything I need to their new value
  (these need to be absolute, so I use `root` to make it more readable)
* commented out everything else to have it visible in case anything else breaks

And there we go, `npm start` and `npm run build` work like a charm.
The latter places the frontend code into `target/classes/public`, which is perfect for Maven to pick it up and roll it into Spring Boot's fat JAR.

What about `npm run test`?

### Telling Jest Where Things Are

By default, React apps use [Jest](https://jestjs.io/) and it also needs to know the right paths.
Here's how to configure that in `config-overrides.js`:

```js
// file: config-overrides.js

module.exports = {
	paths: function (paths, env) { /* as above */ },

	jest: function(config) {

		// use this to check original config:
		// console.log(config)

		config.rootDir = '/home/nipa/code/calendar'
		config.roots = [
			'<rootDir>/src/main/js',
			'<rootDir>/src/test/js'
		]
		config.setupFilesAfterEnv = [
			'<rootDir>/src/test/js/setupTests.js'
		]
		// config.modulePaths = [ ]

		return config;
	}
}
```

I used the same approach as above, but stripped all the config options that are unrelated to paths.

This fixes `npm run test` as well.


## Reflection

So there we go:

* take a default Java app and a default React app
* sort React app folders into Java folders, particularly:
	* source files into `src/{main|test}/js`
	* `package-(lock).json` and `node_modules` into root
* rewire React to new structure:
	* install _react-app-rewired_
	* create `config-overrides.js` in the project root and set paths there

And that's it!
(You can see it all in [this diff](https://github.com/nipafx/calendar/commit/6bbde36ffeda1599ab0e1e3e16aeb3d025b67cf1).)
Here's the result:

```
🗀 project_folder
├─ 🗀 node_modules
├─ 🗀 src
   ├─ 🗀 main
      ├─ 🗀 java
      └─ 🗀 js
   └─ 🗀 test
      ├─ 🗀 java
      └─ 🗀 js
├─ 🗀 target
├─ 🗎 config-overrides.js
├─ 🗎 package.json
├─ 🗎 pom.xml
└─ ...
```

Next up for that little project is to get Maven to build the frontend with npm and then create a self-contained runtime image with jlink.
Who knows, maybe I'll write about that, too. 😉
