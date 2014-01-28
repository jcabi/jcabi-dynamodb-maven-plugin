/**
 * Copyright (c) 2012-2013, JCabi.com All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2) Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3) Neither the name of the
 * jcabi.com nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.dynamodb.maven.plugin;

import com.jcabi.log.Logger;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Abstract MOJO.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
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
    @Parameter(
            defaultValue = "false",
            required = false
    )
    private transient boolean skip;

    /**
     * Port to use.
     */
    @Parameter(
            defaultValue = "10101",
            required = false
    )
    private transient int port;

    /**
     * java.library.path to use.
     */
    @Parameter(
            defaultValue = "",
            required = false
    )
    private transient String libraryPath;

    /**
     * dbPath to use.
     */
    @Parameter(
            defaultValue = "",
            required = false
    )
    private transient String dbPath;

    /**
     * dbPath to use.
     */
    @Parameter(
            defaultValue = "false",
            required = false
    )
    private transient boolean inMemory;

    /**
     * Set skip option.
     *
     * @param skp Shall we skip execution?
     */
    public void setSkip(final boolean skp) {
        this.skip = skp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoFailureException {
        StaticLoggerBinder.getSingleton().setMavenLog(this.getLog());
        if (this.skip) {
            Logger.info(this, "execution skipped because of 'skip' option");
            return;
        }
        this.run(AbstractDynamoMojo.INSTANCES);
    }

    /**
     * Get TCP port we're on.
     *
     * @return Port number
     */
    public int tcpPort() {
        return this.port;
    }

    /**
     * Get java.library.path
     *
     * @return java.library.path
     */
    public String libraryPath() {
        return this.libraryPath;
    }

    /**
     * Get dbPath
     *
     * @return dbPath
     */
    public String dbPath() {
        return this.dbPath;
    }

    /**
     * Get java.library.path
     *
     * @return inMemory
     */
    public boolean inMemory() {
        return this.inMemory;
    }

    /**
     * Run custom functionality.
     *
     * @param instances Instances to work with
     * @throws MojoFailureException If fails
     */
    protected abstract void run(final Instances instances)
            throws MojoFailureException;

}
