/**
 * Copyright (c) 2012-2015, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.dynamodb.maven.plugin;

import java.io.File;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Starts DynamoDB Local.
 *
 * @todo #41:30min Let's avoid code duplication between this class and `StartMojo`.
 *  One idea is to create a decorator called `ThreadedMojo` that receives
 *  another mojo in its constructor and, when called, runs it inside a thread.
 *
 * @author Denis N. Antonioli (denisa@sunrun.com)
 * @version $Id$
 * @since 0.8
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo(
    threadSafe = true, name = "run"
)
public final class RunMojo extends AbstractDynamoMojo {

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
    public void run(final Instances instances) throws MojoFailureException {
        if (!this.dist.exists()) {
            throw new MojoFailureException(
                String.format(
                    "DynamoDB Local distribution doesn't exist: %s",
                    this.dist
                )
            );
        }
        if (!this.dist.isDirectory()) {
            throw new MojoFailureException(
                String.format(
                    "DynamoDB Local distribution is not a directory: %s",
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
        try {
            instances.run(this.dist, this.tcpPort(), this.home, this.args());
        } catch (final IOException ex) {
            throw new MojoFailureException(
                "failed to start DynamoDB Local", ex
            );
        }
    }

}
