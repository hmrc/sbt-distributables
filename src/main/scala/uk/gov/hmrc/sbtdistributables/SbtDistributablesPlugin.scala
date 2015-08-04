/*
 * Copyright 2015 HM Revenue & Customs
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

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream.LONGFILE_GNU
import org.apache.commons.compress.archivers.tar.{TarArchiveEntry, TarArchiveOutputStream}
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.utils.IOUtils._
import sbt.Keys._
import sbt._

object SbtDistributablesPlugin extends AutoPlugin {

  override def trigger = allRequirements
  val logger = ConsoleLogger()

  lazy val distZip = com.typesafe.sbt.SbtNativePackager.NativePackagerKeys.dist
  lazy val distTgz = TaskKey[sbt.File]("dist-tgz", "create tgz distributable")
  val publishTgz = TaskKey[sbt.File]("publish-tgz", "publish tgz artifact")

  lazy val publishingSettings : Seq[sbt.Setting[_]] = addArtifact(artifact in publishTgz, publishTgz)

  override def projectSettings = Seq(
    distTgz := {
      createTgz(target.value / "universal", name.value, version.value)
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

    distTgz <<= distTgz dependsOn distZip,
    publishLocal <<= publishLocal dependsOn distTgz
  )

  private def createTgz(targetDir: File, name: String, version: String): File = {
    val extraFiles = extraTgzFiles(name)

    val zip = targetDir / s"$name-$version.zip"
    val tgz = targetDir / s"$name-$version.tgz"
    val root = new File(".")

    var inputStream: ZipInputStream = null
    var outputStream: TarArchiveOutputStream = null
    try {
      inputStream = zipInputStream(zip)
      outputStream = tarArchiveOutputStream(tgz)

      outputStream.putArchiveEntry(new TarArchiveEntry(root))
      outputStream.closeArchiveEntry()

      addEntries(extraFiles, outputStream, root)
      copyEntries(inputStream, outputStream, root)
      logger.info(s"Your package is ready in $tgz")
    } finally {
      closeQuietly(inputStream)
      closeQuietly(outputStream)
    }
    tgz
  }

  private def addEntries(extraFiles: Array[(String, String)], outputStream: TarArchiveOutputStream, root: File) {
    for (extraFile <- extraFiles) {
      val bytes: Array[Byte] = extraFile._2.getBytes("UTF-8")
      outputStream.putArchiveEntry(tarArchiveEntry(root, extraFile._1, bytes.length, currentTimeMillis()))
      copy(new ByteArrayInputStream(bytes), outputStream)
      outputStream.closeArchiveEntry()
    }
  }

  private def copyEntries(inputStream: ZipInputStream, outputStream: TarArchiveOutputStream, root: File) {
    var inputZipEntry: ZipEntry = inputStream.getNextEntry
    while (inputZipEntry != null) {
      outputStream.putArchiveEntry(tarArchiveEntry(root, inputZipEntry.getName, inputZipEntry.getSize, inputZipEntry.getTime))
      copy(inputStream, outputStream)
      outputStream.closeArchiveEntry()
      inputZipEntry = inputStream.getNextEntry
    }
  }

  private def tarArchiveEntry(root: File, name: String, size: Long, time: Long): TarArchiveEntry = {
    val outputTarEntry = new TarArchiveEntry(root / name)
    outputTarEntry.setSize(size)
    outputTarEntry.setModTime(time)
    outputTarEntry
  }

  private def extraTgzFiles(name: String) = {
    Array(("Procfile", "web: ./start-docker.sh"),
      ("start-docker.sh", s"""|#!/bin/sh
                                  |
                                  |SCRIPT=$$(find . -type f -name $name)
                                  |exec $$SCRIPT \\
                                  |  $$HMRC_CONFIG
                                  |""".stripMargin))
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