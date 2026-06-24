package io.sclera.service;

import io.sclera.Repository.DeviceRepository;
import io.sclera.client.BacnetClient;
import io.sclera.client.CheckListTemplateClient;
import io.sclera.client.DisruptiveClient;
import io.sclera.client.KNXClient;
import io.sclera.client.LorawanClient;
import io.sclera.client.MonnitClient;
import io.sclera.client.MyDevicesClient;
import io.sclera.client.PelicanClient;
import io.sclera.client.SnmpClient;
import io.sclera.client.TicketClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for the DeviceService sensor count/status refresh cluster (lines ~2150-2790): the
 * per-protocol updateDevice*Count / updateDevice*Status methods that resolve a count or alert flag
 * from the relevant sensor client and write it back via the repository. Each method wraps its body
 * in a swallowing try/catch, so the boolean alert flag must be stubbed to reach the repository call.
 *
 * DEFERRED: the bulk of DeviceService (file IO, topology, async/SNMP polling, multi-collaborator
 * orchestration) remains out of scope for this slice.
 */
@ExtendWith(MockitoExtension.class)
class DeviceServiceSensorCountStatusTest {

    @Mock DeviceRepository deviceRepository;
    @Mock SnmpClient snmpService;
    @Mock InterfaceService interfaceService;
    @Mock NotesService notesService;
    @Mock TicketClient ticketService;
    @Mock BacnetClient bacnetService;
    @Mock LorawanClient lorawanService;
    @Mock DisruptiveClient disruptiveService;
    @Mock MyDevicesClient myDevicesService;
    @Mock MonnitClient monnitService;
    @Mock PelicanClient pelicanService;
    @Mock KNXClient knxService;
    @Mock MeasuringInstrumentService measuringInstrumentService;
    @Mock DocumentService documentService;
    @Mock MediaService mediaService;
    @Mock CheckListTemplateClient checkListTemplateService;

    @InjectMocks DeviceService service;

    // ---- count refreshers: stub the client count, verify the repo write -----

    @Test
    void updateDeviceSnmpCount_writesCount() {
        when(snmpService.getSnmpDeviceCountByDeviceAndSnmpConfiguration("d1")).thenReturn(5);
        service.updateDeviceSnmpCount("d1");
        verify(deviceRepository).updateDeviceSnmpCount("d1", 5);
    }

    @Test
    void updateDeviceInterfaceCount_writesCount() {
        when(interfaceService.getInterfaceCountByDevice("d1")).thenReturn(3);
        service.updateDeviceInterfaceCount("d1");
        verify(deviceRepository).updateDeviceInterfaceCount("d1", 3);
    }

    @Test
    void updateDeviceNotesCount_writesCount() {
        when(notesService.getNotesCountByDeviceId("d1")).thenReturn(2);
        service.updateDeviceNotesCount("d1");
        verify(deviceRepository).updateDeviceNotesCount("d1", 2);
    }

    @Test
    void updateDeviceTicketCount_writesCount() {
        when(ticketService.getTicketCountByDeviceId("d1")).thenReturn(7);
        service.updateDeviceTicketCount("d1");
        verify(deviceRepository).updateDeviceTicketCount("d1", 7);
    }

    @Test
    void updateDeviceBacnetCountByDeviceId_writesCount() {
        when(bacnetService.getBacnetObjectCountByDeviceId("d1")).thenReturn(4);
        service.updateDeviceBacnetCountByDeviceId("d1");
        verify(deviceRepository).updateDeviceBacnetCount("d1", 4);
    }

    @Test
    void updateDeviceLorawanCountByDeviceId_writesCount() {
        when(lorawanService.getLorawanSensorCountByDeviceId("d1")).thenReturn(6);
        service.updateDeviceLorawanCountByDeviceId("d1");
        verify(deviceRepository).updateDeviceLorawanCount("d1", 6);
    }

