package io.sclera.web;

import io.sclera.Repository.VdmsRepository;
import io.sclera.utils.VdmsContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VdmsContextInterceptorTest {

    @Mock HttpServletRequest request;
    @Mock VdmsRepository vdmsRepository;

    private VdmsContextInterceptor newInterceptor(VdmsContext ctx) {
        return new VdmsContextInterceptor(ctx, vdmsRepository);
    }

    @Test
    void header_takesPrecedence() {
        VdmsContext ctx = new VdmsContext();
        when(request.getHeader("X-Vdms-Id")).thenReturn("H1");
        lenient().when(request.getParameter("vdms_id")).thenReturn("P1");
        newInterceptor(ctx).preHandle(request, null, null);
        assertThat(ctx.getVdmsId()).isEqualTo("H1");
    }

    @Test
    void param_usedWhenNoHeader() {
        VdmsContext ctx = new VdmsContext();
        when(request.getHeader("X-Vdms-Id")).thenReturn(null);
        when(request.getParameter("vdms_id")).thenReturn("P1");
        newInterceptor(ctx).preHandle(request, null, null);
        assertThat(ctx.getVdmsId()).isEqualTo("P1");
    }

    @Test
    void singleRowFallback_whenNoHeaderNoParam() {
        VdmsContext ctx = new VdmsContext();
        when(request.getHeader("X-Vdms-Id")).thenReturn(null);
        when(request.getParameter("vdms_id")).thenReturn(null);
        when(vdmsRepository.findSingleVdmsId()).thenReturn("ONLY");
        newInterceptor(ctx).preHandle(request, null, null);
        assertThat(ctx.getVdmsId()).isEqualTo("ONLY");
    }

    @Test
    void unset_whenNothingResolves() {
        VdmsContext ctx = new VdmsContext();
        when(request.getHeader("X-Vdms-Id")).thenReturn(null);
        when(request.getParameter("vdms_id")).thenReturn(null);
        when(vdmsRepository.findSingleVdmsId()).thenReturn(null);
        newInterceptor(ctx).preHandle(request, null, null);
        assertThat(ctx.hasVdms()).isFalse();
    }
}
