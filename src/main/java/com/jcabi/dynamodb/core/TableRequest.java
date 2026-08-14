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
import java.util.Collection;
import java.util.LinkedList;
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
                TableRequest.throughput(
                    this.json.getJsonObject("ProvisionedThroughput")
                )
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

    /**
     * Get attribute definitions.
     * @param json JSON input
     * @return Attribute definitions
     */
    private static Collection<AttributeDefinition> attributes(
        final JsonObject json) {
        final Collection<AttributeDefinition> attrs = new LinkedList<>();
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

    /**
     * Get provisioned throughput.
     * @param json JSON input
     * @return Provisioned throughput
     */
    private static ProvisionedThroughput throughput(final JsonObject json) {
        return new ProvisionedThroughput(
            TableRequest.asLong(json, "ReadCapacityUnits"),
            TableRequest.asLong(json, "WriteCapacityUnits")
        );
    }

    /**
     * Get global secondary indexes.
     * @param json JSON input
     * @return Global secondary indexes
     */
    private static Collection<GlobalSecondaryIndex> globals(
        final JsonObject json) {
        final Collection<GlobalSecondaryIndex> indexes = new LinkedList<>();
        final JsonArray array = json.getJsonArray("GlobalSecondaryIndexes");
        for (final JsonObject index : array.getValuesAs(JsonObject.class)) {
            indexes.add(
                new GlobalSecondaryIndex()
                    .withIndexName(index.getString("IndexName"))
                    .withKeySchema(TableRequest.keySchema(index))
                    .withProjection(TableRequest.projection(index))
                    .withProvisionedThroughput(
                        TableRequest.throughput(
                            index.getJsonObject("ProvisionedThroughput")
                        )
                    )
            );
        }
        return indexes;
    }

    /**
     * Get local secondary indexes.
     * @param json JSON input
     * @return Local secondary indexes
     */
    private static Collection<LocalSecondaryIndex> locals(
        final JsonObject json) {
        final Collection<LocalSecondaryIndex> indexes = new LinkedList<>();
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
            final JsonArray array = projn.getJsonArray("NonKeyAttributes");
            for (final JsonString attr : array.getValuesAs(JsonString.class)) {
                attrs.add(attr.getString());
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
    private static Collection<KeySchemaElement> keySchema(
        final JsonObject json) {
        final Collection<KeySchemaElement> keys = new LinkedList<>();
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
