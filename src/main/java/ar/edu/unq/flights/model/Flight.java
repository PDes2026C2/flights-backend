package ar.edu.unq.flights.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Flight {
    @Id
    @GeneratedValue
    public Long id;

    @Column(name="capacity", nullable = false)
    public int capacity;

    @Column(name="airline", nullable = false)
    public String airline;

    @Column(name="departureDate", nullable = false)
    public LocalDateTime departureDate;

    @Column(name="arrivalDate", nullable = false)
    public LocalDateTime arrivalDate;

    @ManyToOne
    @JoinColumn(name="origin_city_id")
    public City originCity;

    @ManyToOne
    @JoinColumn(name="destination_city_id")
    public City destinationCity;

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
            throw new IllegalArgumentException("La capacidad debe ser mayor a 0");
        }

        if (origin == null || destination == null) {
            throw new IllegalArgumentException("El origen y el destino son obligatorios");
        }

        if (origin.equals(destination)) {
            throw new IllegalArgumentException("El origen y el destino no pueden ser la misma ciudad");
        }

        if (departure == null || arrival == null || !arrival.isAfter(departure)) {
            throw new IllegalArgumentException("La fecha de llegada debe ser posterior a la fecha de salida");
        }
    }

}
