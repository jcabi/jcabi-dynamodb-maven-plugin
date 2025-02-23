/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

def log = new File(basedir, 'build.log')
assert log.text.contains('#start(')
assert log.text.contains('Table \'FirstTable\' is now ready for use')
assert log.text.contains('Table \'SecondTable\' is now ready for use')
assert log.text.contains('Table \'FirstTable\' already exists, skipping...')
assert log.text.contains('Table \'ThirdTable\' is now ready for use')
assert log.text.contains('#stop(')
assert !log.text.contains('Non-zero exit code')
