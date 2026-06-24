package io.sclera.repository;

import com.alibaba.fastjson2.JSONArray;
import io.sclera.dto.UserActivityDTO;
import io.sclera.model.UserActivityLog;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivityLog, String> {


    @Query(nativeQuery = true)
    List<UserActivityDTO> getUserActivityLog(String email, String type, String action, String status, String vdmsid, String subType, BigInteger startDate, BigInteger endDate, String key, int pageSize, int offset);

    @Query(nativeQuery = true)
    List<UserActivityDTO> getAllQrCodeAndNfcData(JSONArray typeValue, BigInteger startDate, BigInteger endDate, String vdmsId);

    @Query(nativeQuery = true)
    UserActivityDTO getQrCodeCount(String email, String type, String action, BigInteger startDate,
                                   BigInteger endDate, String status, String vdmsId, String key,String subType);


    @Query(nativeQuery = true)
    UserActivityDTO getNfcCount(String email, String type, String action, BigInteger startDate,
                                BigInteger endDate, String status, String vdmsId, String key,String subType);


    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_activity_log (id, email, type, sub_type, action, status, message, primary_id, vdms_id, created_timestamp) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)", nativeQuery = true)
    void addUserActivityLogs(String id, String email, String type, String subType, String action, String status,
                                String message, String primaryId, String vdmsId, Long createdTimestamp);
}
