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
package com.evolvedbinary.maven.plugins.compressuncompress.common.compression.api;

import com.evolvedbinary.maven.plugins.compressuncompress.common.ProgressListener;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

@NullMarked
public interface CompressionProvider {

    /**
     * Returns true if the compression provider can also archive multiple files e.g. Zip, TAR, etc.
     *
     * @return true if the compression provider can also archive.
     */
    boolean isArchiver();

    /**
     * Get the minimum compression level supported.
     *
     * @return the minimum compression level supported, or -1 if configuring the compression level is unsupported.
     */
    int getMinCompressionLevel();

    /**
     * Get the maximum compression level supported.
     *
     * @return the maximum compression level supported, or -1 if configuring the compression level is unsupported.
     */
    int getMaxCompressionLevel();

    /**
     * Get the name of the algorithm used by the compression provider.
     */
    String getAlgorithmName();

    /**
     * Get the default file extension name for compressed files of this type.
     *
     * @return the default file extension, e.g. 'zip'
     */
    String getDefaultFileExtension();

    /**
     * Compress the input path to the output path.
     *
     * @param inputPath where to read the data to compress.
     * @param compressionLevel the level of compression to apply.
     * @param outputFile where to write the compressed data.
     * @param progressListener an optional listener that can receive progress messages.
     *
     * @throws IOException if an I/O error occurs.
     */
    void compress(Path inputPath, int compressionLevel, Path outputFile, @Nullable ProgressListener progressListener) throws IOException;
}
