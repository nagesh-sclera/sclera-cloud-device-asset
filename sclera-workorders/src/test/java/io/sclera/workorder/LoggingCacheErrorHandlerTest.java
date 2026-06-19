package io.sclera.workorder;

import io.sclera.workorder.config.LoggingCacheErrorHandler;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import static org.assertj.core.api.Assertions.assertThatCode;

class LoggingCacheErrorHandlerTest {

    private final LoggingCacheErrorHandler handler = new LoggingCacheErrorHandler();
    private final ConcurrentMapCache cache = new ConcurrentMapCache("workorder");
    private final RuntimeException boom = new RuntimeException("redis down");

    @Test
    void allHandlers_swallowExceptions() {
        assertThatCode(() -> handler.handleCacheGetError(boom, cache, "k")).doesNotThrowAnyException();
        assertThatCode(() -> handler.handleCachePutError(boom, cache, "k", "v")).doesNotThrowAnyException();
        assertThatCode(() -> handler.handleCacheEvictError(boom, cache, "k")).doesNotThrowAnyException();
        assertThatCode(() -> handler.handleCacheClearError(boom, cache)).doesNotThrowAnyException();
    }
}
