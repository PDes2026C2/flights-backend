package ar.edu.unq.flights.exception;

public class FlightNotFoundException extends RuntimeException {
    public FlightNotFoundException() {
        super("Flight not found.");
    }
}
