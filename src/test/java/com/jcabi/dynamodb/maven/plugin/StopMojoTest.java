/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.maven.plugin;

import org.junit.jupiter.api.Test;

/**
 * Test case for {@link StopMojo} (more detailed test is in maven invoker).
 *
 * @since 0.1
 */
public final class StopMojoTest {

    @Test
    public void skipsExecutionWhenRequired() throws Exception {
        final StopMojo mojo = new StopMojo();
        mojo.setSkip(true);
        mojo.execute();
    }

}
