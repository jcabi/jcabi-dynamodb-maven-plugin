/**
 * Copyright (c) 2012-2014, jcabi.com
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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * Running instances of DynamoDB Local.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @see <a href="http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.html">DynamoDB Local</a>
 */
@ToString
@EqualsAndHashCode(of = "processes")
@Loggable(Loggable.INFO)
@SuppressWarnings("PMD.DoNotUseThreads")
final class Instances {

    /**
     * Running processes.
     */
    private final transient ConcurrentMap<Integer, Process> processes =
        new ConcurrentHashMap<Integer, Process>(0);

    /**
     * Public ctor.
     */
    Instances() {
        Runtime.getRuntime().addShutdownHook(
            new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        Instances.this.shutdown();
                    }
                }
            )
        );
    }

    /**
     * Start a new one at this port.
     * @param dist Path to DynamoDBLocal distribution
     * @param port The port to start at
     * @param home Java home directory
     * @param args Command line arguments
     * @throws IOException If fails to start
     * @checkstyle ParameterNumber (5 lines)
     */
    public void start(@NotNull final File dist, final int port, final File home,
        @NotNull final List<String> args)
        throws IOException {
        final Process process = Instances.process(dist, port, home, args);
        final Thread thread = new Thread(
            new VerboseRunnable(new InstanceProcess(process))
        );
        thread.setDaemon(true);
        thread.start();
        this.processes.put(port, process);
    }

    /**
     * Stop a running one at this port.
     * @param port The port to stop at
     */
    public void stop(final int port) {
        synchronized (this.processes) {
            final Process process = this.processes.get(port);
            if (process == null) {
                throw new IllegalArgumentException(
                    String.format(
                        "No DynamoDB Local instances running on port %d", port
                    )
                );
            }
            process.destroy();
            this.processes.remove(port);
        }
    }

    /**
     * Shutdown everything that is still running.
     */
    private void shutdown() {
        for (final int port : this.processes.keySet()) {
            this.stop(port);
        }
    }

    /**
     * Create new process.
     * @param dist Path to DynamoDBLocal distribution
     * @param port The port to start at
     * @param home Java home directory
     * @param args Extra command line args
     * @return Process ready to be started
     * @throws IOException If fails to start
     * @checkstyle ParameterNumber (5 lines)
     */
    private static Process process(final File dist, final int port,
        final File home, final List<String> args) throws IOException {
        return new ProcessBuilder().command(
            new String[] {
                new File(home, "bin/java").getAbsolutePath(),
                new StringBuilder("-Djava.library.path=")
                    .append(dist)
                    .append(System.getProperty("path.separator"))
                    .append(new File(dist, "DynamoDBLocal_lib"))
                    .toString(),
                "-jar",
                "DynamoDBLocal.jar",
                "--port",
                Integer.toString(port),
                StringUtils.join(args, " "),
            }
        ).directory(dist).redirectErrorStream(true).start();
    }

    /**
     * Instance process of each local DynamoDB.
     */
    private static final class InstanceProcess implements Callable<Void> {
        /**
         * Process.
         */
        private final transient Process prc;
        /**
         * Constructor.
         * @param process The process to work with.
         */
        InstanceProcess(final Process process) {
            this.prc = process;
        }
        @Override
        public Void call() throws Exception {
            new VerboseProcess(this.prc).stdoutQuietly();
            return null;
        }
    }
}
