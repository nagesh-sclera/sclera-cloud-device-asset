package io.sclera.workorder.entity;

import jakarta.persistence.*;

import java.math.BigInteger;
import java.util.Set;

/**
 * Ticket persistence entity.
 *
 * <p>Query logic (the former {@code @SqlResultSetMapping} / {@code @NamedNativeQuery}
 * definitions) has been moved out of this entity into {@code TicketRepository} and
 * converted to JPQL constructor projections; the paginated list queries are paged
 * via {@code Pageable} (which renders the same {@code LIMIT/OFFSET}). This class now
 * carries only mapping metadata.
 *
 * <p>The insert constructor and the small set of setters below back the {@code save()}-based
 * upsert in {@code TicketRepository.upsertTicketNative} (which replaced the native
 * {@code INSERT … ON CONFLICT}). The setters cover exactly the columns the old
 * {@code ON CONFLICT DO UPDATE} clause touched, so update behaviour is identical.
 */
@Entity
@Table(name = "ticket", indexes = {
        @Index(name = "idx_ticket_device", columnList = "device_id"),
        @Index(name = "idx_ticket_status_deleted", columnList = "status, is_deleted"),
        @Index(name = "idx_ticket_assignee", columnList = "assignee_user_email")
})
public class Ticket {

    @Id
    private String id;

    @Column(length = 128)
    private String number;

    @Column(length = 32, columnDefinition = "varchar(255) default 'new'")
    private String status;

    @Column(columnDefinition = "TEXT")
    private String user_message;

    @Column(columnDefinition = "TEXT")
    private String vendor_message;

    @Column(length = 64)
    private String type;

    @Column(length = 64)
    private String response;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 128)
    private String category;

    @Column(length = 128)
    private String request_type;

    private String created_by;

    private BigInteger created_at;

    private String updated_by;

    private BigInteger updated_at;

    private String closed_by;

    private BigInteger closed_at;

    @Column(length = 128)
    private String name;

    @Column(name = "device_id", length = 128)
    private String device;

    @Column(length = 128)
    private String assignee_user_email;

//    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
//    private Set<History> history;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private Set<TicketHistory> ticketHistory;

    @Column(length = 1, columnDefinition = "boolean default false")
    private Boolean is_deleted;

    /** Required by JPA. */
    protected Ticket() {
    }

    /**
     * Insert constructor — mirrors the column list of the former native upsert INSERT
     * (same 17 parameters, same order). {@code is_deleted} is initialised to {@code false}
     * to match the column default the native INSERT relied on (it was not in the column
     * list); {@code vendor_message} and {@code response} stay null, also as before.
     */
    public Ticket(String id, String number, String status, String user_message, String type,
                  String description, String category, String request_type, String created_by,
                  BigInteger created_at, String updated_by, BigInteger updated_at, String closed_by,
                  BigInteger closed_at, String device_id, String assignee_user_email, String name) {
        this.id = id;
        this.number = number;
        this.status = status;
        this.user_message = user_message;
        this.type = type;
        this.description = description;
        this.category = category;
        this.request_type = request_type;
        this.created_by = created_by;
        this.created_at = created_at;
        this.updated_by = updated_by;
        this.updated_at = updated_at;
        this.closed_by = closed_by;
        this.closed_at = closed_at;
        this.device = device_id;
        this.assignee_user_email = assignee_user_email;
        this.name = name;
        this.is_deleted = false;
    }

    // Setters for exactly the columns the former ON CONFLICT DO UPDATE clause overwrote
    // (values are applied as-is, including null — e.g. clearing the assignee on unassign).
    public void setStatus(String status) { this.status = status; }
    public void setUser_message(String user_message) { this.user_message = user_message; }
    public void setType(String type) { this.type = type; }
    public void setUpdated_by(String updated_by) { this.updated_by = updated_by; }
    public void setUpdated_at(BigInteger updated_at) { this.updated_at = updated_at; }
    public void setClosed_by(String closed_by) { this.closed_by = closed_by; }
    public void setClosed_at(BigInteger closed_at) { this.closed_at = closed_at; }
    public void setAssignee_user_email(String assignee_user_email) { this.assignee_user_email = assignee_user_email; }
}

