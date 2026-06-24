package io.sclera.repository;

import io.sclera.model.VdmsCoordinates;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface VdmsCoordinatesRepository extends JpaRepository<VdmsCoordinates,String> {

    @Modifying
    @Transactional
    @Query(value="INSERT into vdms_coordinates(id,coordinates) values (?1,St_GeomFromGeoJSON(?2,2,4326))",nativeQuery = true)
    void addVdmsCoordinates(String coordinatsId,String coordinates);


    @Query(value="SELECT ST_AsGeoJson(coordinates) from vdms_coordinates where id=?1",nativeQuery = true)
    String getCoordinatesById(String coordinatesId);


    @Query(value = "SELECT ST_Contains(ST_GeomFromGeoJSON(?3), ST_GeomFromText(CONCAT('POINT(', ?1, ' ', ?2, ')'), 4326))",
            nativeQuery = true)
    Long checkIsPresent(float lat,float lng,String coordinates);

    @Modifying
    @Transactional
    @Query(value = "UPDATE vdms_coordinates SET coordinates = ST_GeomFromGeoJSON(?2, 2, 4326) WHERE id = ?1", nativeQuery = true)
    void updateVdmsCoordinates(String coordinatesId, String coordinates);


    @Query(value="SELECT ST_AsGeoJSON(vc.coordinates) AS coordinates " +
            "FROM vdms v " +
            "LEFT JOIN vdms_coordinates vc ON v.vdms_coordinates_id = vc.id " +
            "WHERE v.id = ?1 ",nativeQuery = true)
    String getVdmsCoordinatesByVdmsId(String vdmsId);

    @Modifying
    @Transactional
    @Query(value = "DELETE from vdms_coordinates WHERE id = ?1", nativeQuery = true)
    void deleteVdmsCoordinatesById(String id);
}
