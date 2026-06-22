package io.sclera.service;

import io.sclera.Repository.ClientBarCodeRepository;
import io.sclera.client.APICallClient;
import io.sclera.dto.ClientBarCodeDTO;
import io.sclera.queryrepository.ClientBarCodeQueryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for the heavier ClientBarCodeService paths that the delegate-focused
 * {@link ClientBarCodeServiceTest} deferred: the JDBC batch upsert and the two API-pagination
 * sync orchestrations (full sync with soft-delete bookkeeping, and incremental sync).
 */
@ExtendWith(MockitoExtension.class)
class ClientBarCodeServiceBatchTest {

    @Mock APICallClient apiCallService;
    @Mock DataSource dataSource;
    @Mock ClientBarCodeQueryRepository clientBarCodeQueryRepository;
    @Mock ClientBarCodeRepository clientBarCodeRepository;

    @InjectMocks ClientBarCodeService service;

    private PreparedStatement stubJdbc() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(clientBarCodeQueryRepository.getQueryForUpsertClientBarCode()).thenReturn("UPSERT SQL");
        when(connection.prepareStatement("UPSERT SQL")).thenReturn(ps);
        return ps;
    }

    @Test
    void upsertClientBarCodeInBatch_bindsFieldsAndExecutesBatch() throws Exception {
        PreparedStatement ps = stubJdbc();

        service.upsertClientBarCodeInBatch(Set.of(mock(ClientBarCodeDTO.class)));

        verify(ps).setString(eq(1), any());
        verify(ps).addBatch();
        verify(ps).executeBatch();   // batchCounter > 0 after the loop
        verify(ps).close();
    }

    @Test
    void syncAllClientBarCode_marksUpsertsAndPurges() throws Exception {
        stubJdbc();
        // empty page (< pageSize) -> loop breaks after first fetch
        when(apiCallService.getAllClientBarCodeByVdmsId(eq("v1"), anyInt(), anyInt()))
                .thenReturn(Set.of());

        service.syncAllClientBarCode("v1");

        verify(apiCallService).getAllClientBarCodeByVdmsId("v1", 1, 100);
        verify(clientBarCodeRepository).updateIsDeletedForAllClientBarCode();
        verify(clientBarCodeRepository).deleteOldClientBarCode();
    }

    @Test
    void syncClientBarCode_incrementalUpsertsWithoutPurge() throws Exception {
        stubJdbc();
        when(apiCallService.getSyncedClientBarCodeByVdmsId(eq("v1"), anyInt(), anyInt()))
                .thenReturn(Set.of());

        service.syncClientBarCode("v1");

        verify(apiCallService).getSyncedClientBarCodeByVdmsId("v1", 1, 100);
        // incremental sync does not run the soft-delete bookkeeping
        verify(clientBarCodeRepository, org.mockito.Mockito.never()).deleteOldClientBarCode();
    }
}
