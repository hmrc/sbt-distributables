# sbt-distributables-plugin-25

[![Join the chat at https://gitter.im/hmrc/sbt-distributables-plugin-25](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/hmrc/sbt-distributables-plugin-25?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Build Status](https://travis-ci.org/hmrc/sbt-distributables-plugin-25.svg)](https://travis-ci.org/hmrc/sbt-distributables-plugin-25) [ ![Download](https://api.bintray.com/packages/hmrc/sbt-plugin-releases/sbt-distributables-plugin-25/images/download.svg) ](https://bintray.com/hmrc/sbt-plugin-releases/sbt-distributables-plugin-25/_latestVersion) [![Stories in Ready](https://badge.waffle.io/hmrc/sbt-distributables-plugin-25.png?label=ready&title=Ready)](https://waffle.io/hmrc/sbt-distributables-plugin-25)

An SBT plugin to create then publish .tgz artifacts

Usage
-----

In your `project/plugins.sbt` file:
```
resolvers += Resolver.url("hmrc-sbt-plugin-releases",
  url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables-plugin-25" % "x.x.x")
```

where 'x.x.x' is the latest release as advertised above.

In your `project/FrontendBuild.scala` or `project/MicroserviceBuild.scala`

1. Add the line ```.enablePlugins(SbtDistributablesPlugin)``` to enable artifact creation
2. Add the line ```.settings(SbtDistributablePlugin.publisherSettings)``` to enable artifact publication

Optional settings:

1. Add the line ```.settings(extraFiles := Seq(new File("<PATH-TO-FILE>")))``` to add extra files to the archive (path must be relative)

What it does
------------

When enabled sbt-distributables-plugin-25 automatically creates and publishes a .tgz artifact. For an artifact `tester` with version `1.0.0` this would result in the following:

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
    system.properties
```

`system.properties` will specify a `java.runtime.version` derived from the `scalacOptions` setting in SBT. 
