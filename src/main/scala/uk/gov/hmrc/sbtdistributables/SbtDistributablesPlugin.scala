/*
 * Copyright 2017 HM Revenue & Customs
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

import java.io.{File, _}
import java.lang.System.currentTimeMillis
import java.util.zip.{ZipEntry, ZipInputStream}

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream._
import org.apache.commons.compress.archivers.tar.{TarArchiveEntry, TarArchiveOutputStream}
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.utils.IOUtils._
import sbt.Keys._
import sbt._

object SbtDistributablesPlugin extends AutoPlugin {

  override def trigger = allRequirements
  val logger = ConsoleLogger()

  lazy val distZip = com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerKeys.dist
  lazy val distTgzTask = TaskKey[sbt.File]("dist-tgz", "create tgz distributable")
  val publishTgz = TaskKey[sbt.File]("publish-tgz", "publish tgz artifact")

  lazy val extraFiles = SettingKey[Seq[File]]("extraFiles", "Extra files to be added to the tgz")

  private val FILE_MODE_755 = 493

  lazy val publishingSettings : Seq[sbt.Setting[_]] = addArtifact(artifact in publishTgz, publishTgz)

  override def projectSettings = Seq(
    extraFiles := Seq.empty[File],
    distTgzTask := {
      createTgz(target.value / "universal", name.value, version.value, javaRuntimeVersion(scalacOptions.value), extraFiles.value)
    },

    artifact in publishTgz ~= {
      (art: Artifact) => art.copy(`type` = "zip", extension = "tgz")
    },

    publishTgz <<= (target, normalizedName, version) map {
      (targetDir, id, version) => targetDir / "universal" / s"$id-$version.tgz"
    },

    publishArtifact in(Test, packageDoc) := false,
    publishArtifact in(Test, packageSrc) := false,
    publishArtifact in(Test, packageBin) := false,
    publishArtifact in(Compile, packageDoc) := false,
    publishArtifact in(Compile, packageSrc) := false,
    publishArtifact in(Compile, packageBin) := true,

    distTgzTask <<= distTgzTask dependsOn distZip,
    publishLocal <<= publishLocal dependsOn distTgzTask
  )

  private def createTgz(targetDir: File, artifactName: String, version: String, javaRuntimeVersion: String, extraFiles: Seq[File]): File = {
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
      copyEntries(inputStream, outputStream, root, artifactName)
      logger.info(s"Your package is ready in $tgz")
    } finally {
      closeQuietly(inputStream)
      closeQuietly(outputStream)
    }
    tgz
  }

  private def addEntries(outputStream: TarArchiveOutputStream, root: File, extraFiles: Array[(String, String, Option[Int])]) =
    for (extraFile <- extraFiles) {
      val bytes = extraFile._2.getBytes("UTF-8")
      addEntry(outputStream, root, new ByteArrayInputStream(bytes), extraFile._1, bytes.length)
    }


  private def addEntries(outputStream: TarArchiveOutputStream, root: File, extraFiles: Seq[File]) =
    extraFiles.foreach { extraFile =>
      val stream = new FileInputStream(extraFile.getAbsoluteFile)
      addEntry(outputStream, root, stream, extraFile.name, extraFile.getAbsoluteFile.length)
    }


  private def addEntry(outputStream: TarArchiveOutputStream, root: File, stream: InputStream, name: String, size: Long) = {
    outputStream.putArchiveEntry(tarArchiveEntry(root, name, size, currentTimeMillis(), None))
    copy(stream, outputStream)
    outputStream.closeArchiveEntry()
  }

  private def copyEntries(inputStream: ZipInputStream, outputStream: TarArchiveOutputStream, root: File, artifactName: String) {
    var inputZipEntry: ZipEntry = inputStream.getNextEntry
    while (inputZipEntry != null) {
      outputStream.putArchiveEntry(tarArchiveEntry(root, inputZipEntry.getName, inputZipEntry.getSize, inputZipEntry.getTime, getTarEntryMode(inputZipEntry.getName, artifactName)))
      copy(inputStream, outputStream)
      outputStream.closeArchiveEntry()
      inputZipEntry = inputStream.getNextEntry
    }
  }

  private def getTarEntryMode(zipEntryName: String, artifactName: String): Option[Int] = {
    if (zipEntryName.endsWith(s"/bin/$artifactName")) {
      Some(FILE_MODE_755)
    } else {
      None
    }
  }

  private def tarArchiveEntry(root: File, name: String, size: Long, time: Long, mode: Option[Int]): TarArchiveEntry = {
    val outputTarEntry = new TarArchiveEntry(root / name)
    outputTarEntry.setSize(size)
    outputTarEntry.setModTime(time)
    mode.foreach(m => outputTarEntry.setMode(m))
    outputTarEntry
  }

  private def extraTgzFiles(javaRuntimeVersion: String): Array[(String, String, Option[Int])] = {
    Array(("system.properties", s"java.runtime.version=$javaRuntimeVersion", None))
  }

  private def javaRuntimeVersion(scalacOptions: Seq[String]): String = {
    if (scalacOptions.contains("-target:jvm-1.8")) "1.8" else "1.7"
  }

  private def zipInputStream(zip: File): ZipInputStream = {
    new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)))
  }

  private def tarArchiveOutputStream(file: File): TarArchiveOutputStream = {
    val outputStream = new TarArchiveOutputStream(new GzipCompressorOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
    outputStream.setLongFileMode(LONGFILE_GNU)
    outputStream
  }
}
