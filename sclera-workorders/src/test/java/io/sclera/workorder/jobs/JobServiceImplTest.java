package io.sclera.workorder.jobs;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.sclera.workorder.config.DaprProperties;
import io.sclera.workorder.config.JobsProperties;
import io.sclera.workorder.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JobServiceImplTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final RestClient daprRestClient = mock(RestClient.class);   // unused on the happy path
    private final TicketRepository ticketRepository = mock(TicketRepository.class);

    private JobServiceImpl newService(JobsProperties props) {
        return new JobServiceImpl(props, new DaprProperties(), daprRestClient, registry, ticketRepository);
    }

    private static JobsProperties props(boolean enabled, int attempts) {
        JobsProperties p = new JobsProperties();
        p.setEnabled(enabled);
        p.setMaxAttempts(attempts);
        p.setRetryDelayMs(0);
        return p;
    }

    @Test
    void statusRefresh_runsAndRecordsSuccessMetric() {
        when(ticketRepository.count()).thenReturn(5L);
        JobServiceImpl service = newService(props(true, 1));

        boolean ok = service.run("status-refresh");

        assertThat(ok).isTrue();
        verify(ticketRepository).count();
        assertThat(registry.counter("jobs.runs", "job", "status-refresh", "outcome", "success").count())
                .isEqualTo(1.0);
    }

    @Test
    void unknownJob_failsAndReturnsFalse() {
        JobServiceImpl service = newService(props(true, 1));

        boolean ok = service.run("does-not-exist");

        assertThat(ok).isFalse();
        assertThat(registry.counter("jobs.runs", "job", "does-not-exist", "outcome", "failure").count())
                .isEqualTo(1.0);
    }

    @Test
    void disabled_skipsWithoutRunning() {
        JobServiceImpl service = newService(props(false, 3));

        boolean ok = service.run("status-refresh");

        assertThat(ok).isTrue();
        verifyNoInteractions(ticketRepository);
    }
}
