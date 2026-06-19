package io.sclera.workorder.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void vdmsNotFoundException_storesVdmsId() {
        VdmsNotFoundException ex = new VdmsNotFoundException("v-42");

        assertThat(ex.getVdmsId()).isEqualTo("v-42");
        assertThat(ex.getMessage()).contains("v-42");
    }
}
