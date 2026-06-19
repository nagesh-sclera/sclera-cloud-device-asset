package io.sclera.dto.projection;

import io.sclera.models.Device;

/**
 * Projection for the IAQ measuring-instrument lookup ({@code getDeviceByMeasuringInstrumentId}):
 * the managed {@link Device} entity plus the two cross-table FK scalars its native query selected
 * as {@code d.docker_name}/{@code d.location_id} — which are the FK columns of the @ManyToOne
 * Docker/Location relations (docker.name / location.id), not scalar @Column fields. Mapped to
 * {@code DeviceDTO} by {@code DeviceByInstrumentDtoMapper}. Component order matches the JPQL SELECT,
 * which mirrors the old {@code deviceforiaqMapping} @ConstructorResult column order.
 */
public record DeviceByInstrumentRow(
        Device device,          // d.id, d.asset_group, d.user_data_name, d.display_name, d.onboard_status
        String dockerName,      // docker.name  -> docker_name
        String locationId       // location.id  -> location_id
) {}
