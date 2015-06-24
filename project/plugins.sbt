import _root_.sbt.Keys._
import _root_.sbt.Resolver
import _root_.sbt._

resolvers += Resolver.url("hmrc-sbt-plugin-releases",
  url("https://dl.bintray.com/hmrc/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "0.8.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "0.8.0")