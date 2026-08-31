package ar.edu.unq.flights.service.impl;

import ar.edu.unq.flights.controller.FlightFilterDTO;
import ar.edu.unq.flights.model.Flight;
import ar.edu.unq.flights.repository.FlightRepository;
import ar.edu.unq.flights.repository.specifications.FlightSpecifications;
import ar.edu.unq.flights.service.FlightService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlightServiceImpl implements FlightService {
    public FlightRepository flightRepository;

    public FlightServiceImpl(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }


    @Transactional(readOnly = true)
    @Override
    public List<Flight> searchFlights(FlightFilterDTO filter, Pageable page) {
        Specification<Flight> spec = Specification
                .where(FlightSpecifications.hasAirline(filter.airline()))
                .and(FlightSpecifications.departureDateBetween(filter.departureDateFrom(), filter.departureDateTo()))
                .and(FlightSpecifications.arrivalDateBetween(filter.arrivalDateFrom(), filter.arrivalDateTo()))
                .and(FlightSpecifications.hasOriginCity( filter.originCityId()))
                .and(FlightSpecifications.hasOriginCountryIso(filter.originCountryIsoCode()))
                .and(FlightSpecifications.hasDestinationCity(filter.destinationCityId()))
                .and(FlightSpecifications.hasDestinationCountryIso(filter.destinationCountryIsoCode()));

        return flightRepository.findAll(spec, page).getContent();
    }
}
