package ar.edu.unq.flights.controller.dto;

import ar.edu.unq.flights.model.City;

public record CityDTO(
        Long id,
        String name,
        CountryDTO country
) {

    public static CityDTO from(City city) {
        return new CityDTO(
                city.getId(),
                city.getName(),
                CountryDTO.from(city.getCountry())
        );
    }
}
