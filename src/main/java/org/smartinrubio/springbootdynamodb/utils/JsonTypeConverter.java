package org.smartinrubio.springbootdynamodb.utils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.smartinrubio.springbootdynamodb.model.Geo;

import java.io.IOException;

public class JsonTypeConverter implements DynamoDBTypeConverter<String, JsonNode> {
    @Override
    public String convert(JsonNode jsonNode) {
        String str = null;
        try {
            if (jsonNode != null) {
                str = toJsonString(jsonNode);
                System.err.println(str);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    @Override
    public JsonNode unconvert(String jsonStr) {
        JsonNode jsonNode = null;
        System.err.println(jsonStr);
        try {
            if (jsonStr != null && jsonStr.length() != 0) {
                jsonNode = toJsonNode(jsonStr);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return jsonNode;
    }

    public static String toJsonString(JsonNode jsonNode) throws JsonProcessingException {
        return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    public static JsonNode toJsonNode(String jsonString) throws IOException {
        return new ObjectMapper().readTree(jsonString);
    }

    public static JsonNode toJsonNode(Object obj) {
        return new ObjectMapper().valueToTree(obj);
    }


    public static void main(String[] args) throws IOException {
        JsonTypeConverter converter = new JsonTypeConverter();
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("fname", "steve");
        jsonNode.put("lname", "souza");

        String json = "{\"fname\":\"steve\",\"lname\":\"souza\"}";
        System.out.println(converter.unconvert(json));
    }
}
