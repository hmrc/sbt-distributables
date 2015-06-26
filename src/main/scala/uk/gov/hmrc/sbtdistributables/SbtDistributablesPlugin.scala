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
import org.apache.commons.compress.utils.IOUtils
import org.apache.commons.compress.utils.IOUtils._
import sbt.Keys._
import sbt._

object SbtDistributablesPlugin extends AutoPlugin {

  override def trigger = allRequirements

  val distZip = com.typesafe.sbt.SbtNativePackager.NativePackagerKeys.dist
  val distTgz = TaskKey[sbt.File]("dist-tgz", "create tgz distributable")

  override def projectSettings = Seq(
    artifact in distZip ~= {
      (art: Artifact) => art.copy(`type` = "zip", extension = "zip")
    },

    distZip <<= (target, normalizedName, version) map {
      (targetDir, id, version) => targetDir / "universal" / s"$id-$version.zip"
    },

    artifact in distTgz ~= {
      (art: Artifact) => art.copy(`type` = "zip", extension = "tgz")
    },

    distTgz <<= (target, normalizedName, version) map {
      createTgz
    },

    distZip <<= distZip dependsOn Keys.`package`,
    distTgz <<= distTgz dependsOn distZip
  ) ++ addArtifact(artifact in distZip, distZip) ++
       addArtifact(artifact in distTgz, distTgz)

  private def createTgz (targetDir: Types.Id[File], name: Types.Id[String], version: Types.Id[String]): File = {
    val extraFiles = extraTgzFiles(name)

    val universalTargetDir = new File(targetDir, "universal")
    val zip = universalTargetDir / s"$name-$version.zip"
    val tgz = universalTargetDir / s"$name-$version.tgz"
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

  private def extraTgzFiles(name: Types.Id[String]): Array[(String, String)] = {
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