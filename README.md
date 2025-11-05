# [![logo](https://www.jcabi.com/logo-square.svg)](https://www.jcabi.com/logo-square.svg)

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](https://www.rultor.com/b/jcabi/jcabi-dynamodb-maven-plugin)](https://www.rultor.com/p/jcabi/jcabi-dynamodb-maven-plugin)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/jcabi/jcabi-dynamodb-maven-plugin/actions/workflows/mvn.yml/badge.svg)](https://github.com/jcabi/jcabi-dynamodb-maven-plugin/actions/workflows/mvn.yml)
[![PDD status](https://www.0pdd.com/svg?name=jcabi/jcabi-dynamodb-maven-plugin)](https://www.0pdd.com/p?name=jcabi/jcabi-dynamodb-maven-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-dynamodb-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-dynamodb-maven-plugin)
[![Javadoc](https://javadoc.io/badge/com.jcabi/jcabi-dynamodb-maven-plugin.svg)](https://www.javadoc.io/doc/com.jcabi/jcabi-dynamodb-maven-plugin)
[![codecov](https://codecov.io/gh/jcabi/jcabi-dynamodb-maven-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/jcabi/jcabi-dynamodb-maven-plugin)

More details are here: [dynamodb.jcabi.com](http://dynamodb.jcabi.com/index.html).

Also, read this blog post: [DynamoDB Local Maven Plugin](http://www.yegor256.com/2014/05/01/dynamodb-local-maven-plugin.html).

Currently, the [supported versions](http://repo1.maven.org/maven2/com/jcabi/DynamoDBLocal/)
of [DynamoDB Local](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html):

* `2023-05-26` (works with M1)
* [`2015-07-16`](https://repo1.maven.org/maven2/com/jcabi/DynamoDBLocal/2015-07-16/)
* [`2014-10-07`](https://repo1.maven.org/maven2/com/jcabi/DynamoDBLocal/2015-04-27/)
* [`2014-04-24`](https://repo1.maven.org/maven2/com/jcabi/DynamoDBLocal/2014-04-24/)
* [`2014-01-08`](https://repo1.maven.org/maven2/com/jcabi/DynamoDBLocal/2014-01-08/)
* [`2013-09-12`](https://repo1.maven.org/maven2/com/jcabi/DynamoDBLocal/2013-09-12/)

See how [Jare](https://github.com/yegor256/jare) project is using this plugin.

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```bash
mvn clean install -Pqulice
```
