package io.sclera.dto.projection;

import io.sclera.models.Device;

/**
 * Projection for the single-device read ({@code getDeviceByDeviceId}): the managed {@link Device}
 * entity plus the cross-table scalars the DeviceDTO needs — association-FK ids the entity hides
 * behind @ManyToOne/@OneToOne, and the joined location/floor/building/onboard/inventory values.
 * Mapped to {@code DeviceDTO} by {@code DeviceDtoMapper}. Component order matches the JPQL SELECT.
 */
public record DeviceDetailRow(
        Device device,
        String dockerName,          // docker.name           -> docker_name
        String vdmsId,              // docker.vdms.id         -> vdms_id
        String location,            // location.name          -> location
        String locationId,          // location.id            -> location_id
        String floor,               // floor.name             -> floor
        String floorId,             // floor.id               -> floor_id
        String building,            // building.name          -> building
        String buildingId,          // building.id            -> building_id
        String localVendorId,       // local_vendor.id        -> local_vendor_id
        String globalVendorId,      // global_vendor.id       -> global_vendor_id
        String otherVendor1Id,      // other_vendor_1.id      -> other_vendor_1_id
        String otherVendor2Id,      // other_vendor_2.id      -> other_vendor_2_id
        String otherVendor3Id,      // other_vendor_3.id      -> other_vendor_3_id
        String assignedUserEmail,   // user.email             -> assigned_user_email
        String onboardStatusId,     // device_onboard_status.id            -> device_onboard_status_id
        String assigneeEmail,       // device_onboard_status.assignee_email -> assignee_email
        Integer imageStatus,        // device_onboard_status.image_status   -> image_status
        Integer geolocationStatus,  // device_onboard_status.geolocation_status -> geolocation_status
        Integer tagStatus,          // device_onboard_status.tag_status     -> tag_status
        Integer fieldStatus,        // device_onboard_status.field_status   -> field_status
        String inventoryTrackingId  // inventory_device.tracking_id         -> inventory_tracking_id
) {}
