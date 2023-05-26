/*
 * Copyright (c) 2012-2023, jcabi.com
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
