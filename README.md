# sbt-distributables

[![Join the chat at https://gitter.im/hmrc/sbt-distributables](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/hmrc/sbt-distributables?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Build Status](https://travis-ci.org/hmrc/sbt-distributables.svg)](https://travis-ci.org/hmrc/sbt-distributables) [ ![Download](https://api.bintray.com/packages/hmrc/sbt-plugin-releases/sbt-distributables/images/download.svg) ](https://bintray.com/hmrc/sbt-plugin-releases/sbt-distributables/_latestVersion) [![Stories in Ready](https://badge.waffle.io/hmrc/sbt-distributables.png?label=ready&title=Ready)](https://waffle.io/hmrc/sbt-distributables)

An SBT plugin to create and publish distributable .tgz artifacts

Usage
-----

In your `project/plugins.sbt` file:
```
resolvers += Resolver.url("hmrc-sbt-plugin-releases",
  url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "x.x.x")
```

where 'x.x.x' is the latest release as advertised above.

In your `project/FrontendBuild.scala` or `project/MicroserviceBuild.scala`

1. Add the line ```.enablePlugins(SbtDistributablesPlugin)``` to enable artifact creation
2. Add the line ```.settings(SbtDistributablePlugin.publisherSettings)``` to enable artifact publication

What it does
------------

When enabled sbt-distributables automatically creates and publishes a .tgz artifact. For an artifact `tester` with version `1.0.0` this would result in the following:

```
tester-1.0.0.tgz
  .
    tester-1.0.0
      bin
      conf
      lib
      share
      README.md
    Procfile
    start-docker.sh
```
