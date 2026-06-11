package io.sclera.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.*;
import java.math.BigInteger;
import java.util.Set;

/**
 * Represents a floor within a building, including its floor plan image, zoom/positioning metadata, and the
 * locations it contains. Used to organise devices spatially and to render floor plans in the asset-mapping UI.
 *
 * All @NamedNativeQuery / @SqlResultSetMapping entries that previously backed FloorRepository projection
 * methods have been removed — those methods are now JPQL constructor expressions and no longer need
 * named-query metadata.
 */
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Floor.class)
public class Floor {

    @Id
    private String id;

    @Column(length = 128)
    private String name;

    @Column
    private String initial_position;

    @Column
    private String image_url;

    @Column
    private Integer angle;

    @Column(columnDefinition = "text")
    private String path;

    @Column(length = 128)
    private String min_zoom;

    @Column(length = 128)
    private String max_zoom;

    @Column
    private String local_image_url;

    @Column(name = "updated_timestamp", length = 100)
    private BigInteger updatedTimestamp;

    @Column
    @ColumnDefault("'vdms'")
    private String source_type;

    @ManyToOne
    private Building building;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "floor")
    private Set<Location> location;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }


    public Set<Location> getLocation() {
        return location;
    }

    public void setLocation(Set<Location> location) {
        this.location = location;
        location.forEach((temp) -> {
            temp.setFloor(this);
        });
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInitial_position() {
        return initial_position;
    }

    public void setInitial_position(String initial_position) {
        this.initial_position = initial_position;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Integer getAngle() {
        return angle;
    }

    public void setAngle(Integer angle) {
        this.angle = angle;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMin_zoom() {
        return min_zoom;
    }

    public void setMin_zoom(String min_zoom) {
        this.min_zoom = min_zoom;
    }

    public String getMax_zoom() {
        return max_zoom;
    }

    public void setMax_zoom(String max_zoom) {
        this.max_zoom = max_zoom;
    }

    public String getLocal_image_url() {
        return local_image_url;
    }

    public void setLocal_image_url(String local_image_url) {
        this.local_image_url = local_image_url;
    }
}
