package io.sclera.client;

import io.dapr.client.DaprClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin wrapper around Dapr output bindings. Each method targets a single binding
 * component (see {@code dapr/components/local/binding-*.yaml}).
 *
 * <p>Dev: bindings point at local httpbin / mailhog. Prod: component file alone
 * is swapped to the real Corrigo/Daintree/SMTP endpoints.
 */
@Component
public class OutputBindingClient {

    private static final Logger log = LoggerFactory.getLogger(OutputBindingClient.class);
    private static final String BINDING_CORRIGO = "binding-corrigo";
    private static final String BINDING_DAINTREE = "binding-daintree";
    private static final String BINDING_SMTP = "binding-smtp";

    private final DaprClient dapr;

    public OutputBindingClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** POST a payload to Corrigo. Metadata becomes HTTP headers / path params at the binding level. */
    public boolean pushToCorrigo(Object payload, String orgId, String configId) {
        Map<String, String> md = new HashMap<>();
        if (orgId != null) md.put("orgId", orgId);
        if (configId != null) md.put("configId", configId);
        return invokeBindingBestEffort(BINDING_CORRIGO, payload, md);
    }

    /** POST a payload to Daintree. */
    public boolean pushToDaintree(Object payload, String tenantId) {
        Map<String, String> md = new HashMap<>();
        if (tenantId != null) md.put("tenantId", tenantId);
        return invokeBindingBestEffort(BINDING_DAINTREE, payload, md);
    }

    /** Send an email via the SMTP binding. Subject + recipient go in metadata per the SMTP binding contract. */
    public boolean sendEmail(String to, String subject, String body) {
        Map<String, String> md = new HashMap<>();
        md.put("emailTo", to);
        md.put("subject", subject);
        try {
            dapr.invokeBinding(BINDING_SMTP, "create", body, md, byte[].class).block();
            return true;
        } catch (Exception e) {
            log.warn("OutputBindingClient.sendEmail to={} failed: {}", to, e.getMessage());
            return false;
        }
    }

    private boolean invokeBindingBestEffort(String binding, Object payload, Map<String, String> md) {
        try {
            dapr.invokeBinding(binding, "create", payload, md, byte[].class).block();
            return true;
        } catch (Exception e) {
            log.warn("OutputBindingClient.invoke binding={} failed: {}", binding, e.getMessage());
            return false;
        }
    }
}
