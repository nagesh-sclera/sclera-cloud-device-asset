package io.sclera.scheduler.client;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerClientTest {

    static HttpServer server;
    static int port;
    static final List<String> requests = new ArrayList<>();
    static final List<String> bodies = new ArrayList<>();

    @BeforeAll
    static void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        server.createContext("/", ex -> {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requests.add(ex.getRequestMethod() + " " + ex.getRequestURI().getPath());
            bodies.add(body);
            // Jobs named "missing" simulate a not-registered job → Dapr returns 404.
            int status = ex.getRequestURI().getPath().endsWith("/missing") ? 404 : 204;
            ex.sendResponseHeaders(status, -1);
            ex.close();
        });
        server.start();
    }

    @AfterAll
    static void stop() { server.stop(0); }

    @BeforeEach
    void clear() { requests.clear(); bodies.clear(); }

    SchedulerClient client() {
        return new SchedulerClient("http://localhost:" + port);
    }

    @Test
    void scheduleJobPostsToJobsApiWithSchedule() {
        client().schedule(new JobSchedule("snmpSync", "0 0 */3 * * *"));

        assertThat(requests).containsExactly("POST /v1.0-alpha1/jobs/snmpSync");
        assertThat(bodies.get(0)).contains("\"schedule\":\"0 0 */3 * * *\"");
    }

    @Test
    void deleteJobCallsDelete() {
        client().delete("snmpSync");
        assertThat(requests).containsExactly("DELETE /v1.0-alpha1/jobs/snmpSync");
    }

    @Test
    void deleteSwallows404ForUnregisteredJob() {
        // Must NOT throw: pause/disable on a job the Scheduler never registered is a no-op.
        client().delete("missing");
        assertThat(requests).containsExactly("DELETE /v1.0-alpha1/jobs/missing");
    }
}
