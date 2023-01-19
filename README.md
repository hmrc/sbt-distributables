# sbt-distributables

![](https://img.shields.io/github/v/release/hmrc/sbt-distributables)
[![Apache-2.0 license](http://img.shields.io/badge/license-Apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

An SBT plugin to create then publish .tgz artifacts

Usage
-----

### Sbt 1.x

The library has supported sbt 1.x since version 2.0.0 and has dropped support for sbt 0.13 since 2.2.0

| Sbt version | Plugin version |
| ----------- | -------------- |
| `>= 1.x`    | `>= 2.0.0`     |
| `0.13.x`    | `<= 2.1.0`     |



In your `project/plugins.sbt` file:

```scala
resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "x.x.x")
```

where 'x.x.x' is the latest release as advertised above.

In your `build.sbt`

1. Add the line ```.enablePlugins(SbtDistributablesPlugin)``` to enable artifact creation

Optional settings:

1. Add the line ```.settings(extraFiles := Seq(new File("<PATH-TO-FILE>")))``` to add extra files to the archive (path must be relative)

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
    system.properties
```

`system.properties` will specify a `java.runtime.version` derived from the `scalacOptions` setting in SBT.
