/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.core;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * Handles DynamoDB locations.
 * @since 0.8
 */
@SuppressWarnings
    (
        {
            "PMD.NPathComplexity",
            "PMD.CyclomaticComplexity",
            "PMD.StdCyclomaticComplexity",
            "PMD.ModifiedCyclomaticComplexity"
        }
    )
public final class Tables {

    /**
     * Location of the tables to be created.
     */
    private final transient Collection<String> locations;

    /**
     * AWS endpoint.
     */
    private final transient String endpoint;

    /**
     * Port to connect.
     */
    private final transient Integer port;

    /**
     * AWS key.
     */
    private final transient String key;

    /**
     * AWS secret.
     */
    private final transient String secret;

    /**
     * Public ctor.
     * @param locations The location of the tables to be created, in JSON format
     * @param endpoint AWS endpoint
     * @param port Tcp port
     * @param key AWS key
     * @param secret AWS secret
     * @checkstyle ParameterNumberCheck (3 lines)
     */
    public Tables(final Collection<String> locations, final String endpoint,
        final Integer port, final String key, final String secret) {
        this.locations = locations;
        this.endpoint = endpoint;
        this.port = port;
        this.key = key;
        this.secret = secret;
    }

    /**
     * Creates tables.
     * @throws IOException if something goes wrong
     */
    public void create() throws IOException {
        final AmazonDynamoDB aws = AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    String.format("%s:%d", this.endpoint, this.port),
                    Regions.US_EAST_1.getName()
                )
            )
            .withCredentials(
                new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(
                        this.key, this.secret
                    )
                )
            )
            .build();
        for (final String table : this.locations) {
            final JsonObject json = this.readJson(table);
            if (json.containsKey("TableName")) {
                final String name = json.getString("TableName");
                if (Tables.exists(aws, name)) {
                    Logger.info(
                        this, "Table '%s' already exists, skipping...", name
                    );
                } else {
                    this.createTable(aws, json);
                }
            } else {
                throw new IOException(
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
        Logger.info(Tables.class, "Waiting for the table '%s' in DynamoDB...", name);
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
        Logger.info(Tables.class, "The existence of the table '%s': %s", name, exists);
        return exists;
    }

    /**
     * Create DynamoDB table.
     * @param aws DynamoDB client
     * @param json JSON definition of table
     * @checkstyle ExecutableStatementCount (50 lines)
     * @checkstyle NPathComplexityCheck (100 lines)
     */
    private void createTable(final AmazonDynamoDB aws, final JsonObject json) {
        final String name = json.getString("TableName");
        final CreateTableRequest request =
            new CreateTableRequest().withTableName(name);
        if (json.containsKey("KeySchema")) {
            final Collection<KeySchemaElement> keys = Tables.keySchema(json);
            request.setKeySchema(keys);
        }
        if (json.containsKey("AttributeDefinitions")) {
            final Collection<AttributeDefinition> attrs =
                new LinkedList<>();
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
                    Tables.asLong(throughput, "ReadCapacityUnits"),
                    Tables.asLong(throughput, "WriteCapacityUnits")
                )
            );
        }
        if (json.containsKey("GlobalSecondaryIndexes")) {
            final Collection<GlobalSecondaryIndex> indexes =
                new LinkedList<>();
            final JsonArray array =
                json.getJsonArray("GlobalSecondaryIndexes");
            for (final JsonObject index : array.getValuesAs(JsonObject.class)) {
                final JsonObject throughput =
                    index.getJsonObject("ProvisionedThroughput");
                final GlobalSecondaryIndex gsi = new GlobalSecondaryIndex()
                    .withIndexName(index.getString("IndexName"))
                    .withKeySchema(Tables.keySchema(index))
                    .withProjection(Tables.projection(index))
                    .withProvisionedThroughput(
                        new ProvisionedThroughput(
                            Tables.asLong(throughput, "ReadCapacityUnits"),
                            Tables.asLong(throughput, "WriteCapacityUnits")
                        )
                    );
                indexes.add(gsi);
            }
            request.setGlobalSecondaryIndexes(indexes);
        }
        if (json.containsKey("LocalSecondaryIndexes")) {
            final Collection<LocalSecondaryIndex> indexes =
                new LinkedList<>();
            final JsonArray array =
                json.getJsonArray("LocalSecondaryIndexes");
            for (final JsonObject index : array.getValuesAs(JsonObject.class)) {
                final LocalSecondaryIndex lsi = new LocalSecondaryIndex()
                    .withIndexName(index.getString("IndexName"))
                    .withKeySchema(Tables.keySchema(index))
                    .withProjection(Tables.projection(index));
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
     * @param json JSON input
     * @param name The element name
     * @return The element value converted as a long
     */
    private static long asLong(final JsonObject json, final String name) {
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
    private static Projection projection(final JsonObject json) {
        final JsonObject projn = json.getJsonObject("Projection");
        final Projection projection = new Projection()
            .withProjectionType(projn.getString("ProjectionType"));
        if (projn.containsKey("NonKeyAttributes")) {
            final Collection<String> attrs = new LinkedList<>();
            for (final JsonValue attr
                : projn.getJsonArray("NonKeyAttributes")) {
                final JsonString name = (JsonString) attr;
                attrs.add(name.getString());
            }
            projection.setNonKeyAttributes(attrs);
        }
        return projection;
    }

    /**
     * Get key schema elements.
     * @param json JSON input
     * @return Key schema elements
     */
    private static Collection<KeySchemaElement> keySchema(final JsonObject json) {
        final Collection<KeySchemaElement> keys =
            new LinkedList<>();
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
     * @throws IOException If there is an execution failure.
     */
    private JsonObject readJson(final String file) throws IOException {
        InputStream stream = null;
        final JsonObject json;
        try {
            stream = Files.newInputStream(Paths.get(file));
            json = Json.createReader(stream).readObject();
        } catch (final FileNotFoundException ex) {
            throw new IOException("Failed to read table definition", ex);
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
