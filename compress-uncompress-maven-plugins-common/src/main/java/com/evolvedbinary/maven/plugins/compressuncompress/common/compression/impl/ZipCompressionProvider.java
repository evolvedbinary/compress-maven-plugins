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
package com.evolvedbinary.maven.plugins.compressuncompress.common.compression.impl;

import com.evolvedbinary.maven.plugins.compressuncompress.common.compression.api.CompressionProvider;
import com.evolvedbinary.maven.plugins.compressuncompress.common.ProgressListener;
import org.apache.commons.compress.archivers.zip.X7875_NewUnix;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.zip.Deflater;

import static com.evolvedbinary.maven.plugins.compressuncompress.common.Util.IS_WINDOWS;
import static com.evolvedbinary.maven.plugins.compressuncompress.common.Util.getUnixMode;

@NullMarked
public class ZipCompressionProvider implements CompressionProvider {

    @Override
    public boolean isArchiver() {
        return true;
    }

    @Override
    public int getMinCompressionLevel() {
        return Deflater.NO_COMPRESSION;
    }

    @Override
    public int getMaxCompressionLevel() {
        return Deflater.BEST_COMPRESSION;
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
    public void compress(final Path inputPath, final int compressionLevel, final Path outputFile, @Nullable final ProgressListener progressListener) throws IOException {
        @Nullable final Instant startTime;
        if (progressListener != null) {
            startTime = Instant.now();
            progressListener.started(inputPath, outputFile, startTime);
        } else {
            startTime = null;
        }

        try (final ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
            if (compressionLevel != -1) {
                zipArchiveOutputStream.setLevel(compressionLevel);
            }
            zipArchiveOutputStream.setUseZip64(Zip64Mode.AsNeeded);

            if (Files.isDirectory(inputPath)) {
                compressDirectory(inputPath, inputPath, zipArchiveOutputStream, progressListener);
            } else {
                compressFile(inputPath, inputPath.getFileName().toString(), zipArchiveOutputStream, progressListener);
            }
        }

        if (progressListener != null) {
            final Instant endTime = Instant.now();
            progressListener.finished(inputPath, outputFile, Duration.between(startTime, endTime));
        }
    }

    private static void compressDirectory(final Path basePath, final Path inputDirectory, final ZipArchiveOutputStream zipArchiveOutputStream, @Nullable final ProgressListener progressListener) throws IOException {
        final FileVisitor<Path> compressDirectoryVisitor = new CompressDirectoryVisitor(basePath, zipArchiveOutputStream, progressListener);
        Files.walkFileTree(inputDirectory, compressDirectoryVisitor);
    }

    private static class CompressDirectoryVisitor extends SimpleFileVisitor<Path> {
        private final Path basePath;
        private final ZipArchiveOutputStream zipArchiveOutputStream;
        private @Nullable final ProgressListener progressListener;

        public CompressDirectoryVisitor(final Path basePath, final ZipArchiveOutputStream zipArchiveOutputStream, @Nullable final ProgressListener progressListener) {
            this.basePath = basePath;
            this.zipArchiveOutputStream = zipArchiveOutputStream;
            this.progressListener = progressListener;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            if (!Files.isDirectory(file)) {
                final Path relativePath = basePath.relativize(file);
                compressFile(file, relativePath.toString(), zipArchiveOutputStream, progressListener);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private static void compressFile(final Path inputFile, final String name, final ZipArchiveOutputStream zipArchiveOutputStream, @Nullable final ProgressListener progressListener) throws IOException {
        @Nullable final Instant startTime;
        if (progressListener != null) {
            startTime = Instant.now();
        } else {
            startTime = null;
        }

        final ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(name);

        try {
            final long size = Files.size(inputFile);
            zipArchiveEntry.setSize(size);
        } catch (final IOException e) {
            // no-op
        }

        try {
            final FileTime creationTime = (FileTime) Files.getAttribute(inputFile, "creationTime");
            zipArchiveEntry.setCreationTime(creationTime);
        } catch (final UnsupportedOperationException | IOException e) {
            // no-op
        }

        try {
            final FileTime lastModifiedTime = Files.getLastModifiedTime(inputFile);
            zipArchiveEntry.setLastModifiedTime(lastModifiedTime);
        } catch (final IOException e) {
            // no-op
        }

        if (!IS_WINDOWS) {
            try {
                final int unixMode = getUnixMode(inputFile);
                if (unixMode != 0) {
                    zipArchiveEntry.setUnixMode(unixMode);
                }
            } catch (final UnsupportedOperationException | IOException e) {
                // no-op
            }

            try {
                final X7875_NewUnix unixExtraField = new X7875_NewUnix();
                final int uid = (int) Files.getAttribute(inputFile, "unix:uid");
                unixExtraField.setUID(uid);
                final int gid = (int) Files.getAttribute(inputFile, "unix:gid");
                unixExtraField.setGID(gid);
                zipArchiveEntry.addAsFirstExtraField(unixExtraField);
            } catch (final UnsupportedOperationException | IOException e) {
                // no-op
            }
        }

        zipArchiveOutputStream.putArchiveEntry(zipArchiveEntry);
        zipArchiveOutputStream.write(inputFile);
        zipArchiveOutputStream.closeArchiveEntry();

        if (progressListener != null) {
            final Instant endTime = Instant.now();
            progressListener.processedFile(inputFile, Duration.between(startTime, endTime));
        }
    }
}
