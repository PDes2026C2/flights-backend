package ar.edu.unq.flights.controller.dto;

import ar.edu.unq.flights.model.Country;

public record CountryDTO(
        String isoCode,
        String name
) {

    public static CountryDTO from(Country country) {
        return new CountryDTO(
                country.getIsoCode(),
                country.getName()
        );
    }
}
