package io.sclera.integrations.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.Set;

@Entity
public class LorawanConfiguration {

    @Id
    private String id;

    @Column
    private String name;

    @Column
    private String api_key;

    @Column
    private String application_id;

    @Column
    private String tenant_id;

    // PG-port: scalar FK (Vdms entity lives in cloud-device-asset / vdms-service).
    @Column(name = "vdms_id")
    private String vdms_id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "lorawan_configuration")
    private Set<Lorawan_Sensor> lorawan_sensors;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getApi_key() { return api_key; }
    public void setApi_key(String api_key) { this.api_key = api_key; }
    public String getApplication_id() { return application_id; }
    public void setApplication_id(String application_id) { this.application_id = application_id; }
    public String getTenant_id() { return tenant_id; }
    public void setTenant_id(String tenant_id) { this.tenant_id = tenant_id; }
    public String getVdms_id() { return vdms_id; }
    public void setVdms_id(String vdms_id) { this.vdms_id = vdms_id; }
    public Set<Lorawan_Sensor> getLorawan_sensors() { return lorawan_sensors; }
    public void setLorawan_sensors(Set<Lorawan_Sensor> lorawan_sensors) { this.lorawan_sensors = lorawan_sensors; }
}
