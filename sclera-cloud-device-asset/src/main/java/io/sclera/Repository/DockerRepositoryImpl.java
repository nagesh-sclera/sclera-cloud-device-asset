package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.DockerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@Primary
public class DockerRepositoryImpl implements DockerRepository {

    private static final Logger log = LoggerFactory.getLogger(DockerRepositoryImpl.class);

    @Override
    public List<DockerDTO> getAllNetworksByNetworkOrigin(Integer networkOrigin) {
        log.warn("DockerRepositoryImpl.getAllNetworksByNetworkOrigin called (stub) networkOrigin={}", networkOrigin);
        return Collections.emptyList();
    }
}
