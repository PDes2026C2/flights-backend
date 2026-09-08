package ar.edu.unq.flights.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(
        description = "Data Transfer Object for filtering flights. It allows filtering by airline, departure and arrival dates, origin and destination cities, and countries."
)
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
