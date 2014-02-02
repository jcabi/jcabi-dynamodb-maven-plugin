In order to deploy a new version of DynamoDBLocal artifact
to Maven Central you should do the following steps:

 1. Update `deploy/pom.xml` file with a new version of DynamoDBLocal

 2. Download latest TAR.GZ archive from [Amazon](http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html)

 3. Untar and unzip it

 4. Zip it with the following command:

```
$ cd directory_with_dynamodblocal_content
$ zip -0 -r ../dynamo.zip *
```

 5. Install it locally and test how it works with the plugin

```
$ mvn install:install-file -Dfile=dynamo.zip -DgroupId=com.jcabi \
    -DartifactId=DynamoDBLocal -Dversion=2014-01-08 -Dpackaging=zip
```

 6. Sign them both, using gnupg:

 ```
 $ gpg -ab pom.xml
 $ gpg -ab dynamo.zip
 ```

 5. Login to sonatype and deploy them both (pom.xml and dynamo.zip). You
 should have these files before upload:

 ```
 pom.xml
 pom.pom.asc
 dynamo.zip
 dynamo.zip.asc
 ```
