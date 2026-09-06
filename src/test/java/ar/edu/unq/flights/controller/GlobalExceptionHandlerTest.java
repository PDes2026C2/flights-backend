package ar.edu.unq.flights.controller;

import ar.edu.unq.flights.exception.FlightFullException;
import ar.edu.unq.flights.exception.FlightNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test-errors")
    static class TestErrorController {

        @GetMapping("/not-found")
        public void throwNotFound() {
            throw new FlightNotFoundException("Flight not found with id: 99");
        }

        @GetMapping("/flight-full")
        public void throwFlightFull() {
            throw new FlightFullException("Flight is full.");
        }

        @GetMapping("/illegal-argument")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("Capacity must be greater than 0.");
        }

        @GetMapping("/general-error")
        public void throwGeneral() {
            throw new RuntimeException("Unexpected database failure");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestErrorController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should return 404 NOT FOUND when FlightNotFoundException is thrown")
    void handleFlightNotFoundException() throws Exception {
        mockMvc.perform(get("/test-errors/not-found")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Flight not found with id: 99")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Should return 409 CONFLICT when FlightFullException is thrown")
    void handleFlightFullException() throws Exception {
        mockMvc.perform(get("/test-errors/flight-full")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", is("Flight is full.")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Should return 400 BAD REQUEST when IllegalArgumentException is thrown")
    void handleIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test-errors/illegal-argument")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Capacity must be greater than 0.")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Should return 500 INTERNAL SERVER ERROR when unhandled Exception is thrown")
    void handleGeneralException() throws Exception {
        mockMvc.perform(get("/test-errors/general-error")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)))
                .andExpect(jsonPath("$.error", is("Internal Server Error")))
                .andExpect(jsonPath("$.message", is("An unexpected error occurred.")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}
