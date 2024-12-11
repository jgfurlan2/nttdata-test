package io.github.jgfurlan2.nttdata_test.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String zipCode;
    private String streetName;
    private String streetNumber;
    private String neighborhood;
    private String city;
    private String state;
    private String country;

}
