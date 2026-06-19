package io.sclera.workorder;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the per-class logback file appenders are bound to the actual service-impl
 * logger names. The impls live in {@code io.sclera.workorder.service.impl} and their
 * loggers are named after those classes (Lombok {@code @Slf4j} / {@code getLogger(X.class)}),
 * so the {@code <logger name="...">} bindings must use those FQNs — otherwise the
 * per-service Debug/Error files silently never receive anything.
 */
class LoggerRoutingTest {

    private List<String> appendersFor(String configResource, String loggerName) throws Exception {
        LoggerContext ctx = new LoggerContext();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(ctx);
        try (InputStream is = getClass().getResourceAsStream(configResource)) {
            assertThat(is).as("logback config on classpath: %s", configResource).isNotNull();
            configurator.doConfigure(is);
        }
        Logger logger = ctx.getLogger(loggerName);
        List<String> names = new ArrayList<>();
        for (Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders(); it.hasNext(); ) {
            names.add(it.next().getName());
        }
        ctx.stop();
        return names;
    }

    @Test
    void developmentConfig_routesEachServiceImplLoggerToItsOwnFiles() throws Exception {
        String cfg = "/logback/logback-development.xml";

        assertThat(appendersFor(cfg, "io.sclera.workorder.service.impl.MaximoServiceImpl"))
                .contains("MaximoServiceDebugLogger", "MaximoServiceErrorLogger");
        assertThat(appendersFor(cfg, "io.sclera.workorder.service.impl.TicketHistoryServiceImpl"))
                .contains("TicketHistoryServiceDebugLogger", "TicketHistoryServiceErrorLogger");
        assertThat(appendersFor(cfg, "io.sclera.workorder.service.impl.TicketServiceImpl"))
                .contains("TicketServiceDebugLogger", "TicketServiceErrorLogger");
        assertThat(appendersFor(cfg, "io.sclera.workorder.service.impl.UserActionLogServiceImpl"))
                .contains("UserActionLogServiceDebugLogger", "UserActionLogServiceErrorLogger");
    }
}