    @Test
    void updateDeviceDisruptiveCountByDeviceId_writesCount() {
        when(disruptiveService.getDisruptiveSensorCountByDeviceId("d1")).thenReturn(8);
        service.updateDeviceDisruptiveCountByDeviceId("d1");
        verify(deviceRepository).updateDeviceDisruptiveCount("d1", 8);
    }

    @Test
    void updateDeviceMyDevicesCountByDeviceId_writesCount() {
        when(myDevicesService.getMyDevicesSensorCountByDeviceId("d1")).thenReturn(1);
        service.updateDeviceMyDevicesCountByDeviceId("d1");
        verify(deviceRepository).updateDeviceMyDevicesCount("d1", 1);
    }

    @Test
    void updateDeviceMonnitCountByDeviceId_writesCount() {
        when(monnitService.getMonnitCountByDeviceId("d1")).thenReturn(9);
        service.updateDeviceMonnitCountByDeviceId("d1");
        verify(deviceRepository).updateDeviceMonnitCount("d1", 9);
    }

    @Test
    void updateDevicePelicanCountByDeviceId_writesCount() {
        when(pelicanService.getPelicanSensorCountByDeviceId("d1")).thenReturn(2);
        service.updateDevicePelicanCountByDeviceId("d1");
        verify(deviceRepository).updateDevicePelicanCount("d1", 2);
    }

    @Test
    void updateDeviceKNXCountByDeviceId_writesCount() {
        when(knxService.getKNXGroupCountByDeviceId("d1")).thenReturn(3);
        service.updateDeviceKNXCountByDeviceId("d1");
        verify(deviceRepository).updateDeviceKNXCount("d1", 3);
    }

    @Test
    void updateDeviceMeasureCountByDeviceId_writesCount() {
        when(measuringInstrumentService.getInstrumentCountByDeviceId("d1")).thenReturn(5);
        service.updateDeviceMeasureCountByDeviceId("d1");
        verify(deviceRepository).updateDeviceMeasureCount("d1", 5);
    }

    @Test
    void updateDeviceDocumentsCountByDeviceId_writesCount() {
        when(documentService.getDocumentsCountByDeviceId("d1")).thenReturn(4);
        service.updateDeviceDocumentsCountByDeviceId("d1");
        verify(deviceRepository).updateDeviceDocumentsCount("d1", 4);
    }

    @Test
    void updateDeviceMediaCountByDeviceId_writesCount() {
        when(mediaService.getMediasCountByDeviceId("d1")).thenReturn(6);
        service.updateDeviceMediaCountByDeviceId("d1");
        verify(deviceRepository).updateDeviceMediaCount("d1", 6);
    }

    @Test
    void updateDeviceCheckListsCountByDeviceId_writesCount() {
        when(checkListTemplateService.getCheckListTemplatesCountByDeviceId("d1")).thenReturn(2);
        service.updateDeviceCheckListsCountByDeviceId("d1");
        verify(deviceRepository).updateDeviceCheckListsCount("d1", 2);
    }

    @Test
    void updateDeviceSnmpObjectCountByDeviceId_writesCount() {
        when(snmpService.getSnmpObjectCountByDeviceId("d1")).thenReturn(3);
        service.updateDeviceSnmpObjectCountByDeviceId("d1");
        verify(deviceRepository).updateDeviceSnmpObjectCount("d1", 3);
    }

    // ---- status refreshers: alert=true -> "alert" written to repo -----------

    @Test
    void updateDeviceSnmpStatusByDeviceId_alert() {
        when(snmpService.getSnmpDeviceAlertStatusByDeviceId("d1")).thenReturn(true);
        service.updateDeviceSnmpStatusByDeviceId("d1");
        verify(deviceRepository).updateDeviceSnmpStatus("d1", "alert");
    }

    @Test
    void updateDeviceBacnetStatusByDeviceId_alert() {
        when(bacnetService.getBacnetObjectAlertStatusByDeviceId("d1")).thenReturn(true);
        service.updateDeviceBacnetStatusByDeviceId("d1");
        verify(deviceRepository).updateDeviceBacnetStatus("d1", "alert");
    }

