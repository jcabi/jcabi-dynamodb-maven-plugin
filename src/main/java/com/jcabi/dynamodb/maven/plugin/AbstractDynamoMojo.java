/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.maven.plugin;

import com.jcabi.dynamodb.core.Instances;
import com.jcabi.log.Logger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Abstract DynamoMOJO.
 *
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(callSuper = false)
abstract class AbstractDynamoMojo extends AbstractMojo {

    /**
     * All instances.
     */
    private static final Instances INSTANCES = new Instances();

    /**
     * Shall we skip execution?
     */
    @Parameter
        (
            defaultValue = "false"
        )
    private transient boolean skip;

    /**
     * Command line arguments of DynamoDBLocal.
     * @since 0.5
     */
    @Parameter
    private transient List<String> arguments;

    /**
     * Port to use.
     */
    @Parameter
        (
            defaultValue = "10101"
        )
    private transient int port;

    /**
     * Ctor.
     */
    protected AbstractDynamoMojo() {
        // nothing here
    }

    /**
     * Set skip option.
     * @param skp Shall we skip execution?
     */
    public final void setSkip(final boolean skp) {
        this.skip = skp;
    }

    @Override
    public final void execute() throws MojoFailureException {
        StaticLoggerBinder.getSingleton().setMavenLog(this.getLog());
        if (this.skip) {
            Logger.info(this, "execution skipped because of 'skip' option");
            return;
        }
        this.environment();
        this.run(AbstractDynamoMojo.INSTANCES);
    }

    /**
     * Get TCP port we're on.
     * @return Port number
     */
    protected final int tcpPort() {
        return this.port;
    }

    /**
     * Command line arguments.
     * @return List of arguments
     */
    protected final List<String> args() {
        final List<String> args = new LinkedList<>();
        if (this.arguments != null) {
            args.addAll(this.arguments);
        }
        return Collections.unmodifiableList(args);
    }

    /**
     * Set the project environment.
     * {@link AbstractEnvironmentMojo}.
     * @throws MojoFailureException If fails
     */
    protected abstract void environment() throws MojoFailureException;

    /**
     * Run custom functionality.
     * @param instances Instances to work with
     * @throws MojoFailureException If fails
     */
    protected abstract void run(Instances instances)
        throws MojoFailureException;
}
