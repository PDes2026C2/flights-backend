package ar.edu.unq.flights.repository.specifications;

import ar.edu.unq.flights.model.City;
import ar.edu.unq.flights.model.Country;
import ar.edu.unq.flights.model.Flight;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class FlightSpecifications {

    public static Specification<Flight> hasAirline(String airline) {
        return (root, query, cb) -> {
            if (airline == null || airline.isBlank()) return null;
            return cb.like(cb.lower(root.get("airline")), "%" + airline.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Flight> departureDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("departureDate"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("departureDate"), from);
            return cb.lessThanOrEqualTo(root.get("departureDate"), to);
        };
    }

    public static Specification<Flight> arrivalDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) return cb.between(root.get("arrivalDate"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("arrivalDate"), from);
            return cb.lessThanOrEqualTo(root.get("arrivalDate"), to);
        };
    }

    public static Specification<Flight> hasDestinationCity(Long cityId) {
        return (root, query, cb) -> {
            if (cityId == null) return null;

            Join<Flight, City> destinationJoin = root.join("destinationCity", JoinType.INNER);

            return cb.equal(destinationJoin.get("id"), cityId);
        };
    }

    public static Specification<Flight> hasDestinationCountryIso(String countryIso) {
        return (root, query, cb) -> {
            if (countryIso == null || countryIso.isBlank()) return null;

            Join<Flight, City> cityJoin = root.join("destinationCity", JoinType.INNER);
            Join<City, Country> countryJoin = cityJoin.join("country", JoinType.INNER);

            return cb.equal(cb.upper(countryJoin.get("isoCode")), countryIso.trim().toUpperCase());
        };
    }

    public static Specification<Flight> hasOriginCity(Long cityId) {
        return (root, query, cb) -> {
            if (cityId == null) return null;

            Join<Flight, City> originJoin = root.join("originCity", JoinType.INNER);

            return cb.equal(originJoin.get("id"), cityId);
        };
    }

    public static Specification<Flight> hasOriginCountryIso(String countryIso) {
        return (root, query, cb) -> {
            if (countryIso == null || countryIso.isBlank()) return null;

            Join<Flight, City> cityJoin = root.join("originCity", JoinType.INNER);
            Join<City, Country> countryJoin = cityJoin.join("country", JoinType.INNER);

            return cb.equal(cb.upper(countryJoin.get("isoCode")), countryIso.trim().toUpperCase());
        };
    }
}
