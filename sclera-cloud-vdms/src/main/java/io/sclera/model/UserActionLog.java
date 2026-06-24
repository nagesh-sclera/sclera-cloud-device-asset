package io.sclera.model;

import io.sclera.dto.UserActionLogDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Entity

@SqlResultSetMapping(
        name = "userActionLogsMapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserActionLogDTO.class,
                        columns = {
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "action", type = String.class),
                                @ColumnResult(name = "created_timestamp", type = Long.class),
                                @ColumnResult(name = "message", type = String.class),
                                @ColumnResult(name = "status", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "UserActionLog.getAllUserActionLog",
        query = "SELECT u.email,u.type,u.action,u.created_timestamp,u.message,u.status  " +
                "FROM user_action_log u  " +
                "WHERE ((?1 = 'all'  OR email = ?1) " +
                "AND (?2 = 'all'  OR status = ?2) " +
                "AND (?3 = 'all'  OR action = ?3) " +
                "AND (?4 = 'all'  OR type = ?4) " +
                "AND (u.created_timestamp BETWEEN ?5 AND ?6 )) " +
                "AND (?7 = 'all' OR REGEXP_REPLACE(CONCAT_WS('', u.email,u.type,u.action,u.message), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]', '') LIKE CONCAT('%', ?7, '%')) "+
                "ORDER BY u.created_timestamp DESC LIMIT ?9 OFFSET ?8",
        resultSetMapping = "userActionLogsMapping"
)


@Getter
@Setter
public class UserActionLog {

    @Id
    private String id;

    @Column(length = 128)
    private String email;

    @Column(length = 128)
    private String type;

    @Column(length = 128)
    private String action;

    @Column(length = 128)
    private BigInteger created_timestamp;

    @Column(columnDefinition = "LONGTEXT")
    private String message;

    @Column(length = 128)
    private String status;
}