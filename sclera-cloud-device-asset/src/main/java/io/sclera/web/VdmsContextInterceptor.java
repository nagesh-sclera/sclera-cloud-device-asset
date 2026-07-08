package io.sclera.web;

import io.sclera.Repository.VdmsRepository;
import io.sclera.utils.VdmsContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Resolves the target VDMS for each request and stores it in the request-scoped
 * {@link VdmsContext}. Precedence: {@code X-Vdms-Id} header, then {@code vdms_id}
 * request parameter, then the single existing VDMS (backward compatibility).
 */
@Component
public class VdmsContextInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Vdms-Id";
    public static final String PARAM = "vdms_id";

    private final VdmsContext vdmsContext;
    private final VdmsRepository vdmsRepository;

    public VdmsContextInterceptor(VdmsContext vdmsContext, VdmsRepository vdmsRepository) {
        this.vdmsContext = vdmsContext;
        this.vdmsRepository = vdmsRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String id = request.getHeader(HEADER);
        if (isBlank(id)) {
            id = request.getParameter(PARAM);
        }
        if (isBlank(id)) {
            id = vdmsRepository.findSingleVdmsId();
        }
        if (!isBlank(id)) {
            vdmsContext.setVdmsId(id);
        }
        return true;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
