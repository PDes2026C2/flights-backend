package ar.edu.unq.flights.model;

import ar.edu.unq.flights.exception.FlightFullException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Flight {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name="capacity", nullable = false)
    private int capacity;

    @Column(name="airline", nullable = false)
    private String airline;

    @Column(name="departureDate", nullable = false)
    private LocalDateTime departureDate;

    @Column(name="arrivalDate", nullable = false)
    private LocalDateTime arrivalDate;

    @ManyToOne
    @JoinColumn(name="origin_city_id")
    private City originCity;

    @ManyToOne
    @JoinColumn(name="destination_city_id")
    private City destinationCity;

    @ManyToMany
    @JoinTable(
            name = "flight_passenger",
            joinColumns = @JoinColumn(name = "flight_id"),
            inverseJoinColumns = @JoinColumn(name = "passenger_id")
    )
    private List<Passenger> passengers = new ArrayList<>();

    public Flight(int capacity, String airline, LocalDateTime departureDate,
                  LocalDateTime arrivalDate, City originCity, City destinationCity) {
        validateFlight(capacity, departureDate, arrivalDate, originCity, destinationCity);
        this.capacity = capacity;
        this.airline = airline;
        this.departureDate = departureDate;
        this.arrivalDate = arrivalDate;
        this.originCity = originCity;
        this.destinationCity = destinationCity;
    }

    private void validateFlight(int capacity, LocalDateTime departure, LocalDateTime arrival,
                                City origin, City destination) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0.");
        }

        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Origin and destination are required.");
        }

        if (origin.equals(destination)) {
            throw new IllegalArgumentException("Origin and destination could not be the same city.");
        }

        if (departure == null || arrival == null || !arrival.isAfter(departure)) {
            throw new IllegalArgumentException("Departure date must be before arrival date.");
        }
    }

    public void sellTicket(Passenger passenger) {
        validateTicketSelling();
        passengers.add(passenger);
    }

    private void validateTicketSelling() {
        if (passengers.size() >= capacity) {
            throw new FlightFullException();
        }
    }
}
