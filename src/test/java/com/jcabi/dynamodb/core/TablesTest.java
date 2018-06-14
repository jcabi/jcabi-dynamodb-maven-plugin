package com.jcabi.dynamodb.core;

import com.amazonaws.services.dynamodbv2.model.Projection;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class TablesTest {

    private Tables tables;

    @Before
    public void setup() {
        tables = new Tables(ImmutableList.<String>of(), "http://localhost:8080", 8080, "key", "secret");
    }

    @Test
    public void testProjection() {
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("Projection", Json.createObjectBuilder()
                        .add("ProjectionType", "INCLUDE")
                        .add("NonKeyAttributes", Json.createArrayBuilder()
                                .add("a")
                                .add("b")
                                .add("c")))
                .build();
        Projection projection = tables.projection(jsonObject);
        assertThat(projection.getNonKeyAttributes(), contains("a", "b", "c"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProjection_WrongType() {
        JsonObject jsonObject = Json.createObjectBuilder()
                .add("Projection", Json.createObjectBuilder()
                        .add("ProjectionType", "INCLUDE")
                        .add("NonKeyAttributes", Json.createArrayBuilder()
                                .add(1)
                                .add('2')
                                .add("THREE")))
                .build();
        tables.projection(jsonObject);
    }

}
