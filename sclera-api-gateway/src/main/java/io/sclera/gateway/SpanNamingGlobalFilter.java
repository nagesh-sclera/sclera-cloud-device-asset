package io.sclera.gateway;

import io.opentelemetry.api.trace.Span;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;

/**
 * Renames the OTEL server span for each request from the Spring Cloud Gateway
 * <em>route id</em> (e.g. {@code asset-route}) to the actual HTTP endpoint that was hit
 * (e.g. {@code GET /asset/user/admin/.../getdevicecount}).
 *
 * <p>The OTEL javaagent names the gateway's server span after the matched route's id, so in
 * Jaeger every request through a route collapses to a single operation name. By updating the
 * span name here — after the route is resolved — each trace is listed by the endpoint that was
 * actually called, so you can browse/click the relevant ones in the traces UI.
 *
 * <p>Runs at {@link Ordered#LOWEST_PRECEDENCE} so the rename happens after the agent's
 * route-id naming. The OTEL context is active on this thread (the agent bridges it across the
 * reactive chain), so {@link Span#current()} returns the live server span.
 */
@Component
public class SpanNamingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Span span = Span.current();
        if (span.getSpanContext().isValid()) {
            String method = exchange.getRequest().getMethod().name();
            span.updateName(method + " " + originalPath(exchange));
        }
        return chain.filter(exchange);
    }

    /**
     * The path the client actually called, before gateway filters (e.g. StripPrefix) rewrite it.
     * Spring Cloud Gateway records each pre-mutation URL in {@code GATEWAY_ORIGINAL_REQUEST_URL_ATTR}
     * (a LinkedHashSet, original first); fall back to the current request path if it is absent.
     */
    private static String originalPath(ServerWebExchange exchange) {
        Object attr = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        if (attr instanceof Set<?> urls && !urls.isEmpty()) {
            Object first = urls.iterator().next();
            if (first instanceof URI uri && uri.getRawPath() != null) {
                return uri.getRawPath();
            }
        }
        return exchange.getRequest().getURI().getRawPath();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
