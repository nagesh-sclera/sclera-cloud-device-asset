package io.sclera.utils;

import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Per-request holder for the VDMS this request targets. Replaces the process-global
 * vdms id that formerly lived in {@link AuthenticationUtils}. Populated by
 * {@code VdmsContextInterceptor}.
 */
@Component
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class VdmsContext {

    private String vdmsId;

    public String getVdmsId() {
        return vdmsId;
    }

    public void setVdmsId(String vdmsId) {
        this.vdmsId = vdmsId;
    }

    public boolean hasVdms() {
        return vdmsId != null && !vdmsId.trim().isEmpty();
    }
}
