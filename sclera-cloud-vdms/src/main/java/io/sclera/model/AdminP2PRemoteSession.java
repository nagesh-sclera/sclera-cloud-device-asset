package io.sclera.model;

import io.sclera.dto.P2PRemoteSessionDTO;
import io.sclera.model.compositeclass.AdminP2PRemoteSessionIds;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@IdClass(AdminP2PRemoteSessionIds.class)

@SqlResultSetMapping(
        name = "adminP2PRemoteSessionMapping",
        classes = {
                @ConstructorResult(
                        targetClass = P2PRemoteSessionDTO.class,
                        columns = {
                                @ColumnResult(name = "admin_email" , type = String.class),
                                @ColumnResult(name = "session_id" , type = Integer.class),
                                @ColumnResult(name = "port" , type = Integer.class),
                                @ColumnResult(name = "devuid" , type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "AdminP2PRemoteSession.getPortAndSessionIdByAdminEmailAndDevUID",
        query = "SELECT admin_email ,session_id ,port ,devuid FROM adminp2premote_session  WHERE admin_email = ?1 AND devuid = ?2",
        resultSetMapping = "adminP2PRemoteSessionMapping"
)

@Getter
@Setter
public class AdminP2PRemoteSession {

    @Id
    private String devuid;

    @Id
    private String admin_email;

    private Integer session_id;
    private Integer port;
}
