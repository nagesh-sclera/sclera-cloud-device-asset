package io.sclera.Repository;

import io.sclera.dto.CallFlowRuleDTO;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface CallFlowRuleRepository {

    List<CallFlowRuleDTO> getAllCallFlowRules(Integer offset, Integer pagesize, String searchkey);

    void deleteById(String id);

    String checkCallFlowByDeviceid(String deviceId);

    void upsertAiCallFlow(String id, String name, String createdBy, BigInteger createdAt,
                          String updatedBy, BigInteger updatedAt, String deviceId);

    List<CallFlowRuleDTO> getCallFlowByDeviceId(String deviceId);
}
