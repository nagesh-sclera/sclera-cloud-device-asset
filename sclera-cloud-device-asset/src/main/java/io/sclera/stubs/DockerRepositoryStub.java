package io.sclera.stubs;

import io.sclera.Repository.DockerRepository;
import io.sclera.dto.touchscreen.settings.DockerDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DockerRepositoryStub implements DockerRepository {

    @Override
    public List<DockerDTO> getAllNetworksByNetworkOrigin(Integer networkOrigin) {
        return Collections.emptyList();
    }
}
