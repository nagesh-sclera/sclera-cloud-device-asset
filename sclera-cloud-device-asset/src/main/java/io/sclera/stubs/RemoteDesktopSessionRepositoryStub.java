package io.sclera.stubs;

import io.sclera.Repository.RemoteDesktopSessionRepository;
import org.springframework.stereotype.Component;

@Component
public class RemoteDesktopSessionRepositoryStub implements RemoteDesktopSessionRepository {

    @Override
    public void deleteByDeviceId(String deviceId) {
    }
}