    @Test
    void updateDeviceLorawanStatusByDeviceId_alert() {
        when(lorawanService.getLorawanSensorAlertStatusByDeviceId("d1")).thenReturn(true);
        service.updateDeviceLorawanStatusByDeviceId("d1");
        verify(deviceRepository).updateDeviceLorawanStatus("d1", "alert");
    }

    @Test
    void updateDeviceDisruptiveStatusByDeviceId_alert() {
        when(disruptiveService.getDisruptiveSensorAlertStatusByDeviceId("d1")).thenReturn(true);
        service.updateDeviceDisruptiveStatusByDeviceId("d1");
        verify(deviceRepository).updateDeviceDisruptiveStatus("d1", "alert");
    }

    @Test
    void updateDeviceMyDevicesStatusByDeviceId_alert() {
        when(myDevicesService.getMyDevicesSensorAlertStatusByDeviceId("d1")).thenReturn(true);
        service.updateDeviceMyDevicesStatusByDeviceId("d1");
        verify(deviceRepository).updateDeviceMyDevicesStatus("d1", "alert");
    }

    @Test
    void updateDeviceMonnitStatusByDeviceId_alert() {
        when(monnitService.getMonnitAlertStatusByDeviceId("d1")).thenReturn(true);
        service.updateDeviceMonnitStatusByDeviceId("d1");
        verify(deviceRepository).updateDeviceMonnitStatus("d1", "alert");
    }

    @Test
    void updateDevicePelicanStatusByDeviceId_alert() {
        when(pelicanService.getPelicanSensorAlertStatusByDeviceId("d1")).thenReturn(true);
        service.updateDevicePelicanStatusByDeviceId("d1");
        verify(deviceRepository).updateDevicePelicanStatus("d1", "alert");
    }

    @Test
    void updateDeviceKNXStatusByDeviceId_alert() {
        when(knxService.getKNXGroupAlertStatusByDeviceId("d1")).thenReturn(true);
        service.updateDeviceKNXStatusByDeviceId("d1");
        verify(deviceRepository).updateDeviceKNXStatus("d1", "alert");
    }

    @Test
    void updateDeviceSnmpObjectStatusByDeviceId_alert() {
        when(snmpService.getSnmpObjectAlertStatusByDeviceId("d1")).thenReturn(true);
        service.updateDeviceSnmpObjectStatusByDeviceId("d1");
        verify(deviceRepository).updateDeviceSnmpObjectStatus("d1", "alert");
    }

    @Test
    void updateDeviceTicketStatus_open() {
        when(ticketService.getOpenTicketStatus("d1")).thenReturn(true);
        service.updateDeviceTicketStatus("d1");
        verify(deviceRepository).updateDeviceTicketStatus("d1", "open");
    }

    // ---- no-alert branch + sensor-id resolver variants ----------------------

    @Test
    void updateDeviceSnmpStatus_resolvesIdAndWritesNoAlert() {
        when(snmpService.getDeviceIdBySnmpDeviceId("s1")).thenReturn("d1");
        when(snmpService.getSnmpDeviceAlertStatusByDeviceId("d1")).thenReturn(false);
        service.updateDeviceSnmpStatus("s1");
        verify(deviceRepository).updateDeviceSnmpStatus("d1", "no-alert");
    }

    @Test
    void updateDeviceBacnetStatus_resolvesIdAndWritesNoAlert() {
        when(bacnetService.getDeviceIdByBacnetObjectId("bd", "bo")).thenReturn("d1");
        when(bacnetService.getBacnetObjectAlertStatusByDeviceId("d1")).thenReturn(false);
        service.updateDeviceBacnetStatus("bd", "bo");
        verify(deviceRepository).updateDeviceBacnetStatus("d1", "no-alert");
    }

    @Test
    void updateDeviceTicketStatus_closed() {
        when(ticketService.getOpenTicketStatus("d1")).thenReturn(false);
        service.updateDeviceTicketStatus("d1");
        verify(deviceRepository).updateDeviceTicketStatus("d1", "closed");
    }
}
