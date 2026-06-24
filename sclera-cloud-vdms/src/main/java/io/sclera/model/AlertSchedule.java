package io.sclera.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.AlertScheduleDTO;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class , property = "id" , scope = AlertSchedule.class)

@SqlResultSetMapping(
        name = "alertScheduleMuteMapping",
        classes = {
                @ConstructorResult(
                        targetClass = AlertScheduleDTO.class,
                        columns = {
                                @ColumnResult(name = "email_mute", type = Boolean.class),
                                @ColumnResult(name = "sms_mute", type = Boolean.class)
                        }
                )
        }
)

@SqlResultSetMapping(
        name = "alertScheduleMapping",
        classes = {
                @ConstructorResult(
                        targetClass = AlertScheduleDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "schedule", type = String.class),
                                @ColumnResult(name = "email_mute", type = Boolean.class),
                                @ColumnResult(name = "sms_mute", type = Boolean.class),
                                @ColumnResult(name = "email", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "AlertSchedule.getEmailAndSmsMuteByEmail",
        query = "SELECT email_mute ,sms_mute from alert_schedule where email = ?1",
        resultSetMapping = "alertScheduleMuteMapping"
)

@NamedNativeQuery(
        name = "AlertSchedule.getAlertScheduleByEmail",
        query = "SELECT id ,schedule, email_mute ,sms_mute ,email from alert_schedule where email = ?1",
        resultSetMapping = "alertScheduleMapping"
)
@NamedNativeQuery(
        name = "AlertSchedule.getAllAlertScheduler",
        query = "SELECT id ,schedule, email_mute ,sms_mute ,email from alert_schedule ",
        resultSetMapping = "alertScheduleMapping"
)

@Getter
@Setter
public class AlertSchedule {

    @Id
    @Column(length = 64)
    private String id;

    @Column(columnDefinition = "json")
    private String schedule;

    @Column(columnDefinition = "boolean default false")
    private Boolean emailMute;

    @Column(columnDefinition = "boolean default false")
    private Boolean smsMute;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(referencedColumnName = "email", name = "email")
    private User user;
}
