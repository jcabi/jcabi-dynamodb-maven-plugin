In order to deploy a new version of DynamoDBLocal artifact to Maven Central you should do the following steps:

  1. Update `deploy/pom.xml` file with a new version of DynamoDBLocal

  2. Download latest TAR.GZ archive from [Amazon](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html)

  3. Untar and unzip it

  4. Zip it with the following command:

```
$ cd directory_with_dynamodblocal_content
$ zip -0 -r ../dynamo.zip *
```

  5. Install it locally and test how it works with the plugin:

```
$ mvn install:install-file -Dfile=dynamo.zip -DgroupId=com.jcabi -DartifactId=DynamoDBLocal -Dpackaging=zip -Dversion=2014-01-08
```

  6. Move `dynamo.zip` to this directory, and then sign both files, using `gnupg`:

```
$ gpg -ab pom.xml
$ gpg -ab dynamo.zip
```

  7. Login to `oss.sonatype.org` and deploy them both (`pom.xml` and `dynamo.zip`). You should have these files before upload (rename `pom.xml.asc` to `pom.pom.asc`, otherwise sonatype won't accept it):

```
pom.xml
pom.pom.asc
dynamo.zip
dynamo.zip.asc
```
