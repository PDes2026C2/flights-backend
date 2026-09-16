package ar.edu.unq.flights.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name="passenger", schema="flight"
)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Passenger {
    @Id
    @Column(name = "dni", nullable = false)
    public int dni;
    @Column(name = "first_name", nullable = false)
    public String name;
    @Column(name = "last_name", nullable = false)
    public String lastName;
}
