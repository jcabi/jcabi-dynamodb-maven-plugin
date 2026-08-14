/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.maven.plugin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link CreateTablesMojo} (more detailed test is in maven
 * invoker).
 * @since 0.1
 */
final class CreateTablesMojoTest {

    @Test
    void skipsExecutionWhenRequired() {
        final CreateTablesMojo mojo = new CreateTablesMojo();
        mojo.setSkip(true);
        Assertions.assertDoesNotThrow(
            mojo::execute,
            "skipped execution cannot fail"
        );
    }
}
