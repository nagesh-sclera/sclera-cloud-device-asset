package io.sclera.scheduler.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VdmsDirectoryClientTest {

    static HttpServer server;
    static int port;

    @BeforeAll
    static void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        server.createContext("/", ex -> {
            String json = "[{\"vdmsId\":\"vdms-1\",\"timezone\":\"UTC\"},"
                        + "{\"vdmsId\":\"vdms-2\",\"timezone\":\"America/New_York\"}]";
            byte[] out = json.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, out.length);
            ex.getResponseBody().write(out);
            ex.close();
        });
        server.start();
    }

    @AfterAll
    static void stop() { server.stop(0); }

    @Test
    void fetchesActiveVdmsList() {
        VdmsDirectoryClient client =
                new VdmsDirectoryClient("http://localhost:" + port, "sclera-vdms-service");

        List<VdmsActiveDto> active = client.fetchActiveVdms();

        assertThat(active).extracting(VdmsActiveDto::vdmsId).containsExactly("vdms-1", "vdms-2");
        assertThat(active).extracting(VdmsActiveDto::timezone).containsExactly("UTC", "America/New_York");
    }
}
