/*
 * Compress Maven Plugins
 * Copyright (C) 2026, Evolved Binary Ltd
 *
 * admin@evolvedbinary.com
 * https://www.evolvedbinary.com
 *
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Use of this software is governed by the Business Source License 1.1
 * included in the LICENSE file and at www.mariadb.com/bsl11.
 *
 * Change Date: 2029-08-10
 *
 * On the date above, in accordance with the Business Source License, use
 * of this software will be governed by the Apache License, Version 2.0.
 *
 * Additional Use Grant: Production use of the Licensed Work for a permitted
 * purpose. A Permitted Purpose is any purpose other than a Competing Use.
 * A Competing Use means making the Software available to others in a commercial
 * product or service that: substitutes for the Software; substitutes for any
 * other product or service we offer using the Software that exists as of the
 * date we make the Software available; or offers the same or substantially
 * similar functionality as the Software.
 */

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile

import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import java.security.DigestInputStream
import java.security.MessageDigest

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.fail

final Path inputDirectory = basedir.toPath().resolve("src/main/resources/folder1")
if (!Files.exists(inputDirectory) || !Files.isDirectory(inputDirectory)) {
    fail("inputDirectory is missing.")
}

final Path target = basedir.toPath().resolve("target")
if (!Files.exists(target) || !Files.isDirectory(target)) {
    fail("target directory is missing.")
}

// check the Zip file was created
final Path zipOutputFile = target.resolve("folder1.zip")
if (!Files.exists(zipOutputFile)) {
    fail("zip output file is missing.")
}

// uncompress the Zip file
final Path tmpZipOutputDir = Files.createTempDirectory(target, "it-unzip-compress-dir-zip")
unzip(zipOutputFile, tmpZipOutputDir)

// check the original files match the extracted contents from the Zip file
final FileVisitor<Path> zipOutputComparisonVisitor = new ZipOutputComparisonVisitor(inputDirectory, tmpZipOutputDir)
Files.walkFileTree(inputDirectory, zipOutputComparisonVisitor)

return true   // NOTE(AR) indicates that the test succeeded


class ZipOutputComparisonVisitor extends SimpleFileVisitor<Path> {
    private final Path basePath
    private final Path tmpZipOutputDir

    ZipOutputComparisonVisitor(final Path basePath, final Path tmpZipOutputDir) {
        this.basePath = basePath
        this.tmpZipOutputDir = tmpZipOutputDir
    }

    FileVisitResult visitFile(final Path inputFile, final BasicFileAttributes attrs) {
        if (!Files.isDirectory(inputFile)) {
            final String inputFileChecksum = checksum(inputFile)

            final Path relativeInputPath = basePath.relativize(inputFile)
            final Path zipOutputFile = tmpZipOutputDir.resolve(relativeInputPath)
            final String zipOutputFileChecksum = checksum(zipOutputFile)

            assertEquals(inputFileChecksum, zipOutputFileChecksum,
                    "inputFile: " + inputFile + " and unzipped file: " + zipOutputFile + " have different checksums!"
            )
        }
        return FileVisitResult.CONTINUE
    }

    def checksum(final Path path) {
        MessageDigest md = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).with { is ->
            new DigestInputStream(is, md).with { dis ->
                while (dis.read() != -1) {
                }

                md = dis.getMessageDigest()

                // bytes to hex
                final StringBuilder result = new StringBuilder()
                for (byte b : md.digest()) {
                    result.append(String.format("%02x", b))
                }
                return result.toString()
            }
        }
    }
}

def unzip(final Path zipOutputFile, final Path outputDirectory) {
    ZipFile.builder().setPath(zipOutputFile).get().with { zipFile ->
        final Enumeration<ZipArchiveEntry> zipFileEntries = zipFile.getEntriesInPhysicalOrder()
        while (zipFileEntries.hasMoreElements()) {
            final ZipArchiveEntry zipArchiveEntry = zipFileEntries.nextElement()

            final Path outputFile = outputDirectory.resolve(zipArchiveEntry.getName())
            final Path outputFileParentDirectory = outputFile.getParent()
            if (!Files.exists(outputFileParentDirectory)) {
                // create parent dir
                Files.createDirectories(outputFileParentDirectory)
            }

            Files.createFile(outputFile)

            zipFile.getInputStream(zipArchiveEntry).with { zipArchiveEntryInputStream ->
                Files.copy(zipArchiveEntryInputStream, outputFile, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}