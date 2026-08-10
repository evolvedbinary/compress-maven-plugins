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

import com.evolvedbinary.maven.plugins.compressuncompress.common.compression.api.CompressionProvider;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.ServiceLoader;

@NullMarked
public class AbstractCompressMojo extends AbstractMojo {

    /**
     * Input file or directory.
     */
    @Parameter(required = true)
    private String inputPath;

    /**
     * The compression algorithm to use.
     */
    @Parameter(required = true)
    private String algorithm;

    /**
     * The level of compression to apply.
     */
    @Parameter(required = false)
    private int compressionLevel = -1;

    /**
     * Output file or directory.
     * If it's a directory we then use input filename or directory name as
     * a prefix to the new output filename.
     */
    @Parameter(required = true)
    private String outputPath;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // prepare the input path
        final Path input = Paths.get(inputPath).normalize().toAbsolutePath();
        if (!Files.exists(input)) {
            final String errorMessage = "The inputPath: " + inputPath + " does not exist!";
            getLog().error(errorMessage);
            throw new MojoFailureException(errorMessage);
        }
        final boolean inputIsDirectory = Files.isDirectory(input);

        // find a compression provider for the request algorithm
        final CompressionProvider compressionProvider = getCompressionProvider(inputIsDirectory);

        // prepare the output path
        Path output = Paths.get(outputPath).normalize().toAbsolutePath();
        if (Files.exists(output)) {
            if (Files.isDirectory(output)) {
                // Supplied outputPath is a directory so append the input filename or directory name as prefix to new output filename
                String outputFileName = input.getFileName().toString();

                // does the outputFileName have a file extension, if not add one
                if (outputFileName.indexOf('.') == -1) {
                    outputFileName += "." + compressionProvider.getDefaultFileExtension();
                }

                output = output.resolve(outputFileName);
            } else {
                final String errorMessage = "The outputPath: " + outputPath + " already exists, avoiding overwrite!";
                getLog().error(errorMessage);
                throw new MojoFailureException(errorMessage);
            }
        } else {
            // create the parent output path
            final Path outputParent = output.getParent();
            try {
                Files.createDirectories(outputParent);
            } catch (final IOException e) {
                final String errorMessage = "Unable to create directory for outputPath: " + outputPath + ": " + e.getMessage();
                getLog().error(errorMessage);
                throw new MojoExecutionException(errorMessage);
            }
            String outputFileName = output.getFileName().toString();
            // does the outputFileName have a file extension, if not add one
            if (outputFileName.indexOf('.') == -1) {
                outputFileName += "." + compressionProvider.getDefaultFileExtension();
            }
            output = outputParent.resolve(outputFileName);
        }

        final ProgressListener progressListener = new LoggingCompressProgressListener(getLog());

        // perform the compression
        try {
            compressionProvider.compress(input, compressionLevel, output, progressListener);
        } catch (final IOException e) {
            final String errorMessage = "Unable to compress inputPath: " + inputPath + ": " + e.getMessage();
            getLog().error(errorMessage);
            throw new MojoExecutionException(errorMessage);
        }
    }

    private CompressionProvider getCompressionProvider(final boolean needsArchiver) throws MojoFailureException {
        final ServiceLoader<CompressionProvider> serviceLoader = ServiceLoader.load(CompressionProvider.class);
        final Iterator<CompressionProvider> iter = serviceLoader.iterator();
        while (iter.hasNext()) {
            final CompressionProvider compressionProvider = iter.next();
            if (compressionProvider.getAlgorithmName().equals(algorithm)) {

                if (needsArchiver && !compressionProvider.isArchiver()) {
                    final String errorMessage = "The inputPath: " + inputPath + " is a directory, but the algorithm: " + algorithm + " does not support directories!";
                    getLog().error(errorMessage);
                    throw new MojoFailureException(errorMessage);
                }

                if (compressionLevel > - 1) {
                    // user has requested a specific compression level, check it is in range

                    if (compressionProvider.getMinCompressionLevel() == -1) {
                        final String errorMessage = "The algorithm: " + algorithm + " does not support setting a compression level, but compressionLevel: " + compressionLevel + " was requested!";
                        getLog().error(errorMessage);
                        throw new MojoFailureException(errorMessage);
                    }

                    if (compressionLevel < compressionProvider.getMinCompressionLevel() || compressionLevel > compressionProvider.getMaxCompressionLevel()) {
                        final String errorMessage = "The algorithm: " + algorithm + " supports compression levels: " + compressionProvider.getMinCompressionLevel() + " to " + compressionProvider.getMaxCompressionLevel() + ", but compressionLevel: " + compressionLevel + " was requested!";
                        getLog().error(errorMessage);
                        throw new MojoFailureException(errorMessage);
                    }

                    return compressionProvider;
                }
            }
        }

        final String errorMessage = "No compression provider found for the algorithm: " + algorithm + "!";
        getLog().error(errorMessage);
        throw new MojoFailureException(errorMessage);
    }

}
