package org.smartinrubio.springbootdynamodb.controller;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.smartinrubio.springbootdynamodb.exception.HotelNotFoundException;
import org.smartinrubio.springbootdynamodb.model.Geo;
import org.smartinrubio.springbootdynamodb.model.Hotel;
import org.smartinrubio.springbootdynamodb.repository.HotelRepository;
import org.smartinrubio.springbootdynamodb.utils.JsonTypeConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/hotels")
public class HotelController {

    /**
     * code from git clone https://github.com/smartinrub/spring-boot-dynamodb.git
     *      though i had to change it to get it to work.
     * https://github.com/smartinrub/spring-boot-dynamodb
     * article:  https://dzone.com/articles/getting-started-with-dynamodb-and-spring
     * docker dynamodb: docker run -p 8000:8000 amazon/dynamodb-local
     *  swagger doesn't seem to work - http://localhost:8080/swagger-ui.htm
     *
     * create table and populate table with the table and data links.  after that
     * others should work.
     *  http://localhost:8080/hotels/table
     *  http://localhost:8080/hotels/data
     *  Then other links should work.
     *
     *  Note i have a postman spring_dynamodb collection, or for get's you can simply
     *  use a browser
     */

    private final HotelRepository repository;

    public HotelController(HotelRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/table")
    public ResponseEntity createTable() {
        repository.createTable();
        return ResponseEntity.ok("Table Created!");
    }

    @GetMapping("/data")
    public ResponseEntity loadData() throws IOException {
        repository.loadData();
        return ResponseEntity.ok("Data Loaded");
    }

    @GetMapping
    public Iterable<Hotel> readAll() {
        return repository.findAll();
//        return Stream.generate(() -> repository.findAll().iterator().next()).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<Hotel> createHotel(@RequestBody Hotel hotel, UriComponentsBuilder uriComponentsBuilder) {
        Hotel savedHotel = repository.save(hotel);
        HttpHeaders headers = new HttpHeaders();
        URI locationUri = uriComponentsBuilder
                .path("/hotels/")
                .path(String.valueOf(savedHotel.getId()))
                .build()
                .toUri();
        headers.setLocation(locationUri);

        return new ResponseEntity<>(savedHotel, headers, HttpStatus.CREATED);
    }

    @PostMapping("/alt")
    public String alternativeCreateHotel(@RequestBody String hotel, UriComponentsBuilder uriComponentsBuilder) throws IOException {
        System.err.println(hotel);
        JsonNode hotelNode = JsonTypeConverter.toJsonNode(hotel);
        ObjectMapper mapper = new ObjectMapper();
         Hotel hotelToSave = mapper.readValue(hotel, Hotel.class);
         Hotel savedHotel = repository.save(hotelToSave);

        savedHotel.setGeo(new Geo(101.0, 102.0));
        String json = "{\"fname\":\"steve\",\"lname\":\"souza\"}";
        savedHotel.setJsonNode(JsonTypeConverter.toJsonNode(json));
//        Hotel savedHotel = repository.save(hotel);
        HttpHeaders headers = new HttpHeaders();
//        URI locationUri = uriComponentsBuilder
//                .path("/hotels/")
//                .path(String.valueOf(savedHotel.getId()))
//                .build()
//                .toUri();
//        headers.setLocation(locationUri);
        return JsonTypeConverter.toJsonNode(savedHotel).toString();

      //  return new ResponseEntity<>(savedHotel, headers, HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    public Hotel readHotelById(@PathVariable("hotelId") String id) {
        return repository.findById(id).orElseThrow(HotelNotFoundException::new);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Hotel updateHotel(@RequestBody Hotel hotel) {

        repository.findById(hotel.getId()).orElseThrow(HotelNotFoundException::new);

        return repository.save(hotel);
    }

    @GetMapping("/readByName")
    public List<Hotel> readHotelByName(@RequestParam("hotelName") String name) {
        return repository.findAllByName(name);
    }

    @DeleteMapping("/{hotelId}")
    public void deleteHotel(@PathVariable("hotelId") String id) {
        repository.deleteById(id);
    }

    @GetMapping("/count")
    public long count() {
        return repository.count();
    }

    @GetMapping("/deleteAll")
    public String deleteAll() {
         repository.deleteAll();
         return "all Items deleted!";
    }

    @GetMapping("/get2/{hotelId}")
    public ResponseEntity get2(@PathVariable("hotelId") String id) {
        return ResponseEntity.ok(repository.get(id));
    }

    @PutMapping("/update")
    public ResponseEntity update(@RequestBody String json) {
        return ResponseEntity.ok(repository.update(json));
    }
//    @PutMapping("/updateversion")
//    public ResponseEntity updateVersion(@RequestBody String json) {
//        System.out.println(json);
//        Item item = Item.fromJSON(json);
//        item.withString("version", ""+System.currentTimeMillis());
//        return ResponseEntity.ok().body(item.toJSONPretty());
//    }

    @PutMapping("/updatewithversion")
    public ResponseEntity updateWithVersion(@RequestBody String json) {
        return ResponseEntity.ok(repository.updateWithVersion(json));
    }
}
