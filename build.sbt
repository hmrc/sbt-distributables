import uk.gov.hmrc.DefaultBuildSettings.targetJvm

val pluginName = "sbt-distributables"

lazy val root = Project(pluginName, base = file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    majorVersion := 1,
    makePublicallyAvailableOnBintray := true
  )
  .settings(
    sbtPlugin := true,
    targetJvm := "jvm-1.7",
    organization := "uk.gov.hmrc",
    scalaVersion := "2.10.6",
    resolvers += Resolver.url(
      "sbt-plugin-releases",
      url("https://dl.bintray.com/content/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
    resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-compress" % "1.9",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.pegdown" % "pegdown" % "1.4.2" % "test"
    )
  ).settings(addSbtPlugin("com.typesafe.sbt" %% "sbt-native-packager" % "1.1.0"))