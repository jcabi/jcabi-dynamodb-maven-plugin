/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.dynamodb.core;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import java.util.ArrayList;
import java.util.Collection;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

/**
 * JSON definition of a table, as a DynamoDB creation request.
 * @since 1.0
 */
final class TableRequest {

    /**
     * JSON definition of the table.
     */
    private final transient JsonObject json;

    /**
     * Public ctor.
     * @param definition JSON definition of the table
     */
    TableRequest(final JsonObject definition) {
        this.json = definition;
    }

    /**
     * Make the request that creates the table.
     * @return The request
     */
    CreateTableRequest request() {
        final CreateTableRequest request = new CreateTableRequest()
            .withTableName(this.json.getString("TableName"));
        if (this.json.containsKey("KeySchema")) {
            request.setKeySchema(TableRequest.keySchema(this.json));
        }
        if (this.json.containsKey("AttributeDefinitions")) {
            request.setAttributeDefinitions(TableRequest.attributes(this.json));
        }
        if (this.json.containsKey("ProvisionedThroughput")) {
            request.setProvisionedThroughput(
                TableRequest.throughput(this.json)
            );
        }
        if (this.json.containsKey("GlobalSecondaryIndexes")) {
            request.setGlobalSecondaryIndexes(TableRequest.globals(this.json));
        }
        if (this.json.containsKey("LocalSecondaryIndexes")) {
            request.setLocalSecondaryIndexes(TableRequest.locals(this.json));
        }
        return request;
    }

    private static Collection<AttributeDefinition> attributes(
        final JsonObject json) {
        final Collection<AttributeDefinition> attrs = new ArrayList<>(0);
        final JsonArray schema = json.getJsonArray("AttributeDefinitions");
        for (final JsonObject defn : schema.getValuesAs(JsonObject.class)) {
            attrs.add(
                new AttributeDefinition(
                    defn.getString("AttributeName"),
                    defn.getString("AttributeType")
                )
            );
        }
        return attrs;
    }

    private static ProvisionedThroughput throughput(final JsonObject json) {
        final JsonObject provisioned = json.getJsonObject(
            "ProvisionedThroughput"
        );
        return new ProvisionedThroughput(
            TableRequest.asLong(provisioned, "ReadCapacityUnits"),
            TableRequest.asLong(provisioned, "WriteCapacityUnits")
        );
    }

    private static Collection<GlobalSecondaryIndex> globals(
        final JsonObject json) {
        final Collection<GlobalSecondaryIndex> indexes = new ArrayList<>(0);
        final JsonArray array = json.getJsonArray("GlobalSecondaryIndexes");
        for (final JsonObject index : array.getValuesAs(JsonObject.class)) {
            indexes.add(
                new GlobalSecondaryIndex()
                    .withIndexName(index.getString("IndexName"))
                    .withKeySchema(TableRequest.keySchema(index))
                    .withProjection(TableRequest.projection(index))
                    .withProvisionedThroughput(TableRequest.throughput(index))
            );
        }
        return indexes;
    }

    private static Collection<LocalSecondaryIndex> locals(
        final JsonObject json) {
        final Collection<LocalSecondaryIndex> indexes = new ArrayList<>(0);
        final JsonArray array = json.getJsonArray("LocalSecondaryIndexes");
        for (final JsonObject index : array.getValuesAs(JsonObject.class)) {
            indexes.add(
                new LocalSecondaryIndex()
                    .withIndexName(index.getString("IndexName"))
                    .withKeySchema(TableRequest.keySchema(index))
                    .withProjection(TableRequest.projection(index))
            );
        }
        return indexes;
    }

    private static long asLong(final JsonObject json, final String name) {
        long result;
        try {
            result = json.getJsonNumber(name).longValue();
        } catch (final ClassCastException ex) {
            result = Long.parseLong(json.getString(name));
        }
        return result;
    }

    private static Projection projection(final JsonObject json) {
        final JsonObject projn = json.getJsonObject("Projection");
        final Projection projection = new Projection()
            .withProjectionType(projn.getString("ProjectionType"));
        if (projn.containsKey("NonKeyAttributes")) {
            final Collection<String> attrs = new ArrayList<>(0);
            final JsonArray array = projn.getJsonArray("NonKeyAttributes");
            for (final JsonString attr : array.getValuesAs(JsonString.class)) {
                attrs.add(attr.getString());
            }
            projection.setNonKeyAttributes(attrs);
        }
        return projection;
    }

    private static Collection<KeySchemaElement> keySchema(
        final JsonObject json) {
        final Collection<KeySchemaElement> keys = new ArrayList<>(0);
        final JsonArray schema = json.getJsonArray("KeySchema");
        for (final JsonObject element : schema.getValuesAs(JsonObject.class)) {
            keys.add(
                new KeySchemaElement(
                    element.getString("AttributeName"),
                    element.getString("KeyType")
                )
            );
        }
        return keys;
    }
}
