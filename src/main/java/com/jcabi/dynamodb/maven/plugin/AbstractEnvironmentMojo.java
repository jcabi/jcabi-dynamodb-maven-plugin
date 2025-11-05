/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.maven.plugin;

import java.io.File;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Abstract EnvironmentMOJO.
 *
 * @since 0.8
 */
@ToString
@EqualsAndHashCode(callSuper = false)
abstract class AbstractEnvironmentMojo extends AbstractDynamoMojo {

    /**
     * Location of DynamoDB Local distribution.
     * @since 0.4
     */
    @Parameter(required = true)
    private transient File dist;

    /**
     * Java home directory, where "bin/java" can be executed.
     * @since 0.3
     */
    @Parameter(required = false)
    private transient File home;

    @Override
    public void environment() throws MojoFailureException {
        if (!this.dist.exists() || !this.dist.isDirectory()) {
            throw new MojoFailureException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "DynamoDB Local distribution doesn't exist or is not a directory: %s",
                    this.dist
                )
            );
        }
        if (this.home == null) {
            this.home = new File(System.getProperty("java.home"));
        }
        if (!this.home.exists()) {
            throw new MojoFailureException(
                String.format("Java home doesn't exist: %s", this.home)
            );
        }
    }

    /**
     * Location of DynamoDB Local distribution.
     * @return File dist
     */
    protected File distdir() {
        return this.dist;
    }

    /**
     * Java home directory, where "bin/java" can be executed.
     * @return File home
     */
    protected File homedir() {
        return this.home;
    }
}
