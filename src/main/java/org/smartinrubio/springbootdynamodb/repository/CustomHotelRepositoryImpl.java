package org.smartinrubio.springbootdynamodb.repository;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.xspec.Condition;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.smartinrubio.springbootdynamodb.exception.DuplicateTableException;
import org.smartinrubio.springbootdynamodb.exception.GenericDynamoDBException;
import org.smartinrubio.springbootdynamodb.model.Geo;
import org.smartinrubio.springbootdynamodb.utils.JsonTypeConverter;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
public class CustomHotelRepositoryImpl implements CustomHotelRepository {

    private static final String TABLE_NAME = "Hotels";

    private final DynamoDB dynamoDB;

    public CustomHotelRepositoryImpl(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    @Override
    public void createTable() {

        try {
            Table table = dynamoDB.createTable(TABLE_NAME,
                    Collections.singletonList(new KeySchemaElement("id", KeyType.HASH)),
                    Collections.singletonList(new AttributeDefinition("id", ScalarAttributeType.S)),
                    new ProvisionedThroughput(10L, 10L));
            table.waitForActive();

            log.info("Success. Table status: " + table.getDescription().getTableStatus());
        } catch (ResourceInUseException e) {
            log.error("Table already Exists: {}", e.getMessage());
            throw new DuplicateTableException(e);

        } catch (Exception e) {
            log.error("Unable to create table: {}", e.getMessage());
            throw new GenericDynamoDBException(e);
        }
    }

    @Override
    public void loadData() throws IOException {
        Table table = dynamoDB.getTable(TABLE_NAME);

        JsonParser parser = new JsonFactory().createParser(new File("hotels.json"));

        JsonNode rootNode = new ObjectMapper().readTree(parser);
        Iterator<JsonNode> iterator = rootNode.iterator();

        ObjectNode currentNode;

        while (iterator.hasNext()) {
            currentNode = (ObjectNode) iterator.next();

            String id = currentNode.path("id").asText();
            String name = currentNode.path("name").asText();
            String geo = currentNode.path("geo").toString();
            String lon = currentNode.path("geo").get("lon").asText();
            String lat = currentNode.path("geo").get("lat").asText();

            System.err.println(lat);
            try {
//                Geo geoObj = new Geo(Double.valueOf(lon), Double.valueOf(lat));
                table.putItem(new Item()
                        .withPrimaryKey("id", id)
                        .withString("name", name)
//                        .with("jsonNode", JsonTypeConverter.toJsonNode(geoObj))
                        .withString("geo", String.format("%s, %s", lat, lon)));

                log.info("PutItem succeeded " + id + " " + name);
            } catch (Exception e) {
                log.error("Unable to add hotel: {} - {}: \n{}", id, name, e.getMessage());
                break;
            }
        }
        parser.close();
    }

    // version ideas https://stackoverflow.com/questions/40186283/dynamodb-update-item-only-if-new-attribute-is-greater-than-existing-one
    @Override
    public String get(String id) {
        Table table = dynamoDB.getTable(TABLE_NAME);
        GetItemSpec spec = new GetItemSpec()
                .withPrimaryKey("id", id);
        Item item = table.getItem(spec);
        System.err.println(item.toJSONPretty());
//        item.withString("version", getVersion());
//        item.withString("version", uuid());
        return item.toJSONPretty();
    }

    @Override
    public String updateWithVersion(String json) {
        Table table = dynamoDB.getTable(TABLE_NAME);
        Item item =  Item.fromJSON(json);

        // ? what about null value in new? old?
        // not exist on new? old?
        // truth table
        // v1 stored
        //   v1 new works
        //   v2 new fails
        //   null new fails
        //   empty new fails
        // empty value stored?



        Map<String, Object> values = new HashMap<>();
        values.put(":version", item.getString("version"));
        PutItemSpec spec = new PutItemSpec().withItem(item)
                .withConditionExpression("attribute_not_exists(version) OR  version = :version")
                .withValueMap(values);
//                * meMap(new NameMap().with("#createdate", "createdate"))
//                *         .withValueMap(new ValueMap().withString(":val1", createDate))
//                *         .withConditionExpression("createdate < :val1");
//                withExpected(new Expected("version").eq(getVersion1()));

        // item.isPresent(path) - true if exists
        // item.getString(path) - value or null if either doesn't exist or value is null
//        PutItemOutcome outcome = table.putItem(item, new Expected("version").eq(item.getString("version")) );
        table.putItem(spec);
        return item.toJSONPretty();

//                table.pu
//
//        Item item = table.getItem(spec);
//        System.err.println(item.toJSONPretty());
//        item.withString("version", getVersion());
//        return item.toJSONPretty();
    }

    @Override
    public String update(String json) {
        Table table = dynamoDB.getTable(TABLE_NAME);
        Item item =  Item.fromJSON(json);
        PutItemOutcome outcome = table.putItem(item);
//        fix endpoint names. get version from json
        return item.toJSONPretty();
    }

    /*
      final PutItemSpec putItemSpec = new PutItemSpec()
      .withItem(item)
      .withExpected(
          new Expected("version_timestamp").eq(previousVersion.getTimestamp()),
          new Expected("version_counter").eq(previousVersion.getCounter())
      );

  final Supplier<PutItemOutcome> putItemOutcomeSupplier = () -> {
    try {
      return dynamoDB.getTable(featureTableName).putItem(putItemSpec);
    } catch (ConditionalCheckFailedException e) {
      logger.error("err=conflict_feature_version_mismatch feature_key={} {}", feature.getKey(),
          e.getMessage());
      throwConflictVersionMismatch(feature);
      return null;
    }
  };
     */
    private String getVersion1() {
        return "1";
    }
    private String getVersion() {
        return System.currentTimeMillis()+"_"+getRandom();
    }

    private int getRandom() {
        Random random = new Random();
        return random.nextInt(10000);
    }

    private String uuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
