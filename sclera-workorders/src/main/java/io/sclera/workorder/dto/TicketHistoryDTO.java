package io.sclera.workorder.dto;

import java.math.BigInteger;

/** A single ticket-history entry (a recorded state/message change for a ticket). */
public class TicketHistoryDTO {

    private String id;

    private String message;

    private BigInteger timestamp;

    private String status;

    private String action_message;

    private String ticket_number;

    private String created_by;

    private String ticket_id;

    private String ticket_name;


    public TicketHistoryDTO(String id, String message, BigInteger timestamp, String status, String action_message, String ticket_number, String created_by, String ticket_id, String ticket_name) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
        this.action_message = action_message;
        this.ticket_number = ticket_number;
        this.created_by = created_by;
        this.ticket_id = ticket_id;
        this.ticket_name = ticket_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAction_message() {
        return action_message;
    }

    public void setAction_message(String action_message) {
        this.action_message = action_message;
    }

    public String getTicket_number() {
        return ticket_number;
    }

    public void setTicket_number(String ticket_number) {
        this.ticket_number = ticket_number;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getTicket_id() {
        return ticket_id;
    }

    public void setTicket_id(String ticket_id) {
        this.ticket_id = ticket_id;
    }

    public String getTicket_name() {
        return ticket_name;
    }

    public void setTicket_name(String ticket_name) {
        this.ticket_name = ticket_name;
    }

    @Override
    public String toString() {
        return "TicketHistoryDTO{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                ", action_message='" + action_message + '\'' +
                ", ticket_number='" + ticket_number + '\'' +
                ", created_by='" + created_by + '\'' +
                ", ticket_id='" + ticket_id + '\'' +
                ", ticket_name='" + ticket_name + '\'' +
                '}';
    }
}
