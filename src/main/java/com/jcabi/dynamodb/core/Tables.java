/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
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
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.jcabi.log.Logger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * Handles DynamoDB locations.
 * @since 0.8
 */
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
        final AmazonDynamoDB aws = AmazonDynamoDBClientBuilder
            .standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    String.format("%s:%d", this.endpoint, this.port),
                    Regions.US_EAST_1.getName()
                )
            ).withCredentials(
                new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(
                        this.key, this.secret
                    )
                )
            )
            .build();
        for (final String table : this.locations) {
            final JsonObject json = readJson(table);
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

    private static boolean exists(final AmazonDynamoDB aws, final String name) {
        Logger.info(Tables.class, "Waiting for the table '%s' in DynamoDB...", name);
        boolean exists;
        try {
            TableUtils.waitUntilExists(
                aws, name, 1000, 100
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

    private void createTable(final AmazonDynamoDB aws, final JsonObject json) {
        final String name = json.getString("TableName");
        aws.createTable(new TableRequest(json).request());
        Logger.info(this, "Waiting for table '%s' to become active", name);
        try {
            TableUtils.waitUntilActive(aws, name);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
        Logger.info(this, "Table '%s' is now ready for use", name);
    }

    private static JsonObject readJson(final String file) throws IOException {
        final JsonObject json;
        try (
            JsonReader reader = Json.createReader(
                Files.newInputStream(Paths.get(file))
            )
        ) {
            json = reader.readObject();
        } catch (final FileNotFoundException ex) {
            throw new IOException("Failed to read table definition", ex);
        }
        return json;
    }
}
