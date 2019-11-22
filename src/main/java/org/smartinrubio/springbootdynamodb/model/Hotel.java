package org.smartinrubio.springbootdynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.smartinrubio.springbootdynamodb.utils.GeoTypeConverter;
import org.smartinrubio.springbootdynamodb.utils.JsonTypeConverter;

@Data
@DynamoDBTable(tableName = "Hotels")
public class Hotel {

    @DynamoDBHashKey
    @DynamoDBGeneratedUuid(DynamoDBAutoGenerateStrategy.CREATE) // Requires a mutable object
    private String id;

    @DynamoDBAttribute
    private String name;

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = GeoTypeConverter.class)
    private Geo geo;

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = JsonTypeConverter.class)
    private JsonNode jsonNode;


}
