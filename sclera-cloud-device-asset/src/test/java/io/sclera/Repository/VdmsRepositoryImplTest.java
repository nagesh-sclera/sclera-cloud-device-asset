package io.sclera.Repository;

import io.sclera.models.Vdms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VdmsRepositoryImplTest {

    @Mock VdmsJpaRepository jpa;
    @InjectMocks VdmsRepositoryImpl repo;

    @Test
    void findSingleVdmsId_returnsId_whenExactlyOne() {
        when(jpa.findAllIds()).thenReturn(List.of("VDMS760"));
        assertThat(repo.findSingleVdmsId()).isEqualTo("VDMS760");
    }

    @Test
    void findSingleVdmsId_null_whenMany() {
        when(jpa.findAllIds()).thenReturn(List.of("A", "B"));
        assertThat(repo.findSingleVdmsId()).isNull();
    }

    @Test
    void findSingleVdmsId_null_whenNone() {
        when(jpa.findAllIds()).thenReturn(List.of());
        assertThat(repo.findSingleVdmsId()).isNull();
    }

    @Test
    void getVDMSPasswordById_returnsRowPassword() {
        Vdms v = new Vdms();
        v.setId("A");
        v.setPassword("secret");
        when(jpa.findById("A")).thenReturn(Optional.of(v));
        assertThat(repo.getVDMSPassword("A")).isEqualTo("secret");
    }

    @Test
    void getIsMasterById_returnsRowFlag() {
        Vdms v = new Vdms();
        v.setId("A");
        v.setIs_master(1);
        when(jpa.findById("A")).thenReturn(Optional.of(v));
        assertThat(repo.getIsMaster("A")).isEqualTo(1);
    }
}
