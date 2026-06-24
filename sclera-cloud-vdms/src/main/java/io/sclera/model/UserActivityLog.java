package io.sclera.model;


import io.sclera.dto.UserActivityDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Entity


@SqlResultSetMapping(
        name = "qrCodeCountMapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserActivityDTO.class,
                        columns = {
                                @ColumnResult(name = "count", type = Integer.class)
                        }
                )
        }
)
@NamedNativeQuery(
        name = "UserActivityLog.getQrCodeCount",
        query = "SELECT COUNT(DISTINCT u.id) AS count "
                + " FROM user_activity_log u"
                + " WHERE (?1 = 'all' or u.email = ?1) AND (u.type = ?2) AND (?3 = 'all' or u.action = ?3) "
                + " AND (u.created_timestamp BETWEEN ?4 AND ?5 ) AND (?6 = 'all' or u.status = ?6) "
                + " AND (u.vdms_id = ?7)"
                + " AND (?9 = 'all' OR u.sub_type = ?9)"
                + " AND (?8 = 'all' OR REGEXP_REPLACE(CONCAT_WS('', u.type,u.action,u.email,u.message), '[ -.!\\t_+#~`@$%^&*()=;:<>?,/{}|\\\\\\\\ ]', '') LIKE CONCAT('%', ?8, '%'))"
                + " ORDER BY u.created_timestamp DESC ",
        resultSetMapping = "qrCodeCountMapping"
)

@NamedNativeQuery(
        name = "UserActivityLog.getNfcCount",
        query = "SELECT COUNT(DISTINCT u.id) AS count "
                + " FROM user_activity_log u"
                + " WHERE (?1 = 'all' or u.email = ?1) AND"
                + " (u.type = ?2) AND"
                + " (?3 = 'all' or u.action = ?3)"
                + " AND (u.created_timestamp BETWEEN ?4 AND ?5 )"
                + " AND (?6 = 'all' or u.status = ?6)"
                + " AND (u.vdms_id = ?7)"
                + " AND (?9 = 'all' OR u.sub_type = ?9)"
                + " AND (?8 = 'all' OR REGEXP_REPLACE(CONCAT_WS('', u.type,u.action,u.email,u.message), '[ -.!\\t_+#~`@$%^&*()=;:<>?,/{}|\\\\\\\\ ]', '') LIKE CONCAT('%', ?8, '%'))"
                + " ORDER BY u.created_timestamp DESC ",
        resultSetMapping = "qrCodeCountMapping"
)

@SqlResultSetMapping(
        name = "userActivityLogMapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserActivityDTO.class,
                        columns = {
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "sub_type", type = String.class),
                                @ColumnResult(name = "action", type = String.class),
                                @ColumnResult(name = "created_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "message", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "primary_id", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "UserActivityLog.getUserActivityLog",
        query = "SELECT u.email,u.type,u.sub_type,u.action,u.created_timestamp,u.message,u.status,u.primary_id,u.vdms_id " +
                "FROM user_activity_log u " +
                "WHERE " +
                "(?1 = 'all' or u.email = ?1) AND " +
                "(u.type = ?2) AND " +
                "(?3 = 'all' or u.action = ?3) AND " +
                "(?4 = 'all' or u.status = ?4) AND " +
                "(u.vdms_id = ?5) AND " +
                "(?6 = 'all' or u.sub_type = ?6) AND " +
                "(u.created_timestamp BETWEEN ?7 AND ?8) AND " +
                "(?9 = 'all' OR REGEXP_REPLACE(CONCAT_WS('', u.email,u.type,u.action,u.message), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]', '') LIKE CONCAT('%', ?9, '%')) " +
                "ORDER BY " +
                "u.created_timestamp DESC " +
                "LIMIT ?10 OFFSET ?11",
        resultSetMapping = "userActivityLogMapping"
)


@SqlResultSetMapping(
        name = "qrCodeNfcMapping",
        classes = {
                @ConstructorResult(
                        targetClass = UserActivityDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "email", type = String.class),
                                @ColumnResult(name = "type", type = String.class),
                                @ColumnResult(name = "action", type = String.class),
                                @ColumnResult(name = "created_timestamp", type = BigInteger.class),
                                @ColumnResult(name = "message", type = String.class),
                                @ColumnResult(name = "status", type = String.class),
                                @ColumnResult(name = "sub_type", type = String.class)

                        }
                )
        }
)

@NamedNativeQuery(
        name = "UserActivityLog.getAllQrCodeAndNfcData",
        query = "SELECT u.id,u.email, u.type, u.action, u.created_timestamp, u.message, u.status,u.sub_type "
                + " FROM user_activity_log u"
                + " WHERE (u.type IN ?1) "
                + " AND (u.created_timestamp BETWEEN ?2 AND ?3 ) "
                + " AND (u.vdms_id = ?4)",
        resultSetMapping = "qrCodeNfcMapping"
)


@Getter
@Setter
public class UserActivityLog {


    @Id
    private String id;

    @Column(length = 128)
    private String email;

    @Column(length = 128)
    private String type;

    @Column(length = 128)
    private String sub_type;

    @Column(length = 128)
    private String action;
    @Column(length = 128)
    private String status;
    @Column(columnDefinition = "LONGTEXT")
    private String message;
    @Column(length = 128)
    private String primary_id;
    @Column(length = 128)
    private String vdms_id;
    @Column(length = 128)
    private BigInteger created_timestamp;

}
