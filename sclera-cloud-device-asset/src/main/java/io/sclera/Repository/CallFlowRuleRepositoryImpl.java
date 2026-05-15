package io.sclera.Repository;
import io.sclera.dto.CallFlowRuleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
@Repository("callFlowRuleRepositoryImpl")
@Primary
public class CallFlowRuleRepositoryImpl implements CallFlowRuleRepository {
    private static final Logger log = LoggerFactory.getLogger(CallFlowRuleRepositoryImpl.class);
    @Override public List<CallFlowRuleDTO> getAllCallFlowRules(Integer offset, Integer pagesize, String searchkey) { log.warn("[STUB] CallFlowRuleRepository.getAllCallFlowRules"); return Collections.emptyList(); }
    @Override public void deleteById(String id) { log.warn("[STUB] CallFlowRuleRepository.deleteById"); }
    @Override public String checkCallFlowByDeviceid(String deviceId) { log.warn("[STUB] CallFlowRuleRepository.checkCallFlowByDeviceid"); return null; }
    @Override public void upsertAiCallFlow(String id, String name, String createdBy, BigInteger createdAt, String updatedBy, BigInteger updatedAt, String deviceId) { log.warn("[STUB] CallFlowRuleRepository.upsertAiCallFlow"); }
    @Override public List<CallFlowRuleDTO> getCallFlowByDeviceId(String deviceId) { log.warn("[STUB] CallFlowRuleRepository.getCallFlowByDeviceId"); return Collections.emptyList(); }
}
