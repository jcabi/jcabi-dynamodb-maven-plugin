/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.maven.plugin;

import com.jcabi.dynamodb.core.Instances;
import com.jcabi.dynamodb.core.Tables;
import java.io.IOException;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Creates DynamoDB tables.
 *
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo
    (
        threadSafe = true, name = "create-tables",
        defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST
    )
public final class CreateTablesMojo extends AbstractDynamoMojo {

    /**
     * The location of the tables to be created, in JSON format.
     */
    @Parameter(required = false)
    private transient Collection<String> tables;

    /**
     * AWS endpoint, use localhost if not specified.
     */
    @Parameter(required = false, defaultValue = "http://localhost")
    private transient String endpoint;

    /**
     * AWS key.
     */
    @Parameter(required = false, defaultValue = "AWS-Key")
    private transient String key;

    /**
     * AWS secret.
     */
    @Parameter(required = false, defaultValue = "AWS-Secret")
    private transient String secret;

    @Override
    public void environment() {
        // nothing here
    }

    @Override
    public void run(final Instances instances) throws MojoFailureException {
        try {
            new Tables(
                this.tables, this.endpoint, this.tcpPort(), this.key,
                this.secret
            )
                .create();
        } catch (final IOException ex) {
            throw new MojoFailureException(
                String.format(
                    "Failed to create tables for instances: %s", instances
                ),
                ex
            );
        }
    }
}
