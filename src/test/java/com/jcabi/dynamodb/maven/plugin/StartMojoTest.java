/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.maven.plugin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link StartMojo} (more detailed test is in maven invoker).
 * @since 0.1
 */
final class StartMojoTest {

    @Test
    void skipsExecutionWhenRequired() {
        final StartMojo mojo = new StartMojo();
        mojo.setSkip(true);
        Assertions.assertDoesNotThrow(
            mojo::execute,
            "skipped execution cannot fail"
        );
    }
}
