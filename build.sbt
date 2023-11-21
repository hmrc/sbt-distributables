lazy val root = Project("sbt-distributables", file("."))
  .settings(
    majorVersion     := 2,
    isPublicArtefact := true,
    sbtPlugin        := true,
    scalaVersion     := "2.12.18",
    libraryDependencies ++= Seq(
      "org.apache.commons"    % "commons-compress"  % "1.19"
    )
  ).settings(addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.4"))
