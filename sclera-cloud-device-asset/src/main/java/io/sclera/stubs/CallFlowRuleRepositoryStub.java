package io.sclera.stubs;

import io.sclera.Repository.CallFlowRuleRepository;
import io.sclera.dto.CallFlowRuleDTO;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

@Component
public class CallFlowRuleRepositoryStub implements CallFlowRuleRepository {

    @Override
    public List<CallFlowRuleDTO> getAllCallFlowRules(Integer offset, Integer pagesize, String searchkey) {
        return Collections.emptyList();
    }

    @Override
    public void deleteById(String id) {
    }

    @Override
    public String checkCallFlowByDeviceid(String deviceId) {
        return null;
    }

    @Override
    public void upsertAiCallFlow(String id, String name, String createdBy, BigInteger createdAt,
                                  String updatedBy, BigInteger updatedAt, String deviceId) {
    }

    @Override
    public List<CallFlowRuleDTO> getCallFlowByDeviceId(String deviceId) {
        return Collections.emptyList();
    }
}
