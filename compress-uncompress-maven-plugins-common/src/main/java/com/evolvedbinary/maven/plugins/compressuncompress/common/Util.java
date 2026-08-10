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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Util {

    /**
     * The name of the Operating System.
     */
    public static final String OS_NAME = System.getProperty("os.name");

    /**
     * True if the Operating System is Windows.
     */
    public static final boolean IS_WINDOWS = OS_NAME.toLowerCase().startsWith("windows");

    private static final DateTimeFormatter DTF_HUMAN_TIME = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    /**
     * Format an instant as HH:MM:SS.
     *
     * @param instant the instant to format.
     *
     * @return the formatted instant string.
     */
    public static String formatTime(final Instant instant) {
        return DTF_HUMAN_TIME.format(instant);
    }

    /**
     * Format a duration as HH:MM:SS
     *
     * @param duration the duration to format.
     *
     * @return the formatted duration string.
     */
    public static String formatDuration(final Duration duration) {
        final long millis = duration.toMillis();
        final long hours = TimeUnit.MILLISECONDS.toHours(millis);
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Get the Unix Mode as an Octal number from the file path.
     *
     * @param path the file to get the mode for.
     *
     * @return the octal unix mode.
     *
     * @throws IOException if an I/O error occurs.
     */
    public static int getUnixMode(final Path path) throws UnsupportedOperationException, IOException {
        final Set<PosixFilePermission> posixFilePermissions = Files.getPosixFilePermissions(path);
        int mode = 0;
        for (final PosixFilePermission posixFilePermission : posixFilePermissions) {
            switch (posixFilePermission) {
                case OWNER_READ:
                    mode |= 0400;
                    break;

                case OWNER_WRITE:
                    mode |= 0200;
                    break;

                case OWNER_EXECUTE:
                    mode |= 0100;
                    break;

                case GROUP_READ:
                    mode |= 040;
                    break;

                case GROUP_WRITE:
                    mode |= 020;
                    break;

                case GROUP_EXECUTE:
                    mode |= 010;
                    break;

                case OTHERS_READ:
                    mode |= 04;
                    break;

                case OTHERS_WRITE:
                    mode |= 02;
                    break;

                case OTHERS_EXECUTE:
                    mode |= 01;
                    break;
            }
        }

        return mode;
    }

    /**
     * Convert a Unix Mode as an Octal number to a String representation.
     *
     * @param mode the Octal mode.
     *
     * @return the string representation.
     */
    public static String toUnixModeStr(final int mode) {
        final char[] modeStr = new char[] {
                (mode & 0400) == 0400 ? 'r' : '-',
                (mode & 0200) == 0200 ? 'w' : '-',
                (mode & 0100) == 0100 ? 'x' : '-',
                (mode & 040) == 040 ? 'r' : '-',
                (mode & 020) == 020 ? 'w' : '-',
                (mode & 010) == 010 ? 'x' : '-',
                (mode & 04) == 04 ? 'r' : '-',
                (mode & 02) == 02 ? 'w' : '-',
                (mode & 01) == 01 ? 'x' : '-'
        };
        return new String(modeStr);
    }
}
