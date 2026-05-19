## AP-C6 history/audit

### `io.sclera.service.HistoryService`
File: `src/main/java/io/sclera/service/HistoryService.java`
- `String getDeviceIdByHistoryId(String historyId)` — returns null
- `Integer getHistoryCount(String deviceId)` — returns 0
- `void addHistory(HistoryDTO historyDTO)` — no-op
- `List<UserActionLogDTO> getAuditLogs(String deviceId)` — returns empty list
- `JSONObject getSyslogConfig(String a, String b)` — returns null
