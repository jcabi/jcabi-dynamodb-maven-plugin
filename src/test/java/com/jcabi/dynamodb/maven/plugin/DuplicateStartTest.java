/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.maven.plugin;

import com.jcabi.dynamodb.core.Instances;
import java.io.File;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Duplicate port tests for {@link Instances}.
 *
 * @since 0.1
 */
final class DuplicateStartTest {

    @Test
    void rejectsDuplicateBeforeLaunchingAnotherProcess(@TempDir final Path temp)
        throws Exception {
        final int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        final Instances instances = new Instances();
        final File dist = new File(System.getProperty("surefire.dist"));
        instances.start(
            dist, port, new File(System.getProperty("java.home")),
            Collections.singletonList("-inMemory")
        );
        try {
            Assertions.assertThrows(
                IllegalStateException.class,
                () -> instances.start(
                    dist, port, temp.resolve("missing-java-home").toFile(),
                    Collections.singletonList("-inMemory")
                )
            );
        } finally {
            instances.stop(port);
        }
    }
}
