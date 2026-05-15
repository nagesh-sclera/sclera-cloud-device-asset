package io.sclera.stubs;

import io.sclera.Repository.CallFlowRuleConditionRepository;
import io.sclera.dto.CallFlowRuleConditionDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CallFlowRuleConditionRepositoryStub implements CallFlowRuleConditionRepository {

    @Override
    public void upsertCallFlowRuleCondition(String id, String criteria, String actionType,
                                             String actionValue, String actionMessage, String callFlowRuleId) {
    }

    @Override
    public List<CallFlowRuleConditionDTO> getCallFlowRuleConditionsByCallFlowRuleId(String callFlowRuleId) {
        return Collections.emptyList();
    }

    @Override
    public List<CallFlowRuleConditionDTO> getCallFlowRuleConditionByRuleIdAndCriteria(String ruleId, String criteria) {
        return Collections.emptyList();
    }

    @Override
    public void deleteCallFlowRuleConditionById(List<String> ids) {
    }
}
