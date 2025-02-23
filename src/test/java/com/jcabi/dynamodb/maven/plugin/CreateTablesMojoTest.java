/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.maven.plugin;

import org.junit.jupiter.api.Test;

/**
 * Test case for {@link CreateTablesMojo} (more detailed test is in maven
 * invoker).
 *
 * @since 0.1
 */
public final class CreateTablesMojoTest {

    @Test
    public void skipsExecutionWhenRequired() throws Exception {
        final CreateTablesMojo mojo = new CreateTablesMojo();
        mojo.setSkip(true);
        mojo.execute();
    }
}
