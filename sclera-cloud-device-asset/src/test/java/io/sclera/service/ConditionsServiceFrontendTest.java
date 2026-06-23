package io.sclera.service;

import io.sclera.Repository.ConditionsRepository;
import io.sclera.dto.ConditionsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Coverage for ConditionsService.getConditionsFrontend - maps the protocol group + primary/secondary
 * ids to the repository condition lookup, returning null for an unrecognised group.
 */
@ExtendWith(MockitoExtension.class)
class ConditionsServiceFrontendTest {

    @Mock ConditionsRepository conditionsRepository;

    @InjectMocks ConditionsService service;

    @Test
    void getConditionsFrontend_unrecognisedGroup_returnsNull() {
        assertThat(service.getConditionsFrontend("u", "v", "d", "no_such_group", "id1", "sub1")).isNull();
    }

    @Test
    void getConditionsFrontend_validGroup_returnsConditions() {
        when(conditionsRepository.getConditions(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Set.of(mock(ConditionsDTO.class)));

        assertThat(service.getConditionsFrontend("u", "v", "d", "measuring_instrument", "mi1", null))
                .hasSize(1);
    }
}
