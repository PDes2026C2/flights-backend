package ar.edu.unq.flights.exception;

public class FlightFullException extends RuntimeException {
    public FlightFullException() {
        super("Flight is full.");
    }
}
