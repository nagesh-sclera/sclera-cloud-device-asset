package io.sclera.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.tomcat.TomcatWebServerFactory;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 * Configures the embedded Tomcat server (when SSL is enabled) to require confidential transport on
 * all paths and to add an additional HTTP connector that redirects to the secure port.
 */
@Configuration
@ConditionalOnProperty(name = "server.ssl.enabled", havingValue = "true", matchIfMissing = true)
public class ServerConfig {

    @Autowired
    private Environment environment;

    /**
     * Provides a Tomcat servlet web server factory that enforces a CONFIDENTIAL security constraint
     * on all paths and registers the HTTP-to-HTTPS redirect connector.
     *
     * @return the configured servlet web server factory
     */
    @Bean
    public ServletWebServerFactory servletWebServerFactory() {
        TomcatServletWebServerFactory tomcatServletWebServerFactory = null;
        tomcatServletWebServerFactory = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/**");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcatServletWebServerFactory.addAdditionalConnectors(redirectConnector());
        return tomcatServletWebServerFactory;
    }

    private Connector redirectConnector() {
        int httpPort = Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.http.port"), "Http port cannot be null"));
        org.apache.catalina.connector.Connector connector = new org.apache.catalina.connector.Connector(TomcatWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        return connector;
    }
}
