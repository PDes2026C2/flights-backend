package ar.edu.unq.flights.controller.dto;

public record FlightDTO(
        long id,
        String airline,
        CityDTO originCity,
        CityDTO destinationCity,
        String departureDate,
        String arrivalDate
) {
    public static FlightDTO from(ar.edu.unq.flights.model.Flight flight) {
        return new FlightDTO(
                flight.getId(),
                flight.getAirline(),
                CityDTO.from(flight.getOriginCity()),
                CityDTO.from(flight.getDestinationCity()),
                flight.getDepartureDate().toString(),
                flight.getArrivalDate().toString()
        );
    }
}
