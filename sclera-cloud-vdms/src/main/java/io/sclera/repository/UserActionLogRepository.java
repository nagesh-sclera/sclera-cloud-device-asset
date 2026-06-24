package io.sclera.repository;


import com.alibaba.fastjson2.JSONArray;
import io.sclera.dto.UserActionLogDTO;
import io.sclera.model.UserActionLog;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface UserActionLogRepository extends JpaRepository<UserActionLog, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_action_log(id,email,type,action,created_timestamp,message,status) VALUE(?1,?2,?3,?4,?5,?6,?7)", nativeQuery = true)
    void addUserActionLog(String id, String email, String type, String action, Long created_timestamp, String message, String status);

    @Query(nativeQuery = true)
    List<UserActionLogDTO> getAllUserActionLog(String email, String status, String action, String type,
                                               long startDate, long endDate, String key, int offset, int pagesize);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_action_log WHERE email=?1", nativeQuery = true)
    void deleteUserLogsByEmail(String child);

    @Query(value = "SELECT DISTINCT type FROM user_action_log", nativeQuery = true)
    List<String> getAllTypes();


    @Query(value = "SELECT COUNT(*) AS count " +
            "FROM user_action_log u " +
            "WHERE (?1 = 'all'  OR u.email = ?1) " +
            "AND (?2 = 'all'  OR u.status = ?2) " +
            "AND (?3 = 'all'  OR u.action = ?3) " +
            "AND (?4 = 'all'  OR u.type = ?4) " +
            "AND (u.created_timestamp BETWEEN ?5 AND ?6 ) " +
            "AND (?7 = 'all' OR REGEXP_REPLACE(CONCAT_WS('',u.email,u.type,u.action,u.message), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\ ]', '') LIKE CONCAT('%', ?7, '%')) " +
            "ORDER BY u.created_timestamp DESC ",nativeQuery = true)
    Integer getUserActionLogsCount(String email, String status, String action, String type, long start, long end, String key);

    @Query(value = "SELECT COUNT(id) FROM user_action_log WHERE created_timestamp < ?1",nativeQuery = true)
    Integer getCount(long sixMonthsAgo);

    @Query(value = "SELECT COUNT(id) FROM user_action_log",nativeQuery = true)
    Integer getAllCount();


    @Transactional
    @Modifying
    @Query(value = "DELETE FROM user_action_log WHERE created_timestamp < ?1 ORDER BY created_timestamp LIMIT ?2", nativeQuery = true)
    void deleteByLimit(long sixMonthsAgo, int i);
}