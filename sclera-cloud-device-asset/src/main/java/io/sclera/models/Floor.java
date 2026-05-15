package io.sclera.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.sclera.dto.FloorDTO;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.math.BigInteger;
import java.util.Set;

@SqlResultSetMapping(
        name = "floormapping",
        classes = {
                @ConstructorResult(
                        targetClass = FloorDTO.class,
                        columns = {
                                @ColumnResult(name = "floor_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "initial_position", type = String.class),
                                @ColumnResult(name = "image_url", type = String.class),
                                @ColumnResult(name = "building_id", type = String.class),
                                @ColumnResult(name = "angle", type = Integer.class),
                                @ColumnResult(name = "min_zoom", type = String.class),
                                @ColumnResult(name = "max_zoom", type = String.class),
                                @ColumnResult(name = "local_image_url", type = String.class)
                        }
                )
        }
)

@NamedNativeQuery(
        name = "Floor.getFloorById",
        query = "SELECT f.id AS floor_id , f.name ,f.initial_position ,f.image_url , f.building_id, f.angle, f.min_zoom, f.max_zoom, f.local_image_url  FROM floor f WHERE f.id = ?1 ",
        resultSetMapping = "floormapping"
)

@NamedNativeQuery(
        name = "Floor.getFloorsDetailsByBuildingId",
        query = "SELECT f.id AS floor_id , f.name ,f.initial_position ,f.image_url , f.building_id, f.angle, f.min_zoom, f.max_zoom, f.local_image_url FROM floor f WHERE f.building_id = ?1 ",
        resultSetMapping = "floormapping"
)


@NamedNativeQuery(
        name = "Floor.getBatchFloorsByPagination",
        query = "SELECT f.id AS floor_id , f.name ,f.initial_position ,f.image_url , f.building_id, f.angle, f.min_zoom, f.max_zoom, f.local_image_url  FROM floor f WHERE f.id IN (?1) LIMIT ?2 OFFSET ?3",
        resultSetMapping = "floormapping"
)

@NamedNativeQuery(
        name = "Floor.getFloorIdsByBuildingIds",
        query = "SELECT f.id AS floor_id , f.name ,f.initial_position ,f.image_url , f.building_id, f.angle, f.min_zoom, f.max_zoom, f.local_image_url  FROM floor f WHERE f.building_id IN (?1) LIMIT ?2 OFFSET ?3",
        resultSetMapping = "floormapping"
)


@SqlResultSetMapping(
        name = "floordetailsmapping",
        classes = {
                @ConstructorResult(
                        targetClass = FloorDTO.class,
                        columns = {
                                @ColumnResult(name = "floor_id", type = String.class),
                                @ColumnResult(name = "name", type = String.class)

                        }
                )
        }
)

@NamedNativeQuery(
        name = "Floor.getFloorsByBuildingId",
        query = "SELECT f.id AS floor_id, f.name FROM floor f WHERE ('all' = ?1 OR f.building_id = ?1) ORDER BY f.name, f.id",
        resultSetMapping = "floordetailsmapping"
)


@NamedNativeQuery(
        name = "Floor.getFloor",
        query = "SELECT f.id AS floor_id, f.name  FROM floor f WHERE f.id = ?1 ",
        resultSetMapping = "floordetailsmapping"
)

@NamedNativeQuery(
        name = "Floor.getFloorByLocationId",
        query = "SELECT f.id AS floor_id , f.name FROM floor f LEFT JOIN location l ON l.floor_id = f.id  WHERE l.id = ?1 ",
        resultSetMapping = "floordetailsmapping"
)

@SqlResultSetMapping(
        name = "floordetailsadcmapping",
        classes = {
                @ConstructorResult(
                        targetClass = FloorDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "name", type = String.class),
                                @ColumnResult(name = "buildingId", type = String.class)

                        }
                )
        }
)


@NamedNativeQuery(
        name = "Floor.getFloorsByBuildingIds",
        query = "SELECT f.id AS id, f.name, f.building_id AS buildingId FROM floor f WHERE f.building_id IN ?1 ",
        resultSetMapping = "floordetailsadcmapping"
)

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

    @Column(columnDefinition = "LONGTEXT")
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
