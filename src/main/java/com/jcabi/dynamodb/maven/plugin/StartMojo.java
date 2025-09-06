/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.maven.plugin;

import com.jcabi.dynamodb.core.Instances;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Starts DynamoDB Local.
 *
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo
    (
        threadSafe = true, name = "start",
        defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST
    )
public final class StartMojo extends AbstractEnviromentMojo {

    @Override
    public void run(final Instances instances) throws MojoFailureException {
        try {
            instances.start(
                this.distdir(), this.tcpPort(), this.homedir(), this.args()
            );
        } catch (final IOException ex) {
            throw new MojoFailureException(
                "failed to start DynamoDB Local", ex
            );
        }
    }

}
