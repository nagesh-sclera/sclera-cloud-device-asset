package io.sclera.service;

import io.sclera.Repository.DeviceRepository;
import io.sclera.Repository.DeviceTypesRepository;
import io.sclera.client.APICallClient;
import io.sclera.dto.DeviceTypesDTO;
import io.sclera.queryrepository.DeviceTypeQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for DeviceTypeService.batchUpdateDeviceTypes, the JDBC bulk-upsert path that the
 * delegate-focused {@link DeviceTypeServiceTest} deferred. Binds id/name/timestamp and executes
 * the trailing partial batch.
 */
@ExtendWith(MockitoExtension.class)
class DeviceTypeServiceBatchTest {

    @Mock DeviceTypesRepository deviceTypesRepository;
    @Mock APICallClient apiCallService;
    @Mock DataSource dataSource;
    @Mock DeviceTypeQueryRepository deviceTypesQueryRepository;
    @Mock DeviceRepository deviceRepository;

    DeviceTypeService service;

    @BeforeEach
    void setUp() {
        // DeviceTypeService has a constructor, so Mockito would use constructor injection only
        // (skipping the @Autowired fields). Wire the package-private fields explicitly instead.
        service = new DeviceTypeService(deviceTypesRepository);
        service.dataSource = dataSource;
        service.deviceTypesQueryRepository = deviceTypesQueryRepository;
        service.apiCallService = apiCallService;
        service.deviceRepository = deviceRepository;
    }

    @Test
    void batchUpdateDeviceTypes_bindsFieldsAndExecutesBatch() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(deviceTypesQueryRepository.getQueryForUpsertDeviceTypesInBatch()).thenReturn("UPSERT SQL");
        when(connection.prepareStatement("UPSERT SQL")).thenReturn(ps);

        DeviceTypesDTO dto = new DeviceTypesDTO();
        dto.setId("t1");
        dto.setName("Camera");
        dto.setUpdatedTimestamp(BigInteger.valueOf(100));

        service.batchUpdateDeviceTypes(Set.of(dto));

        verify(ps).setString(1, "t1");
        verify(ps).setString(2, "Camera");
        verify(ps).setBigDecimal(eq(3), any());
        verify(ps).addBatch();
        verify(ps).executeBatch();   // trailing partial batch (batchCounter > 0)
        verify(ps).close();
        // connection is closed both explicitly and by try-with-resources
        verify(connection, org.mockito.Mockito.atLeastOnce()).close();
    }
}
