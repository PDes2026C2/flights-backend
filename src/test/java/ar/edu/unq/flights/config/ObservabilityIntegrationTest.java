package ar.edu.unq.flights.config;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.server.port=0")
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@AutoConfigureMetrics
@ExtendWith(OutputCaptureExtension.class)
class ObservabilityIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Value("${local.server.port}")
    private int applicationPort;

    @Value("${local.management.port}")
    private int managementPort;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test
    @DisplayName("Should expose health and probes without internal details")
    void healthAndProbesAreAccessibleWithoutExposingDetails() throws Exception {
        for (String path : new String[]{"/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness"}) {
            HttpResponse<String> response = get(managementPort, path);
            Map<String, Object> health = JsonPath.read(response.body(), "$");

            assertThat(response.statusCode()).as(path).isEqualTo(200);
            assertThat(health).containsEntry("status", "UP").doesNotContainKeys("components", "details");
        }
    }

    @Test
    @DisplayName("Should export JVM, connection pool and HTTP request metrics for flights")
    void prometheusExportsRequestHistogramsAndJvmMetrics() throws Exception {
        assertThat(get(applicationPort, "/flights?size=1").statusCode()).isEqualTo(200);

        HttpResponse<String> response = get(managementPort, "/actuator/prometheus");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("jvm_memory_used_bytes", "hikaricp_connections_active",
                "http_server_requests_seconds_bucket", "uri=\"/flights\"", "application=\"flights\"");
    }

    @Test
    @DisplayName("Should serve Prometheus metrics on a separate management port")
    void prometheusIsOnlyAvailableOnManagementPort() throws Exception {
        HttpResponse<String> response = get(applicationPort, "/actuator/prometheus");

        assertThat(managementPort).isNotEqualTo(applicationPort);
        assertThat(response.statusCode()).isBetween(400, 599);
        assertThat(response.body()).doesNotContain("jvm_memory_used_bytes");
    }

    @Test
    @DisplayName("Should expose only health and Prometheus management endpoints")
    void managementDiscoveryExposesOnlyHealthAndPrometheus() throws Exception {
        HttpResponse<String> response = get(managementPort, "/actuator");
        Map<String, Object> links = JsonPath.read(response.body(), "$._links");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(links).containsOnlyKeys("self", "health", "health-path", "prometheus");
    }

    @Test
    @DisplayName("Should correlate the HTTP response with a structured request log")
    void httpRequestsReturnTheirIdAndStructuredLog(CapturedOutput output) throws Exception {
        String requestId = "09c409d2-cf9b-4a33-a5f5-388dcf4de99e";
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + applicationPort + "/flights?privateValue=not-for-logs"))
                .header("X-Request-ID", requestId)
                .header("X-Private-Value", "private-header-value")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("X-Request-ID")).contains(requestId);
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            Map<String, Object> log = requestLog(output, requestId);
            assertThat(log).containsEntry("httpMethod", "GET")
                    .containsEntry("httpRoute", "/flights").containsEntry("httpStatus", 200);
            assertThat(((Number) log.get("durationMs")).longValue()).isGreaterThanOrEqualTo(0);
            assertThat(JsonPath.<String>read(log, "$.service.name")).isEqualTo("flights");
            assertThat(log.toString()).doesNotContain("privateValue", "not-for-logs", "private-header-value");
        });
    }

    @Test
    @DisplayName("Should generate distinct UUIDs for absent or invalid request IDs")
    void httpRequestsGenerateDistinctIdsForMissingOrInvalidHeaders() throws Exception {
        Set<String> generated = new HashSet<>();
        for (String candidate : new String[]{null, null, "not-a-uuid", "1-1-1-1-1", "x".repeat(200)}) {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + applicationPort + "/flights"));
            if (candidate != null) {
                request.header("X-Request-ID", candidate);
            }

            HttpResponse<String> response = client.send(request.GET().build(), HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            String requestId = response.headers().firstValue("X-Request-ID").orElseThrow();
            assertThat(UUID.fromString(requestId).toString()).isEqualTo(requestId);
            assertThat(generated.add(requestId)).isTrue();
        }
    }

    @Test
    @DisplayName("Should preserve correlation and log the status of an error response")
    void errorResponsesKeepTheirRequestId(CapturedOutput output) throws Exception {
        String requestId = "015e7bad-55b7-4ff7-81c5-ce36feffba9b";
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + applicationPort + "/flights/9223372036854775807"))
                .header("X-Request-ID", requestId).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.headers().firstValue("X-Request-ID")).contains(requestId);
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            Map<String, Object> log = requestLog(output, requestId);
            assertThat(log).containsEntry("httpStatus", 404);
            assertThat(JsonPath.<String>read(log, "$.log.level")).isEqualTo("WARN");
        });
    }

    @Test
    @DisplayName("Should keep request IDs isolated for concurrent HTTP requests")
    void concurrentRequestsKeepTheirOwnIds(CapturedOutput output) throws Exception {
        List<String> requestIds = List.of("083541b8-ee65-41b9-903a-1c9ddb2bca50",
                "3c74e61f-d35e-41e3-86ae-ced9746a7ff1", "bfb8c617-e68f-4e49-805d-70b1aa7ff850");
        var responses = requestIds.stream().map(requestId -> client.sendAsync(
                HttpRequest.newBuilder(URI.create("http://localhost:" + applicationPort + "/flights"))
                        .header("X-Request-ID", requestId).GET().build(), HttpResponse.BodyHandlers.ofString())).toList();

        for (int index = 0; index < responses.size(); index++) {
            HttpResponse<String> response = responses.get(index).get(10, TimeUnit.SECONDS);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("X-Request-ID")).contains(requestIds.get(index));
        }
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            for (String requestId : requestIds) {
                assertThat(requestLog(output, requestId)).containsEntry("httpRoute", "/flights")
                        .containsEntry("httpStatus", 200);
            }
        });
    }

    private Map<String, Object> requestLog(CapturedOutput output, String requestId) {
        return output.getOut().lines()
                .filter(line -> line.startsWith("{"))
                .map(line -> JsonPath.<Map<String, Object>>read(line, "$"))
                .filter(log -> requestId.equals(log.get("requestId"))
                        && "Solicitud HTTP finalizada".equals(log.get("message")))
                .findFirst().orElseThrow();
    }

    private HttpResponse<String> get(int port, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
