package io.sclera.models;

import io.sclera.dto.LorawanConfigurationDTO;

import javax.persistence.*;
import java.util.Set;

@Entity
//Shashikala
@SqlResultSetMapping(
        name = "allLorawanConfigurationMapping",
        classes = {
                @ConstructorResult(
                        targetClass = LorawanConfigurationDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "api_key", type = String.class),
                                @ColumnResult(name = "application_id", type = String.class),
                                @ColumnResult(name = "tenant_id", type = String.class),
                                @ColumnResult(name = "vdms_id", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "LorawanConfiguration.getLorawanAllConfigurations",
        query = "SELECT id, name, api_key, application_id, tenant_id, vdms_id FROM lorawan_configuration",
        resultSetMapping = "allLorawanConfigurationMapping"
)
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

//    @ManyToOne
//    @JoinColumn(name = "vdms_id", referencedColumnName = "id")
//    private Vdms vdms_id;

    @ManyToOne(optional = true)  // allow no Vdms reference
    @JoinColumn(name = "vdms_id", referencedColumnName = "id", nullable = true)
    private Vdms vdms_id;


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
    public Vdms getVdms_id() { return vdms_id; }
    public void setVdms_id(Vdms vdms_id) { this.vdms_id = vdms_id; }
    public Set<Lorawan_Sensor> getLorawan_sensors() { return lorawan_sensors; }
    public void setLorawan_sensors(Set<Lorawan_Sensor> lorawan_sensors) { this.lorawan_sensors = lorawan_sensors; }
}
