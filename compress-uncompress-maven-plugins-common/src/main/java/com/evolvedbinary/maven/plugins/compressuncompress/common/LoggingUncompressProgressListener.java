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
package com.evolvedbinary.maven.plugins.compressuncompress.common;

import org.apache.maven.plugin.logging.Log;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import static com.evolvedbinary.maven.plugins.compressuncompress.common.Util.formatDuration;
import static com.evolvedbinary.maven.plugins.compressuncompress.common.Util.formatTime;

@NullMarked
public class LoggingUncompressProgressListener implements ProgressListener {

    private final Log log;

    public LoggingUncompressProgressListener(final Log log) {
        this.log = log;
    }

    @Override
    public void started(final Path inputPath, final Path outputFile, final Instant instant) {
        log.info("Started processing: " + inputPath.toString() + " at: " + formatTime(instant));
    }

    @Override
    public void processedFile(final Path inputFile, final Duration duration) {
        log.info("Uncompressed: " + inputFile.toString() + " in: " + formatDuration(duration));
    }

    @Override
    public void finished(final Path inputPath, final Path outputFile, final Duration duration) {
        log.info("Finished uncompressing: " + inputPath.toString() + " to: " + outputFile.toString() + " in: " + formatDuration(duration));
    }
}
