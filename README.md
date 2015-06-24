# sbt-slug

[![Join the chat at https://gitter.im/hmrc/sbt-slug](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/hmrc/sbt-slug?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Build Status](https://travis-ci.org/hmrc/sbt-slug.svg)](https://travis-ci.org/hmrc/sbt-slug) [ ![Download](https://api.bintray.com/packages/hmrc/releases/sbt-slug/images/download.svg) ](https://bintray.com/hmrc/releases/sbt-slug/_latestVersion) [![Stories in Ready](https://badge.waffle.io/hmrc/sbt-slug.png?label=ready&title=Ready)](https://waffle.io/hmrc/sbt-slug)

An SBT plugin to create and publish slug .tgz artifacts, ready for a build pack to be applied at a later date

Usage
-----

In your project/plugins.sbt file:
```
resolvers += Resolver.url("hmrc-sbt-plugin-releases",
  url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc" % "sbt-slug" % "x.x.x")
```

where 'x.x.x' is the latest release as advertised above.

Add the line ```.enablePlugins(SbtSlugPlugin)``` to your project to enable the plugin.

What it does
------------

When enabled sbt-slug automatically creates and publishes a slug tgz artifact with the following structure:

'name-version.tgz'
- .
-   name-version
-   Procfile
-   start-docker.sh
