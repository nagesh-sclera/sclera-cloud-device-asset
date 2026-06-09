package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.DockerDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface DockerRepository {

    /**
     * Returns the networks matching the given network origin.
     *
     * @param networkOrigin the network origin to filter by
     * @return the matching network projections
     */
    List<DockerDTO> getAllNetworksByNetworkOrigin(Integer networkOrigin);
}