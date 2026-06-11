package io.sclera.config;

import io.opentelemetry.api.trace.Span;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Adds the request and response BODY of EVERY request to the OpenTelemetry server span the
 * javaagent created, so the full payload is visible alongside the captured headers
 * (OTEL_INSTRUMENTATION_HTTP_SERVER_CAPTURE_*_HEADERS) and the un-sanitized SQL in Jaeger
 * (and anything else reading those spans — the traces UI, Grafana's Jaeger datasource, etc.).
 *
 * <p>Bodies are captured for any non-binary payload (JSON / text / xml / form / unknown), capped
 * at {@code sclera.tracing.payload-max-bytes} (default 256 KB). Binary payloads — file uploads
 * (multipart) and downloads (octet-stream, images, pdf, the .xlsx export, …) — are skipped so we
 * don't dump binary blobs into spans.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class PayloadTracingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(PayloadTracingFilter.class);

    /** Max bytes of a body captured onto a span. Override with sclera.tracing.payload-max-bytes. */
    @Value("${sclera.tracing.payload-max-bytes:262144}")
    private int maxBytes;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        boolean captureRequest = !isBinary(request.getContentType());
        HttpServletRequest reqToUse = captureRequest ? new ContentCachingRequestWrapper(request, maxBytes) : request;
        ContentCachingResponseWrapper respWrapper = new ContentCachingResponseWrapper(response);

        try {
            chain.doFilter(reqToUse, respWrapper);
        } finally {
            try {
                Span span = Span.current();
                if (span != null && span.getSpanContext().isValid()) {
                    if (reqToUse instanceof ContentCachingRequestWrapper) {
                        byte[] body = ((ContentCachingRequestWrapper) reqToUse).getContentAsByteArray();
                        if (body.length > 0) span.setAttribute("http.request.body", clip(body));
                    }
                    if (!isBinary(respWrapper.getContentType())) {
                        byte[] body = respWrapper.getContentAsByteArray();
                        if (body.length > 0) span.setAttribute("http.response.body", clip(body));
                    }
                }
            } catch (Exception e) {
                // Never let tracing break the request.
                log.debug("PayloadTracingFilter: failed to attach payload to span: {}", e.getMessage());
            }
            // CRITICAL: write the buffered response back to the real output stream.
            respWrapper.copyBodyToResponse();
        }
    }

    private String clip(byte[] body) {
        int len = Math.min(body.length, maxBytes);
        String s = new String(body, 0, len, StandardCharsets.UTF_8);
        return body.length > maxBytes ? s + "…(truncated " + body.length + " bytes)" : s;
    }

    /** True for payloads we should NOT dump into a span (file uploads/downloads and other binaries). */
    private boolean isBinary(String contentType) {
        if (contentType == null) return false; // unknown -> treat as text and capture
        String ct = contentType.toLowerCase();
        return ct.contains("multipart/")
                || ct.contains("octet-stream")
                || ct.startsWith("image/")
                || ct.startsWith("video/")
                || ct.startsWith("audio/")
                || ct.contains("pdf")
                || ct.contains("zip")
                || ct.contains("spreadsheet")    // .xlsx export
                || ct.contains("ms-excel")
                || ct.contains("officedocument");
    }
}
