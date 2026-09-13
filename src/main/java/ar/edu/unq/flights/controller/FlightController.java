package ar.edu.unq.flights.controller;

import ar.edu.unq.flights.controller.dto.ErrorDTO;
import ar.edu.unq.flights.controller.dto.FlightDTO;
import ar.edu.unq.flights.controller.dto.FlightFilterDTO;
import ar.edu.unq.flights.controller.dto.PassengerDTO;
import ar.edu.unq.flights.model.Flight;
import ar.edu.unq.flights.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flights")
public class FlightController {
    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @Operation(
            summary = "Search for flights based on the provided filter criteria. You can filter by origin, destination, departure date, and arrival date. The results are paginated."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Flights retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorDTO.class)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<FlightDTO>> searchFlights(
            @ParameterObject FlightFilterDTO filter,
            @ParameterObject @PageableDefault(size = 15, page = 0) Pageable page) {
        List<Flight> flights = flightService.searchFlights(filter, page);
        return ResponseEntity.ok(flights.stream().map(FlightDTO::from).toList());
    }

    @Operation(
            summary = "Sell a flight ticket for a specific flight. Provide the flight ID and passenger details (DNI, name, surname) in the request body."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Flight ticket sold successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Flight not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(
                            schema = @Schema(implementation = ErrorDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Flight is full. Cannot sell ticket",
                    content = @Content(
                            schema = @Schema(implementation = ErrorDTO.class)
                    )
            )
    })
    @PostMapping("/{id}/sell")
    public ResponseEntity<FlightDTO> sellFlight(
            @PathVariable Long id,
            @RequestBody PassengerDTO passengerDTO
    ) {
        Flight flight = flightService.sellFlight(
                id,
                passengerDTO.dni(),
                passengerDTO.name(),
                passengerDTO.surname()
        );
        return ResponseEntity.ok(FlightDTO.from(flight));
    }


    @Operation(
            summary = "Get flight details by flight ID. Provide the flight ID in the path variable."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Flight details retrieved successfully",
                content = @Content(
                        schema = @Schema(implementation = FlightDTO.class)
                )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Flight not found",
                    content = @Content(
                            schema = @Schema(implementation = ErrorDTO.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<FlightDTO> getFlight(
            @PathVariable Long id
    ) {
        Flight flight = flightService.getFlightById(id);
        return ResponseEntity.ok(FlightDTO.from(flight));
    }
}
