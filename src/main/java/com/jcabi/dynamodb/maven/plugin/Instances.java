/**
 * Copyright (c) 2012-2013, JCabi.com
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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.VerboseProcess;
import com.jcabi.log.VerboseRunnable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.codehaus.plexus.util.FileUtils;

/**
 * Running instances of DynamoDB Local.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 * @see <a href="http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.html">DynamoDB Local</a>
 */
@ToString
@EqualsAndHashCode(of = "threads")
@Loggable(Loggable.INFO)
@SuppressWarnings("PMD.DoNotUseThreads")
final class Instances {

    /**
     * Directory where to run.
     */
    private final transient File dir;

    /**
     * Running threads.
     */
    private final transient ConcurrentMap<Integer, Thread> threads =
        new ConcurrentHashMap<Integer, Thread>(0);

    /**
     * Public ctor.
     * @param tgz Path to DynamoDBLocal.zip
     * @param temp Temp directory to unpack TGZ into
     * @throws IOException If fails
     */
    protected Instances(@NotNull final File tgz,
        @NotNull final File temp) throws IOException {
        FileUtils.deleteDirectory(temp);
        temp.mkdirs();
        new VerboseProcess(
            new ProcessBuilder().command(
                new String[] {
                    "/usr/bin/tar",
                    "xzf",
                    tgz.getAbsolutePath(),
                    "-C",
                    temp.getAbsolutePath(),
                }
            )
        ).stdout();
        this.dir = temp.listFiles()[0];
    }

    /**
     * Start a new one at this port.
     * @param port The port to start at
     */
    public void start(final int port) {
        final ProcessBuilder proc = new ProcessBuilder().command(
            new String[] {
                String.format(
                    "%s%sbin%<sjava",
                    System.getProperty("java.home"),
                    System.getProperty("file.separator")
                ),
                "-jar",
                new File(this.dir, "DynamoDBLocal.jar").getAbsolutePath(),
                "--port",
                Integer.toString(port),
            }
        ).directory(Instances.this.dir);
        final Thread thread = new Thread(
            new VerboseRunnable(
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        new VerboseProcess(proc).stdout();
                        return null;
                    }
                }
            )
        );
        thread.start();
        this.threads.put(port, thread);
    }

    /**
     * Stop a running one at this port.
     * @param port The port to stop at
     */
    public void stop(final int port) {
        final Thread thread = this.threads.get(port);
        if (thread == null) {
            throw new IllegalArgumentException(
                String.format(
                    "No DynamoDB Local instances running on port %d", port
                )
            );
        }
        thread.interrupt();
    }

}
