package ar.edu.unq.flights.config;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.server.port=0")
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@AutoConfigureMetrics
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

    private HttpResponse<String> get(int port, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
