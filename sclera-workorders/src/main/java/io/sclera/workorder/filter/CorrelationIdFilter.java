package io.sclera.workorder.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Puts a correlation id into the SLF4J MDC for the duration of each request, so every log
 * line carries it via the {@code %X{requestId}} pattern.
 *
 * The id comes from the {@code X-Request-Id} header (set by the gateway, or forwarded by a
 * calling service's Dapr client); if absent a short one is generated. It is echoed on the
 * response and cleared from the MDC in a finally block so thread-pool reuse never leaks an
 * id into the next request. A concise request-entry line is logged (noise paths excluded).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put(MDC_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            if (shouldLog(request.getRequestURI())) {
                log.info("{} {}", request.getMethod(), request.getRequestURI());
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private static boolean shouldLog(String path) {
        return path != null
                && !path.contains("/actuator")
                && !path.contains("/swagger")
                && !path.contains("/v3/api-docs")
                && !path.contains("/healthz");
    }
}
