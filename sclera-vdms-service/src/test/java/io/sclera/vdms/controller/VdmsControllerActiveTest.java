package io.sclera.vdms.controller;

import io.sclera.vdms.model.Vdms;
import io.sclera.vdms.repository.UserActionLogRepository;
import io.sclera.vdms.repository.VdmsJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VdmsControllerActiveTest {

    @Mock VdmsJpaRepository repo;
    @Mock UserActionLogRepository auditRepo;

    @Test
    void activeReturnsIdAndTimezonePerActiveVdms() {
        Vdms v = new Vdms();
        v.setId("vdms-1");
        v.setTimezone("America/New_York");
        when(repo.findAllActive()).thenReturn(List.of(v));

        List<Map<String, String>> out = new VdmsController(repo, auditRepo).getActive();

        assertThat(out).hasSize(1);
        assertThat(out.get(0)).containsEntry("vdmsId", "vdms-1")
                              .containsEntry("timezone", "America/New_York");
    }
}
