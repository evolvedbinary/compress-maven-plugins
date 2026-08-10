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
package com.evolvedbinary.maven.plugins.compressuncompress.common.uncompression.api;

import com.evolvedbinary.maven.plugins.compressuncompress.common.ProgressListener;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

@NullMarked
public interface UncompressionProvider {

    /**
     * Returns true if the uncompression provider can also unarchive multiple files e.g. Zip, TAR, etc.
     *
     * @return true if the uncompression provider can also unarchive.
     */
    boolean isUnarchiver();

    /**
     * Get the name of the algorithm used by the uncompression provider.
     */
    String getAlgorithmName();

    /**
     * Get the default file extension name for compressed files of this type.
     *
     * @return the default file extension, e.g. 'zip'
     */
    String getDefaultFileExtension();

    /**
     * Uncompress the input file to the output directory.
     *
     * @param inputFile where to read the data to uncompress.
     * @param outputPath where to write the uncompressed data.
     *
     * @throws IOException if an I/O error occurs.
     */
    void uncompress(Path inputFile, Path outputPath, @Nullable ProgressListener progressListener) throws IOException;
}
