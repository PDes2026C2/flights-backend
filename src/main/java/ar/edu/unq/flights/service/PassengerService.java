package ar.edu.unq.flights.service;

import ar.edu.unq.flights.model.Passenger;
import org.springframework.stereotype.Service;

public interface PassengerService {
    Passenger save(Passenger passenger);
}
