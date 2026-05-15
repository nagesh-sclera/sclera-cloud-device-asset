package io.sclera.service;

import io.sclera.dto.HistoryDTO;
import org.springframework.stereotype.Service;

/** STUB: replace with remote call to AP-C6 */
@Service
public class HistoryService {
    public void insertDeviceStatusHistory(Integer alarm, String ipAddress, Object o, Object o1, String finalDeviceId) {}
    public void addHistory(HistoryDTO historyDTO) {}
    public void addHistoryWithTimestamp(HistoryDTO historyDTO) {}
    public void updateHistoryDeviceId(String oldId, String newId) {}
}
