package ar.edu.unq.flights.controller.dto;

import java.time.LocalDateTime;

public record FlightFilterDTO(
        String airline,
        LocalDateTime departureDateFrom,
        LocalDateTime departureDateTo,

        LocalDateTime arrivalDateFrom,
        LocalDateTime arrivalDateTo,

        Long originCityId,
        String originCountryIsoCode,

        Long destinationCityId,
        String destinationCountryIsoCode
) {
}
