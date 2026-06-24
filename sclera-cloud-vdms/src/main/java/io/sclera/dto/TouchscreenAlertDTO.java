package io.sclera.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TouchscreenAlertDTO {

    // alert profile fields
    private Integer alert_type;
    private Integer email_alert;
    private Integer sms_alert;

    // general fields
    private BigInteger timestamp;

    // vdms data
    private VdmsDTO vdms;

    // sensor data
    private SensorAlertDTO sensor;

    // device data
    private DeviceAlertDTO device;

    // cloud data
    private CloudAlertDTO cloud_alert;

    // user-list data
    private List<UserDTO> profile_users;

    // service data
    private RecordChecklistAlertDTO checklist;

    // service location data
    private LocationAlertDTO location;

    // inspection data
    private InspectionRecordAlertDTO inspection;

    // sensor report data

    private SensorReportDetailsDTO sensor_report_details;

    // vdms backup data
    private VdmsBackupAlertDTO vdms_info;

    // ITAM ticket data
    private TicketAlertDTO ticket;

}
