/**
 * Copyright (c) 2012-2015, jcabi.com
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

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamodb.core.Instances;
import com.jcabi.log.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Creates DynamoDB tables.
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (300 lines)
 * @checkstyle MultipleStringLiterals (300 lines)
 * @checkstyle ExcessiveImports (400 lines)
 */
@ToString
@EqualsAndHashCode(callSuper = false)
@Mojo
    (
        threadSafe = true, name = "create-tables",
        defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST
    )
@SuppressWarnings
    (
        {
            "PMD.NPathComplexity",
            "PMD.CyclomaticComplexity",
            "PMD.StdCyclomaticComplexity",
            "PMD.ModifiedCyclomaticComplexity",
            "PMD.AvoidInstantiatingObjectsInLoops"
        }
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
    public void run(final Instances instances) throws MojoFailureException {
        final AmazonDynamoDB aws = new AmazonDynamoDBClient(
            new BasicAWSCredentials(this.key, this.secret)
        );
        aws.setEndpoint(String.format("%s:%d", this.endpoint, this.tcpPort()));
        for (final String table : this.tables) {
            final JsonObject json = this.readJson(table);
            if (json.containsKey("TableName")) {
                final String name = json.getString("TableName");
                if (CreateTablesMojo.exists(aws, name)) {
                    Logger.info(
                        this, "Table '%s' already exists, skipping...", name
                    );
                } else {
                    this.createTable(aws, json);
                }
            } else {
                throw new MojoFailureException(
                    String.format(
                        "File '%s' does not specify TableName attribute", table
                    )
                );
            }
        }
    }

    /**
     * Table exists?
     * @param aws AWS
     * @param name Table name
     * @return TRUE if it exists
     */
    private static boolean exists(final AmazonDynamoDB aws, final String name) {
        boolean exists;
        try {
            TableUtils.waitUntilExists(
                aws, name, Tv.THOUSAND, Tv.HUNDRED
            );
            exists = true;
        } catch (final AmazonClientException ex) {
            exists = false;
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        return exists;
    }

    /**
     * Create DynamoDB table.
     *
     * @param aws DynamoDB client
     * @param json JSON definition of table
     * @checkstyle ExecutableStatementCount (50 lines)
     * @checkstyle NPathComplexityCheck (100 lines)
     */
    private void createTable(final AmazonDynamoDB aws,  final JsonObject json) {
        final String name = json.getString("TableName");
        final CreateTableRequest request =
            new CreateTableRequest().withTableName(name);
        if (json.containsKey("KeySchema")) {
            final Collection<KeySchemaElement> keys = this.keySchema(json);
            request.setKeySchema(keys);
        }
        if (json.containsKey("AttributeDefinitions")) {
            final Collection<AttributeDefinition> attrs =
                new LinkedList<AttributeDefinition>();
            final JsonArray schema =
                json.getJsonArray("AttributeDefinitions");
            for (final JsonObject defn : schema.getValuesAs(JsonObject.class)) {
                attrs.add(
                    new AttributeDefinition(
                        defn.getString("AttributeName"),
                        defn.getString("AttributeType")
                    )
                );
            }
            request.setAttributeDefinitions(attrs);
        }
        if (json.containsKey("ProvisionedThroughput")) {
            final JsonObject throughput =
                json.getJsonObject("ProvisionedThroughput");
            request.setProvisionedThroughput(
                new ProvisionedThroughput(
                    this.asLong(throughput, "ReadCapacityUnits"),
                    this.asLong(throughput, "WriteCapacityUnits")
                )
            );
        }
        if (json.containsKey("GlobalSecondaryIndexes")) {
            final Collection<GlobalSecondaryIndex> indexes =
                new LinkedList<GlobalSecondaryIndex>();
            final JsonArray array =
                json.getJsonArray("GlobalSecondaryIndexes");
            for (final JsonObject index : array.getValuesAs(JsonObject.class)) {
                final JsonObject throughput =
                    index.getJsonObject("ProvisionedThroughput");
                final GlobalSecondaryIndex gsi = new GlobalSecondaryIndex()
                    .withIndexName(index.getString("IndexName"))
                    .withKeySchema(this.keySchema(index))
                    .withProjection(this.projection(index))
                    .withProvisionedThroughput(
                        new ProvisionedThroughput(
                            this.asLong(throughput, "ReadCapacityUnits"),
                            this.asLong(throughput, "WriteCapacityUnits")
                        )
                    );
                indexes.add(gsi);
            }
            request.setGlobalSecondaryIndexes(indexes);
        }
        if (json.containsKey("LocalSecondaryIndexes")) {
            final Collection<LocalSecondaryIndex> indexes =
                new LinkedList<LocalSecondaryIndex>();
            final JsonArray array =
                json.getJsonArray("LocalSecondaryIndexes");
            for (final JsonObject index : array.getValuesAs(JsonObject.class)) {
                final LocalSecondaryIndex lsi = new LocalSecondaryIndex()
                    .withIndexName(index.getString("IndexName"))
                    .withKeySchema(this.keySchema(index))
                    .withProjection(this.projection(index));
                indexes.add(lsi);
            }
            request.setLocalSecondaryIndexes(indexes);
        }
        aws.createTable(request);
        Logger.info(this, "Waiting for table '%s' to become active", name);
        try {
            TableUtils.waitUntilActive(aws, name);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        Logger.info(this, "Table '%s' is now ready for use", name);
    }

    /**
     * Get json value as a long - with a backward compatibility fallback for
     * string values.
     *
     * @param json JSON input
     * @param name The element name
     * @return The element value converted as a long
     */
    private long asLong(final JsonObject json, final String name) {
        long result;
        try {
            result = json.getJsonNumber(name).longValue();
        } catch (final ClassCastException ex) {
            result = Long.parseLong(json.getString(name));
        }
        return result;
    }

    /**
     * Get projection JSON element.
     * @param json JSON input
     * @return Projection
     */
    private Projection projection(final JsonObject json) {
        final JsonObject projn = json.getJsonObject("Projection");
        final Projection projection = new Projection()
            .withProjectionType(projn.getString("ProjectionType"));
        final Collection<String> nonkeyattrs = new LinkedList<String>();
        if (projn.containsKey("NonKeyAttributes")) {
            for (final JsonValue nonkey
                : projn.getJsonArray("NonKeyAttributes")) {
                nonkeyattrs.add(nonkey.toString());
            }
            projection.setNonKeyAttributes(nonkeyattrs);
        }
        return projection;
    }

    /**
     * Get key schema elements.
     * @param json JSON input
     * @return Key schema elements
     */
    private Collection<KeySchemaElement> keySchema(final JsonObject json) {
        final Collection<KeySchemaElement> keys =
            new LinkedList<KeySchemaElement>();
        final JsonArray schema = json.getJsonArray("KeySchema");
        for (final JsonValue value : schema) {
            final JsonObject element = (JsonObject) value;
            keys.add(
                new KeySchemaElement(
                    element.getString("AttributeName"),
                    element.getString("KeyType")
                )
            );
        }
        return keys;
    }

    /**
     * Reads a file's contents into a JsonObject.
     * @param file The path of the file to read
     * @return The JSON object
     * @throws MojoFailureException If there is an execution failure.
     */
    private JsonObject readJson(final String file)
        throws MojoFailureException {
        InputStream stream = null;
        JsonObject json = null;
        try {
            stream = new FileInputStream(file);
            json = Json.createReader(stream).readObject();
        } catch (final IOException ex) {
            throw new MojoFailureException(
                "Failed to read table definition", ex
            );
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (final IOException ex) {
                    Logger.error(
                        this,
                        "Failed to close stream with message %s",
                        ex.getMessage()
                    );
                }
            }
        }
        return json;
    }

}
