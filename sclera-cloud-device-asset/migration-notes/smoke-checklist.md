# Manual smoke checklist

Run after Phase 3 boot.

- [ ] GET /swagger-ui.html renders AP-C1 endpoints only
- [ ] POST /admin/device returns 201 (or 401 if JWT required) and writes a row when authorised
- [ ] GET /admin/device/{id} returns the created row
- [ ] AssetOnboard flow logs WARN for each stub call
- [ ] SHOW TABLES matches the expected AP-C1 set (device, asset_field, building, floor, location, location_history, device_specification, device_lifecycle_history, device_conditions, managed_software, ai_call_log, device_technician_ai_suggestion, document, media, note, asset_device_mapping, device_onboard_status, device_onboard_status_assignee, device_types, application_user)
- [ ] No `alert_profile`, `ticket`, `inspection_*`, `integration_*`, `inventory_*` tables present