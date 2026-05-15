package io.sclera.stubs;

import io.sclera.Repository.ClientQrCodeRepository;
import org.springframework.stereotype.Component;

@Component
public class ClientQrCodeRepositoryStub implements ClientQrCodeRepository {

    @Override
    public Integer countByDeviceId(String deviceId) {
        return 0;
    }
}
