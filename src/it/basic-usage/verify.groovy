/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

def log = new File(basedir, 'build.log')
assert log.text.contains('#start(')
assert log.text.contains('#stop(')
assert !log.text.contains('Non-zero exit code')
