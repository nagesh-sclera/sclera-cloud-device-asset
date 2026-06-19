package io.sclera.dto.projection;

import io.sclera.models.Device;

/**
 * Projection for the call-status single-device read ({@code getDeviceInfoFromDb}): the managed
 * {@link Device} entity plus the cross-table scalars its native query selected — the docker
 * association FK columns ({@code docker_name}/{@code docker_vdms_id}) and the LEFT-JOINed
 * location/floor/building name+id scalars. Mapped to {@code DeviceDTO} by
 * {@code DeviceInfoDtoMapper}. Component order matches the JPQL SELECT, which mirrors the old
 * {@code devicedetailesforcallstatusmapping} @ConstructorResult column order.
 */
public record DeviceInfoRow(
        Device device,
        String dockerName,    // docker.name        -> docker_name
        String vdmsId,        // docker.vdms.id     -> docker_vdms_id
        String buildingId,    // building.id        -> building_id
        String building,      // building.name      -> building
        String floorId,       // floor.id           -> floor_id
        String floor,         // floor.name         -> floor
        String locationId,    // location.id        -> location_id
        String location       // location.name      -> location
) {}
