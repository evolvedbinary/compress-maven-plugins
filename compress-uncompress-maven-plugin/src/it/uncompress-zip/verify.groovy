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

final Path inputFile = basedir.toPath().resolve("src/main/resources/folder1.zip")
if (!Files.exists(inputFile) || Files.isDirectory(inputFile)) {
    fail("inputFile is missing.")
}

final Path target = basedir.toPath().resolve("target")
if (!Files.exists(target) || !Files.isDirectory(target)) {
    fail("target directory is missing.")
}

// check the unzipped output directory was created
final Path unzippedOutputDirectory = target.resolve("folder1")
if (!Files.exists(unzippedOutputDirectory)) {
    fail("unzipped output directory is missing.")
}

// uncompress the Zip file
final Path tmpZipOutputDir = Files.createTempDirectory(target, "it-unzip-uncompress-zip")
unzip(inputFile, tmpZipOutputDir)

// check the original files match the extracted contents from the Zip file
final FileVisitor<Path> zipOutputComparisonVisitor = new ZipOutputComparisonVisitor(tmpZipOutputDir, unzippedOutputDirectory)
Files.walkFileTree(tmpZipOutputDir, zipOutputComparisonVisitor)

return true   // NOTE(AR) indicates that the test succeeded


class ZipOutputComparisonVisitor extends SimpleFileVisitor<Path> {
    private final Path basePath
    private final Path unzippedOutputDirectory

    ZipOutputComparisonVisitor(final Path basePath, final Path unzippedOutputDirectory) {
        this.basePath = basePath
        this.unzippedOutputDirectory = unzippedOutputDirectory
    }

    FileVisitResult visitFile(final Path inputFile, final BasicFileAttributes attrs) {
        if (!Files.isDirectory(inputFile)) {
            final String inputFileChecksum = checksum(inputFile)

            final Path relativeInputPath = basePath.relativize(inputFile)
            final Path zipOutputFile = unzippedOutputDirectory.resolve(relativeInputPath)
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

            final Path outputPath = outputDirectory.resolve(zipArchiveEntry.getName())

            if (zipArchiveEntry.isDirectory()) {
                if (!Files.exists(outputPath)) {
                    Files.createDirectories(outputPath)
                }

            } else {
                Files.createFile(outputPath)
                zipFile.getInputStream(zipArchiveEntry).with { zipArchiveEntryInputStream ->
                    Files.copy(zipArchiveEntryInputStream, outputPath, StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }
    }
}