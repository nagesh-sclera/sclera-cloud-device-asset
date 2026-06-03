package io.sclera.service;

import io.sclera.Repository.SystemInterfaceRepository;
import io.sclera.dto.VlanDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the active delegate methods of SystemInterfaceService. The interface-status
 * and status-list orchestration methods are commented out in the source; the private
 * waitForSlaveGatewayReachable (real ping/sleep) is deferred.
 */
@ExtendWith(MockitoExtension.class)
class SystemInterfaceServiceTest {

    @Mock SystemInterfaceRepository systemInterfaceRepository;

    @InjectMocks SystemInterfaceService service;

    @Test
    void deleteAllInterface_delegates() {
        service.deleteAllInterface();
        verify(systemInterfaceRepository).deleteAllInterface();
    }

    @Test
    void getVlanDiscoverPidByInterfaceName_delegates() {
        VlanDTO vlan = mock(VlanDTO.class);
        when(systemInterfaceRepository.getVlanDiscoverPidByInterfaceName("eth0")).thenReturn(vlan);
        assertThat(service.getVlanDiscoverPidByInterfaceName("eth0")).isSameAs(vlan);
    }

    @Test
    void updateVlanDiscoverPidByInterfaceName_delegates() {
        BigInteger ts = BigInteger.valueOf(123L);
        service.updateVlanDiscoverPidByInterfaceName("pid1", ts, "eth0");
        verify(systemInterfaceRepository).updateVlanDiscoverPidByInterfaceName("pid1", ts, "eth0");
    }
}
