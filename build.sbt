val pluginName = "sbt-distributables"

lazy val root = Project(pluginName, base = file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    majorVersion := 1,
    makePublicallyAvailableOnBintray := true
  )
  .settings(
    sbtPlugin := true,
    organization := "uk.gov.hmrc",
    scalaVersion := "2.10.7",
    //crossSbtVersions := Vector("0.13.18", "1.3.4"),
    resolvers += Resolver.url(
      "sbt-plugin-releases",
      url("https://dl.bintray.com/content/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
    resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    libraryDependencies ++= Seq(
      "org.apache.commons"    % "commons-compress"  % "1.19"
    )
  ).settings(addSbtPlugin("com.typesafe.sbt" %% "sbt-native-packager" % "1.5.1"))