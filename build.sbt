lazy val root = Project("sbt-distributables", file("."))
  .settings(
    majorVersion     := 2,
    isPublicArtefact := true,
    sbtPlugin        := true,
    scalaVersion     := "2.12.20",
    libraryDependencies ++= Seq(
      "org.apache.commons"    % "commons-compress"  % "1.27.1"
    )
  ).settings(addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.1"))
