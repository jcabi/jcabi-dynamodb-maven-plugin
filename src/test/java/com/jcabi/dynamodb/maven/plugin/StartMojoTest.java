/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.maven.plugin;

import org.junit.jupiter.api.Test;

/**
 * Test case for {@link StartMojo} (more detailed test is in maven invoker).
 *
 * @since 0.1
 */
public final class StartMojoTest {

    @Test
    public void skipsExecutionWhenRequired() throws Exception {
        final StartMojo mojo = new StartMojo();
        mojo.setSkip(true);
        mojo.execute();
    }

}
