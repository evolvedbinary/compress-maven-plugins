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
package com.evolvedbinary.maven.plugins.compressuncompress.common.uncompression.impl;

import com.evolvedbinary.maven.plugins.compressuncompress.common.ProgressListener;
import com.evolvedbinary.maven.plugins.compressuncompress.common.uncompression.api.UncompressionProvider;
import org.apache.commons.compress.archivers.zip.X7875_NewUnix;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.time.Instant;
import java.util.Enumeration;
import java.util.Set;

import static com.evolvedbinary.maven.plugins.compressuncompress.common.Util.IS_WINDOWS;
import static com.evolvedbinary.maven.plugins.compressuncompress.common.Util.toUnixModeStr;

@NullMarked
public class ZipUncompressionProvider implements UncompressionProvider {

    @Override
    public boolean isUnarchiver() {
        return true;
    }

    @Override
    public String getAlgorithmName() {
        return "zip";
    }

    @Override
    public String getDefaultFileExtension() {
        return "zip";
    }

    @Override
    public void uncompress(final Path inputFile, final Path outputDirectory, @Nullable final ProgressListener progressListener) throws IOException {
        @Nullable final Instant startTime;
        if (progressListener != null) {
            startTime = Instant.now();
            progressListener.started(inputFile, outputDirectory, startTime);
        } else {
            startTime = null;
        }

        try (final ZipFile zipFile = ZipFile.builder().setPath(inputFile).get()) {
            final Enumeration<ZipArchiveEntry> zipFileEntries = zipFile.getEntriesInPhysicalOrder();
            while (zipFileEntries.hasMoreElements()) {
                final ZipArchiveEntry zipArchiveEntry = zipFileEntries.nextElement();
                if (zipArchiveEntry.isDirectory()) {
                    uncompressDirectory(zipArchiveEntry, outputDirectory);
                } else {
                    uncompressFile(zipFile, zipArchiveEntry, outputDirectory, progressListener);
                }
            }
        }

        if (progressListener != null) {
            final Instant endTime = Instant.now();
            progressListener.finished(inputFile, outputDirectory, Duration.between(startTime, endTime));
        }
    }

    private static void uncompressDirectory(final ZipArchiveEntry zipArchiveEntry, final Path outputDirectory) throws IOException {
        final Path newOutputDirectory = outputDirectory.resolve(zipArchiveEntry.getName());
        if (!Files.exists(newOutputDirectory)) {
            // create dir
            Files.createDirectories(newOutputDirectory);
        }
    }

    private static void uncompressFile(final ZipFile zipFile, final ZipArchiveEntry zipArchiveEntry, final Path outputDirectory, @Nullable final ProgressListener progressListener) throws IOException {
        @Nullable final Instant startTime;
        if (progressListener != null) {
            startTime = Instant.now();
        } else {
            startTime = null;
        }

        final Path outputFile = outputDirectory.resolve(zipArchiveEntry.getName());

        final FileAttribute[] fileAttributes;
        if (!IS_WINDOWS) {
            final int unixMode = zipArchiveEntry.getUnixMode();
            if (unixMode > 0) {
                final Set<PosixFilePermission> posixFilePermissions = PosixFilePermissions.fromString(toUnixModeStr(unixMode));
                fileAttributes = new FileAttribute[] { PosixFilePermissions.asFileAttribute(posixFilePermissions) };
            } else {
                fileAttributes = new FileAttribute[0];
            }
        } else {
            fileAttributes = new FileAttribute[0];
        }

        Files.createFile(outputFile, fileAttributes);

        if (!IS_WINDOWS) {
            final ZipExtraField[] extraFields = zipArchiveEntry.getExtraFields();
            for (final ZipExtraField extraField : extraFields) {
                if (extraField instanceof X7875_NewUnix) {
                    final X7875_NewUnix unixExtraField = (X7875_NewUnix) extraField;
                    final long uid = unixExtraField.getUID();
                    final long gid = unixExtraField.getGID();
                    try {
                        Files.setAttribute(outputFile, "unix:uid", (int) uid);
                        Files.setAttribute(outputFile, "unix:gid", (int) gid);
                    } catch (final UnsupportedOperationException | IOException e) {
                        // no-op
                    }
                    break;
                }
            }
        }

        @Nullable final FileTime creationTime = zipArchiveEntry.getCreationTime();
        if (creationTime != null) {
            try {
                Files.setAttribute(outputFile, "creationTime", creationTime);
            } catch (final UnsupportedOperationException | IOException e) {
                // no-op
            }
        }

        @Nullable final FileTime lastModifiedTime = zipArchiveEntry.getLastModifiedTime();
        if (lastModifiedTime != null) {
            try {
                Files.setLastModifiedTime(outputFile, lastModifiedTime);
            } catch (final UnsupportedOperationException | IOException e) {
                // no-op
            }
        }

        try (final InputStream zipArchiveEntryInputStream = zipFile.getInputStream(zipArchiveEntry)) {
            Files.copy(zipArchiveEntryInputStream, outputFile, StandardCopyOption.REPLACE_EXISTING);
        }

        if (progressListener != null) {
            final Instant endTime = Instant.now();
            progressListener.processedFile(outputFile, Duration.between(startTime, endTime));
        }
    }
}
