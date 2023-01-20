lazy val root = Project("sbt-distributables", file("."))
  .settings(
    majorVersion     := 2,
    isPublicArtefact := true,
    sbtPlugin        := true,
    scalaVersion     := "2.12.14",
    crossSbtVersions := Vector("1.3.4"),
    libraryDependencies ++= Seq(
      "org.apache.commons"    % "commons-compress"  % "1.19"
    )
  ).settings(addSbtPlugin("com.typesafe.sbt" %% "sbt-native-packager" % "1.5.1"))
