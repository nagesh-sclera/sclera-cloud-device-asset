package io.sclera.workorder.entity;

import jakarta.persistence.*;
import java.math.BigInteger;

/**
 * Ticket-history persistence entity.
 *
 * <p>The former {@code @SqlResultSetMapping}/{@code @NamedNativeQuery} definitions
 * have been moved into {@code TicketHistoryRepository} and converted to JPQL
 * constructor projections paged via {@code Pageable}. This class now carries only
 * mapping metadata.
 */
@Entity
@Table(name = "ticket_history", indexes = {
        @Index(name = "idx_ticket_history_ticket", columnList = "ticket_id")
})
public class TicketHistory {

    @Id
    private String id;

    @Column(columnDefinition = "TEXT")
    private String message;

    private BigInteger timestamp;

    @Column(length = 32)
    private String status;

    private String action_message;

    @Column(length = 128)
    private String ticket_number;

    @Column(length = 255)
    private String created_by;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    /** Required no-arg constructor for JPA. */
    protected TicketHistory() {
    }

    /**
     * Full constructor used by the ORM insert (replaces the former native
     * {@code INSERT INTO ticket_history(...) VALUES(...)}). The {@code ticket}
     * argument is typically a lazy reference ({@code getReferenceById}) so only the
     * {@code ticket_id} foreign key is written — no extra SELECT — matching the
     * former native insert exactly.
     */
    public TicketHistory(String id, String message, BigInteger timestamp, String status,
                         String action_message, String ticket_number, String created_by, Ticket ticket) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
        this.action_message = action_message;
        this.ticket_number = ticket_number;
        this.created_by = created_by;
        this.ticket = ticket;
    }

}
