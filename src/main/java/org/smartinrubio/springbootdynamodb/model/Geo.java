package org.smartinrubio.springbootdynamodb.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Geo {
    private final Double latitude;
    private final Double longitude;
}
