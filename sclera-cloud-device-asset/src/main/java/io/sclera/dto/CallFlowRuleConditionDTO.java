package io.sclera.dto;

/** STUB: belongs to a future microservice */
public class CallFlowRuleConditionDTO {

    private String id;
    private String criteria;
    private String actionType;
    private String actionValue;
    private String actionMessage;
    private String configuration;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCriteria() { return criteria; }
    public void setCriteria(String criteria) { this.criteria = criteria; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getActionValue() { return actionValue; }
    public void setActionValue(String actionValue) { this.actionValue = actionValue; }

    public String getActionMessage() { return actionMessage; }
    public void setActionMessage(String actionMessage) { this.actionMessage = actionMessage; }

    public String getConfiguration() { return configuration; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }
}
