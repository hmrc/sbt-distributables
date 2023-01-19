/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.sbtdistributables

import java.io._
import java.lang.System.currentTimeMillis
import java.util.zip.{ZipEntry, ZipInputStream}

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream._
import org.apache.commons.compress.archivers.tar.{TarArchiveEntry, TarArchiveOutputStream}
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.utils.IOUtils._
import sbt.Keys._
import sbt._

object SbtDistributablesPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = noTrigger
  val logger = ConsoleLogger()

  lazy val distZip = com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerKeys.dist
  lazy val distTgzTask = TaskKey[sbt.File]("dist-tgz", "create tgz distributable")
  val publishTgz = TaskKey[sbt.File]("publish-tgz", "publish tgz artifact")

  lazy val extraFiles = SettingKey[Seq[File]]("extraFiles", "Extra files to be added to the tgz")
  lazy val executableFilesInTar = SettingKey[Seq[String]]("executableFilesInTar", "Files to made executable in tar")

  private val FILE_MODE_775 = 493

  lazy val publishingSettings : Seq[sbt.Setting[_]] = addArtifact(publishTgz / artifact , publishTgz)

  override def projectSettings = Seq(
    extraFiles := Seq.empty[File],
    executableFilesInTar := Seq.empty[String],
    distTgzTask := {
      createTgz(target.value / "universal", name.value, version.value, javaRuntimeVersion(scalacOptions.value), extraFiles.value, executableFilesInTar.value)
    },

    publishTgz / artifact ~= {
      art: Artifact => art.withType("zip").withExtension("tgz")
    },

    publishTgz := target.value / "universal" / s"${normalizedName.value}-${version.value}.tgz",

    Test    / packageDoc / publishArtifact := false,
    Test    / packageSrc / publishArtifact := false,
    Test    / packageBin / publishArtifact := false,
    Compile / packageDoc / publishArtifact := false,
    Compile / packageSrc / publishArtifact := false,
    Compile / packageBin / publishArtifact := true,

    distTgzTask := (distTgzTask dependsOn distZip).value,
    publishLocal := (publishLocal dependsOn distTgzTask).value
  ) ++ publishingSettings

  private def createTgz(
    targetDir           : File,
    artifactName        : String,
    version             : String,
    javaRuntimeVersion  : String,
    extraFiles          : Seq[File],
    executableFilesInTar: Seq[String]
  ): File = {
    val externalTgzFiles = extraTgzFiles(javaRuntimeVersion)

    val zip = targetDir / s"$artifactName-$version.zip"
    val tgz = targetDir / s"$artifactName-$version.tgz"
    val root = new File(".")

    var inputStream: ZipInputStream = null
    var outputStream: TarArchiveOutputStream = null
    try {
      inputStream = zipInputStream(zip)
      outputStream = tarArchiveOutputStream(tgz)

      outputStream.putArchiveEntry(new TarArchiveEntry(root))
      outputStream.closeArchiveEntry()

      addEntries(outputStream, root, extraFiles)
      addEntries(outputStream, root, externalTgzFiles)
      copyEntries(inputStream, outputStream, root, artifactName, executableFilesInTar)
      logger.info(s"Your package is ready in $tgz")
    } finally {
      closeQuietly(inputStream)
      closeQuietly(outputStream)
    }
    tgz
  }

  private def addEntries(
    outputStream: TarArchiveOutputStream,
    root        : File,
    extraFiles  : Array[(String, String, Option[Int])]
  ): Unit =
    for (extraFile <- extraFiles) {
      val bytes = extraFile._2.getBytes("UTF-8")
      addEntry(outputStream, root, new ByteArrayInputStream(bytes), extraFile._1, bytes.length)
    }


  private def addEntries(outputStream: TarArchiveOutputStream, root: File, extraFiles: Seq[File]): Unit =
    extraFiles.foreach { extraFile =>
      val stream = new FileInputStream(extraFile.getAbsoluteFile)
      addEntry(outputStream, root, stream, extraFile.name, extraFile.getAbsoluteFile.length)
    }


  private def addEntry(
    outputStream: TarArchiveOutputStream,
    root        : File,
    stream      : InputStream,
    name        : String,
    size        : Long
  ): Unit = {
    outputStream.putArchiveEntry(tarArchiveEntry(root, name, size, currentTimeMillis(), None))
    copy(stream, outputStream)
    outputStream.closeArchiveEntry()
  }

  private def copyEntries(
    inputStream    : ZipInputStream,
    outputStream   : TarArchiveOutputStream,
    root           : File,
    artifactName   : String,
    executableFiles: Seq[String]
  ): Unit = {
    var inputZipEntry: ZipEntry = inputStream.getNextEntry
    while (inputZipEntry != null) {
      outputStream.putArchiveEntry(
        tarArchiveEntry(
          root,
          inputZipEntry.getName,
          inputZipEntry.getSize,
          inputZipEntry.getTime,
          getTarEntryMode(inputZipEntry.getName, artifactName, executableFiles)
        )
      )
      copy(inputStream, outputStream)
      outputStream.closeArchiveEntry()
      inputZipEntry = inputStream.getNextEntry
    }
  }

  private def getTarEntryMode(zipEntryName: String, artifactName: String, executableFiles: Seq[String]): Option[Int] =
    if (zipEntryName.endsWith(s"/bin/$artifactName") || executableFiles.exists(f => zipEntryName.endsWith( s"/bin/$f")))
      Some(FILE_MODE_775)
    else
      None

  private def tarArchiveEntry(root: File, name: String, size: Long, time: Long, mode: Option[Int]): TarArchiveEntry = {
    val outputTarEntry = new TarArchiveEntry(root / name)
    outputTarEntry.setSize(size)
    outputTarEntry.setModTime(time)
    mode.foreach(m => outputTarEntry.setMode(m))
    outputTarEntry
  }

  private def extraTgzFiles(javaRuntimeVersion: String): Array[(String, String, Option[Int])] =
    Array(("system.properties", s"java.runtime.version=$javaRuntimeVersion", None))

  private def javaRuntimeVersion(scalacOptions: Seq[String]): String =
    if (scalacOptions.contains("-target:jvm-1.8")) "1.8" else "1.7"

  private def zipInputStream(zip: File): ZipInputStream =
    new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)))

  private def tarArchiveOutputStream(file: File): TarArchiveOutputStream = {
    val outputStream = new TarArchiveOutputStream(
                         new GzipCompressorOutputStream(
                          new BufferedOutputStream(
                            new FileOutputStream(file))))
    outputStream.setLongFileMode(LONGFILE_GNU)
    outputStream
  }
}
