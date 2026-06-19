package io.sclera.workorder.repository;


import io.sclera.workorder.dto.TicketDTO;
import io.sclera.workorder.entity.Ticket;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {

    /**
     * Add-or-update a ticket via {@code save()} — replaces the former native
     * {@code INSERT … ON CONFLICT (id) DO UPDATE}, with the same per-column rules:
     * <ul>
     *   <li><b>new id</b> → insert all columns (the {@link Ticket} constructor sets
     *       {@code is_deleted = false}, matching the old column default);</li>
     *   <li><b>existing id</b> → overwrite only {@code status, user_message, updated_by,
     *       updated_at, closed_by, closed_at, type, assignee_user_email} (applied as-is,
     *       including null — so unassign/reopen still clear those fields), leaving every
     *       other column at its stored value.</li>
     * </ul>
     * (The former dead {@code upsertTicketByDeviceId} native query was removed — no callers.)
     */
    default void upsertTicketNative(String id, String number, String status, String user_message, String type,
                                    String description, String category, String request_type, String created_by,
                                    BigInteger created_at, String updated_by, BigInteger updated_at, String closed_by,
                                    BigInteger closed_at, String device_id, String assignee_user_email, String name) {
        Ticket existing = findById(id).orElse(null);
        if (existing == null) {
            save(new Ticket(id, number, status, user_message, type, description, category, request_type,
                    created_by, created_at, updated_by, updated_at, closed_by, closed_at, device_id,
                    assignee_user_email, name));
        } else {
            existing.setStatus(status);
            existing.setUser_message(user_message);
            existing.setUpdated_by(updated_by);
            existing.setUpdated_at(updated_at);
            existing.setClosed_by(closed_by);
            existing.setClosed_at(closed_at);
            existing.setType(type);
            existing.setAssignee_user_email(assignee_user_email);
            save(existing);
        }
    }

    @Transactional
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = ?1 AND t.is_deleted = false")
    Integer getTicketStatusCount(String status);

    //get ticket count by device id
    @Transactional
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.device = ?1 AND t.is_deleted = false")
    Integer getTicketCountByDeviceId(String device_id);

    //get open ticket status
    @Transactional
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.device = ?1 AND t.status NOT IN ('closed') AND t.is_deleted = false")
    Integer getOpenTicketStatus(String device_id);


    /**
     * Filtered, paginated ticket list. Converted from the entity's
     * {@code Ticket.getAllNativeTickets} named native query to a JPQL constructor
     * projection. {@code CONCAT_WS('', ...)} (which skips NULLs) is reproduced with
     * {@code CONCAT(COALESCE(...,''))}; {@code LIMIT/OFFSET} is supplied by
     * {@code Pageable}, preserving the original pagination exactly.
     */
    @Query("SELECT new io.sclera.workorder.dto.TicketDTO(" +
            "t.id, t.number, t.status, t.user_message, t.type, t.description, t.category, t.request_type, " +
            "t.created_by, t.created_at, t.updated_by, t.updated_at, t.closed_by, t.closed_at, t.device, " +
            "t.assignee_user_email, t.name) " +
            "FROM Ticket t " +
            "WHERE (:searchkey = 'null' OR CONCAT(CONCAT(COALESCE(t.number, ''), COALESCE(t.user_message, '')), COALESCE(t.description, '')) " +
            "       LIKE CONCAT(CONCAT('%', :searchkey), '%')) " +
            "AND (:category IS NULL OR :category = 'all' OR t.category = :category) " +
            "AND (:status = 'all' OR t.status = :status) " +
            "AND (:userEmail IS NULL OR :userEmail = 'all' OR t.assignee_user_email = :userEmail OR t.created_by = :createdBy) " +
            "AND (:deviceId IS NULL OR :deviceId = 'all' OR t.device = :deviceId) " +
            "AND t.is_deleted = false " +
            "ORDER BY t.created_at DESC")
    List<TicketDTO> getAllNativeTickets(@Param("searchkey") String searchkey,
                                        @Param("category") String category,
                                        @Param("status") String status,
                                        @Param("userEmail") String user_email,
                                        @Param("deviceId") String device_id,
                                        @Param("createdBy") String created_by,
                                        Pageable pageable);

    /**
     * Highest numeric ticket-number suffix for the given prefix. Converted from the
     * former native query
     * {@code SELECT MAX(CASE WHEN SUBSTRING(number FROM LENGTH(?1)+1) ~ '^[0-9]+$'
     * THEN CAST(... AS INTEGER) ELSE 0 END) FROM ticket WHERE number LIKE CONCAT(?1,'%')
     * AND is_deleted = false}. JPQL has no regex operator, so the projection
     * {@link #findTicketNumbersByPrefix} returns the candidate numbers and this
     * {@code default} method reproduces the Postgres semantics exactly in Java:
     * <ul>
     *   <li>a suffix matching {@code ^[0-9]+$} contributes its integer value;</li>
     *   <li>a non-numeric suffix contributes 0 (the former {@code CASE … ELSE 0} branch);</li>
     *   <li>no matching rows → {@code MAX} over the empty set → {@code null}
     *       (the caller maps null → 0).</li>
     * </ul>
     */
    default Integer findMaxTicketNumberByPrefix(String prefix) {
        List<String> numbers = findTicketNumbersByPrefix(prefix);
        if (numbers.isEmpty()) {
            return null;
        }
        int prefixLength = prefix.length();
        int max = 0;
        for (String number : numbers) {
            String suffix = number.substring(prefixLength);
            if (suffix.matches("[0-9]+")) {
                max = Math.max(max, Integer.parseInt(suffix));
            }
        }
        return max;
    }

    /** Candidate ticket numbers for {@link #findMaxTicketNumberByPrefix} — those starting with the prefix and not soft-deleted. */
    @Query("SELECT t.number FROM Ticket t WHERE t.number LIKE CONCAT(?1, '%') AND t.is_deleted = false")
    List<String> findTicketNumbersByPrefix(String prefix);

    /**
     * Filtered, paginated ticket list scoped to a device. Converted from the entity's
     * {@code Ticket.getAllNativeTicketsByDeviceId} named native query to JPQL;
     * {@code LIMIT/OFFSET} is supplied by {@code Pageable}.
     */
    @Query("SELECT new io.sclera.workorder.dto.TicketDTO(" +
            "t.id, t.number, t.status, t.user_message, t.type, t.description, t.category, t.request_type, " +
            "t.created_by, t.created_at, t.updated_by, t.updated_at, t.closed_by, t.closed_at, t.device, " +
            "t.assignee_user_email, t.name) " +
            "FROM Ticket t " +
            "WHERE t.device = :deviceId " +
            "AND (:assigneeUserEmail IS NULL OR :assigneeUserEmail = 'all' OR t.assignee_user_email = :assigneeUserEmail OR t.created_by = :createdBy) " +
            "AND t.is_deleted = false " +
            "ORDER BY t.created_at DESC")
    List<TicketDTO> getAllNativeTicketsByDeviceId(@Param("deviceId") String device_id,
                                                  @Param("assigneeUserEmail") String assignee_user_email,
                                                  @Param("createdBy") String created_by,
                                                  Pageable pageable);

    //get ticket count by status and user
    @Query("SELECT COUNT(t) FROM Ticket t WHERE (?1 = 'all' OR t.status = ?1) " +
            "AND (?2 IS NULL OR ?2 = 'all' OR t.assignee_user_email = ?2 OR t.created_by = ?3) " +
            "AND t.is_deleted = false")
Integer getTicketCountByStatusAndUser(String status, String assignee_user_email, String created_by);


    /**
     * Single ticket by id. Converted from the entity's {@code Ticket.getNativeTicketById}
     * named native query to a JPQL constructor projection (no pagination involved).
     */
    @Query("SELECT new io.sclera.workorder.dto.TicketDTO(" +
            "t.id, t.number, t.status, t.user_message, t.type, t.description, t.category, t.request_type, " +
            "t.created_by, t.created_at, t.updated_by, t.updated_at, t.closed_by, t.closed_at, t.device, " +
            "t.assignee_user_email, t.name) " +
            "FROM Ticket t WHERE t.id = ?1")
    TicketDTO getNativeTicketById(String id);

    @Modifying
    @Transactional
    @Query("UPDATE Ticket t SET t.assignee_user_email = NULL WHERE t.assignee_user_email = ?1")
    void updateTicketAssigneeByUserEmail(String assigneeUserEmail);

    @Modifying
    @Transactional
    @Query("UPDATE Ticket t SET t.is_deleted = true WHERE t.id = ?1")
    void deleteTicketById(String ticket_id);
}
