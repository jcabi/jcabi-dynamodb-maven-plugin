/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.core;

import com.jcabi.log.VerboseProcess;
import java.util.concurrent.Callable;

/**
 * Instance process of each local DynamoDB.
 * @since 0.1
 */
final class InstanceProcess implements Callable<Void> {

    /**
     * Process.
     */
    private final transient Process prc;

    /**
     * Constructor.
     * @param process The process to work with
     */
    InstanceProcess(final Process process) {
        this.prc = process;
    }

    @Override
    public Void call() {
        new VerboseProcess(this.prc).stdoutQuietly();
        return null;
    }
}
