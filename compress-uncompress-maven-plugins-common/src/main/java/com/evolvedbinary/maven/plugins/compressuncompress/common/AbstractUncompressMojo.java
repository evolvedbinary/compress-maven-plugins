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

import com.evolvedbinary.maven.plugins.compressuncompress.common.uncompression.api.UncompressionProvider;
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
public class AbstractUncompressMojo extends AbstractMojo {

    /**
     * Input file.
     */
    @Parameter(required = true)
    private String inputFile;

    /**
     * The uncompression algorithm to use.
     */
    @Parameter(required = true)
    private String algorithm;

    /**
     * Output file or directory.
     * If {@link #algorithm} indicates an unarchiver, if the path exists
     * then it must be a directory, if the path does not exist, it is created.
     * Otherwise, if it is not an unarchiver, if the path exists and it's a directory we then use input filename as prefix to
     * the new output filename, otherwise we create it.
     */
    @Parameter(required = true)
    private String outputPath;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // prepare the input path
        final Path input = Paths.get(inputFile).normalize().toAbsolutePath();
        if (!Files.exists(input)) {
            final String errorMessage = "The inputFile: " + inputFile + " does not exist!";
            getLog().error(errorMessage);
            throw new MojoFailureException(errorMessage);
        }
        if (Files.isDirectory(input)) {
            final String errorMessage = "The inputFile: " + inputFile + " is a directory and not a file!";
            getLog().error(errorMessage);
            throw new MojoFailureException(errorMessage);
        }

        // find a uncompression provider for the request algorithm
        final UncompressionProvider uncompressionProvider = getUncompressionProvider();

        // prepare the output path
        Path output = Paths.get(outputPath).normalize().toAbsolutePath();
        if (uncompressionProvider.isUnarchiver()) {
            // uncompressionProvider provider is an archiver
            if (Files.exists(output)) {
                if (!Files.isDirectory(output)) {
                    final String errorMessage = "The outputPath: " + outputPath + " already exists and is not a directory, avoiding overwrite!";
                    getLog().error(errorMessage);
                    throw new MojoFailureException(errorMessage);
                }
            } else {
                // create the output directory
                try {
                    Files.createDirectories(output);
                } catch (final IOException e) {
                    final String errorMessage = "Unable to create directory for outputPath: " + outputPath + ": " + e.getMessage();
                    getLog().error(errorMessage);
                    throw new MojoExecutionException(errorMessage, e);
                }
            }

        } else {
            // uncompressionProvider provider is NOT an archiver
            if (Files.exists(output)) {
                if (Files.isDirectory(output)) {
                    // Supplied outputPath is a directory so append the input filename or directory name as prefix to new output filename
                    String outputFileName = input.getFileName().toString();
                    final String fileExtension = "." + uncompressionProvider.getDefaultFileExtension();
                    if (outputFileName.endsWith(fileExtension)) {
                        // remove the compression filename suffix
                        outputFileName = outputFileName.substring(0, outputFileName.length() - fileExtension.length());
                    }
                    output = output.resolve(outputFileName);
                } else {
                    final String errorMessage = "The outputPath: " + outputPath + " already exists and is not a directory, avoiding overwrite!";
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
                    throw new MojoExecutionException(errorMessage, e);
                }
            }
        }

        final ProgressListener progressListener = new LoggingUncompressProgressListener(getLog());

        // perform the uncompression
        try {
            uncompressionProvider.uncompress(input, output, progressListener);
        } catch (final IOException e) {
            final String errorMessage = "Unable to uncompress inputFile " + inputFile + ": " + e.getMessage();
            getLog().error(errorMessage);
            throw new MojoExecutionException(errorMessage, e);
        }
    }

    private UncompressionProvider getUncompressionProvider() throws MojoFailureException {
        final ServiceLoader<UncompressionProvider> serviceLoader = ServiceLoader.load(UncompressionProvider.class);
        final Iterator<UncompressionProvider> iter = serviceLoader.iterator();
        while (iter.hasNext()) {
            final UncompressionProvider uncompressionProvider = iter.next();
            if (uncompressionProvider.getAlgorithmName().equals(algorithm)) {
                return uncompressionProvider;
            }
        }

        final String errorMessage = "No uncompression provider found for the algorithm: " + algorithm + "!";
        getLog().error(errorMessage);
        throw new MojoFailureException(errorMessage);
    }

}
