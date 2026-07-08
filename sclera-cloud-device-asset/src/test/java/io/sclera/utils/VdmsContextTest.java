package io.sclera.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class VdmsContextTest {

    @Test
    void empty_hasVdms_false() {
        VdmsContext ctx = new VdmsContext();
        assertThat(ctx.hasVdms()).isFalse();
        assertThat(ctx.getVdmsId()).isNull();
    }

    @Test
    void setVdmsId_thenHasVdms_true() {
        VdmsContext ctx = new VdmsContext();
        ctx.setVdmsId("VDMS760");
        assertThat(ctx.hasVdms()).isTrue();
        assertThat(ctx.getVdmsId()).isEqualTo("VDMS760");
    }

    @Test
    void blankVdmsId_hasVdms_false() {
        VdmsContext ctx = new VdmsContext();
        ctx.setVdmsId("  ");
        assertThat(ctx.hasVdms()).isFalse();
    }
}
