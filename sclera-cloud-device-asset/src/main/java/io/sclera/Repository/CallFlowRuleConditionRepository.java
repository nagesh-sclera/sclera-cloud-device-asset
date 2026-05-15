package io.sclera.Repository;

import io.sclera.dto.CallFlowRuleConditionDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface CallFlowRuleConditionRepository {

    void upsertCallFlowRuleCondition(String id, String criteria, String actionType,
                                     String actionValue, String actionMessage, String callFlowRuleId);

    List<CallFlowRuleConditionDTO> getCallFlowRuleConditionsByCallFlowRuleId(String callFlowRuleId);

    List<CallFlowRuleConditionDTO> getCallFlowRuleConditionByRuleIdAndCriteria(String ruleId, String criteria);

    void deleteCallFlowRuleConditionById(List<String> ids);
}
