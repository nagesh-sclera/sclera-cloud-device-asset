package io.sclera.Repository;
import io.sclera.dto.CallFlowRuleConditionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import java.util.Collections;
import java.util.List;
@Repository("callFlowRuleConditionRepositoryImpl")
@Primary
public class CallFlowRuleConditionRepositoryImpl implements CallFlowRuleConditionRepository {
    private static final Logger log = LoggerFactory.getLogger(CallFlowRuleConditionRepositoryImpl.class);
    @Override public void upsertCallFlowRuleCondition(String id, String criteria, String actionType, String actionValue, String actionMessage, String callFlowRuleId) { log.warn("[STUB] CallFlowRuleConditionRepository.upsertCallFlowRuleCondition"); }
    @Override public List<CallFlowRuleConditionDTO> getCallFlowRuleConditionsByCallFlowRuleId(String callFlowRuleId) { log.warn("[STUB] CallFlowRuleConditionRepository.getCallFlowRuleConditionsByCallFlowRuleId"); return Collections.emptyList(); }
    @Override public List<CallFlowRuleConditionDTO> getCallFlowRuleConditionByRuleIdAndCriteria(String ruleId, String criteria) { log.warn("[STUB] CallFlowRuleConditionRepository.getCallFlowRuleConditionByRuleIdAndCriteria"); return Collections.emptyList(); }
    @Override public void deleteCallFlowRuleConditionById(List<String> ids) { log.warn("[STUB] CallFlowRuleConditionRepository.deleteCallFlowRuleConditionById"); }
}
