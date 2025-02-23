/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.maven.plugin;

import com.jcabi.dynamodb.core.Instances;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Stops DynamoDB Local.
 *
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo
    (
        threadSafe = true, name = "stop",
        defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST
    )
public final class StopMojo extends AbstractDynamoMojo {

    @Override
    public void environment() {
        // nothing here
    }

    @Override
    public void run(final Instances instances) {
        instances.stop(this.tcpPort());
    }
}
