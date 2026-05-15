package io.sclera.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
@Repository
@Primary
public class HistoryRepositoryImpl implements HistoryRepository {
    private static final Logger log = LoggerFactory.getLogger(HistoryRepositoryImpl.class);
    @Override public void deleteByDeviceId(String deviceId) { log.warn("[STUB] HistoryRepository.deleteByDeviceId"); }
}
