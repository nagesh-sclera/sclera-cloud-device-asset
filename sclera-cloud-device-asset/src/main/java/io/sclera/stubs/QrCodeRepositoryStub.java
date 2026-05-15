package io.sclera.stubs;

import io.sclera.Repository.QrCodeRepository;
import org.springframework.stereotype.Component;

@Component
public class QrCodeRepositoryStub implements QrCodeRepository {

    @Override
    public Integer countByDeviceId(String deviceId) {
        return 0;
    }
}
