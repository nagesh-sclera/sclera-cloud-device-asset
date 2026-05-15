package io.sclera.models;

import io.sclera.dto.DeviceOnboardStatusDTO;

import javax.persistence.*;
import java.util.Set;

@SqlResultSetMapping(
        name = "deviceonboardstatusmapping",
        classes = {
                @ConstructorResult(
                        targetClass = DeviceOnboardStatusDTO.class,
                        columns = {
                                @ColumnResult(name = "assignee_email", type = String.class),
                                @ColumnResult(name = "image_status", type = Integer.class),
                                @ColumnResult(name = "geolocation_status", type = Integer.class),
                                @ColumnResult(name = "tag_status", type = Integer.class),
                                @ColumnResult(name = "field_status", type = Integer.class),
                                @ColumnResult(name = "onboard_status", type = Integer.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "DeviceOnboardStatus.getOnboardDataByDeviceId",
        query = "SELECT dos.assignee_email, dos.image_status, dos.geolocation_status, dos.tag_status, dos.field_status, d.onboard_status  "
                + " FROM device_onboard_status dos "
                + " LEFT JOIN device d ON d.id = dos.device_id "
                + " WHERE dos.device_id = ?1",
        resultSetMapping = "deviceonboardstatusmapping"
)

@Entity
public class DeviceOnboardStatus {
    @Id
    private String id;

    private String assignee_email;

    @Column(columnDefinition = "integer default 0", length = 10)
    private Integer image_status;

    @Column(columnDefinition = "integer default 0", length = 10)
    private Integer geolocation_status;

    @Column(columnDefinition = "integer default 0", length = 10)
    private Integer tag_status;

    @Column(columnDefinition = "integer default 0", length = 10)
    private Integer field_status;

    @OneToOne
    private Device device;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "device_onboard_status")
    private Set<DeviceOnboardStatusAssignee> device_onboard_status_assignees;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssignee_email() {
        return assignee_email;
    }

    public void setAssignee_email(String assignee_email) {
        this.assignee_email = assignee_email;
    }

    public Integer getImage_status() {
        return image_status;
    }

    public void setImage_status(Integer image_status) {
        this.image_status = image_status;
    }

    public Integer getGeolocation_status() {
        return geolocation_status;
    }

    public void setGeolocation_status(Integer geolocation_status) {
        this.geolocation_status = geolocation_status;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Integer getTag_status() {
        return tag_status;
    }

    public void setTag_status(Integer tag_status) {
        this.tag_status = tag_status;
    }

    public Integer getField_status() {
        return field_status;
    }

    public void setField_status(Integer field_status) {
        this.field_status = field_status;
    }

    public Set<DeviceOnboardStatusAssignee> getDevice_onboard_status_assignees() {
        return device_onboard_status_assignees;
    }

    public void setDevice_onboard_status_assignees(Set<DeviceOnboardStatusAssignee> device_onboard_status_assignees) {
        this.device_onboard_status_assignees = device_onboard_status_assignees;
    }
}
