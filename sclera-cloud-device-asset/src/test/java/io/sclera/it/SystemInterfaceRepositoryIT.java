package io.sclera.it;

import io.sclera.Repository.SystemInterfaceRepository;
import io.sclera.dto.DockerInfoDto;
import io.sclera.dto.VlanDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/system-interface-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-system-interface-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class SystemInterfaceRepositoryIT extends PostgresJpaIT {

    @Autowired
    SystemInterfaceRepository repo;

    @PersistenceContext
    EntityManager em;

    @Test
    void contextLoadsAndRepositoryAutowires() {
        assertThat(repo).isNotNull();
        assertThat(repo.count()).isEqualTo(2);
    }

    @Test
    void getInterfaceStatus_returnsStatus() {
        String status = repo.getInterfaceStatus("eth0");
        assertThat(status).isEqualTo("UP");
    }

    @Test
    void getInterfaceStatus_unknownInterface_returnsNull() {
        String status = repo.getInterfaceStatus("nonexistent");
        assertThat(status).isNull();
    }

    @Test
    void getInterfaceStatusList_returnsAllInterfaces() {
        List<DockerInfoDto> list = repo.getInterfaceStatusList();
        assertThat(list).hasSize(2);
        assertThat(list).extracting(DockerInfoDto::getInterface_out)
                .containsExactlyInAnyOrder("eth0", "eth1");
        // verify the status value is carried through
        DockerInfoDto eth0 = list.stream()
                .filter(d -> "eth0".equals(d.getInterface_out()))
                .findFirst().orElseThrow();
        assertThat(eth0.getInterface_status()).isEqualTo("UP");
    }

    @Test
    void getVlanDiscoverPidByInterfaceName_returnsPidAndTimestamp() {
        VlanDTO dto = repo.getVlanDiscoverPidByInterfaceName("eth0");
        assertThat(dto).isNotNull();
        assertThat(dto.getPid()).isEqualTo("pid-111");
        assertThat(dto.getTimestamp()).isEqualTo(BigInteger.valueOf(1700000001000L));
    }

    @Test
    void updateVlanDiscoverPidByInterfaceName_updatesPidAndTimestamp() {
        repo.updateVlanDiscoverPidByInterfaceName("pid-999", BigInteger.valueOf(1700000099000L), "eth0");
        em.flush();
        em.clear();
        // Read back via scalar JPQL to avoid stale L1 cache
        BigInteger ts = (BigInteger) em.createQuery(
                "SELECT si.timestamp FROM System_interface si WHERE si.interface_name = 'eth0'")
                .getSingleResult();
        assertThat(ts).isEqualTo(BigInteger.valueOf(1700000099000L));
        String pid = (String) em.createQuery(
                "SELECT si.pid FROM System_interface si WHERE si.interface_name = 'eth0'")
                .getSingleResult();
        assertThat(pid).isEqualTo("pid-999");
    }

    @Test
    void deleteAllInterface_removesAllRows() {
        repo.deleteAllInterface();
        em.flush();
        em.clear();
        assertThat(repo.count()).isEqualTo(0);
    }
}
