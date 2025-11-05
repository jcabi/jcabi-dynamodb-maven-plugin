/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.maven.plugin;

import com.jcabi.dynamodb.core.Instances;
import com.jcabi.log.Logger;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Put the plugin on hold.
 *
 * @since 0.9
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo
    (
        threadSafe = true, name = "wait",
        defaultPhase = LifecyclePhase.INTEGRATION_TEST
    )
public final class WaitMojo extends AbstractEnvironmentMojo {

    @Override
    public void run(final Instances instances) {
        Logger.info(
            this, "DynamoDB Local is listening on port %d... (Ctrl-C to stop)",
            this.tcpPort()
        );
        try {
            TimeUnit.SECONDS.sleep(Long.MAX_VALUE);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

}
