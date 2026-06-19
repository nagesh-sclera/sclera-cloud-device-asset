package io.sclera.workorder.config;

import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Forwards the current request's correlation id (SLF4J MDC {@code requestId}) as an
 * {@code X-Request-Id} header on outbound Dapr service-invocation calls, so the id survives
 * service-to-service hops. Dapr forwards the header to the target service, whose
 * {@code CorrelationIdFilter} reads it back into its MDC.
 *
 * If there's no id in scope (e.g. a scheduled job, not a web request) nothing is added.
 */
public class CorrelationIdPropagationInterceptor implements ClientHttpRequestInterceptor {

    public static final String HEADER = "X-Request-Id";
    private static final String MDC_KEY = "requestId";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String requestId = MDC.get(MDC_KEY);
        if (requestId != null && !requestId.isBlank() && request.getHeaders().getFirst(HEADER) == null) {
            request.getHeaders().add(HEADER, requestId);
        }
        return execution.execute(request, body);
    }
}
