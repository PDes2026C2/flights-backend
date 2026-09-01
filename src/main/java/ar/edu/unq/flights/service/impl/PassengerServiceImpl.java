package ar.edu.unq.flights.service.impl;

import ar.edu.unq.flights.model.Passenger;
import ar.edu.unq.flights.repository.PassengerRepository;
import ar.edu.unq.flights.service.PassengerService;
import org.springframework.stereotype.Service;

@Service
public class PassengerServiceImpl implements PassengerService {
    private final PassengerRepository passengerRepository;

    public PassengerServiceImpl(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    @Override
    public Passenger save(Passenger passenger) {
        // if a passenger p with dni x exists, it overrides p.name and p.surname with passenger.name and passenger.surname
        return passengerRepository.save(passenger);
    }

}
