package io.sclera.service;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.uuid.Generators;
import io.sclera.cache.CacheService;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsUserActivityDTO;
import io.sclera.model.VdmsUserActivity;
import io.sclera.repository.VdmsUserActivityRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class VdmsUserActivityService {

    private final VdmsUserActivityRepository vdmsUserActivityRepository;
    private final WebClientService webClientService;
    private final CacheService cacheService;

    public VdmsUserActivityService(VdmsUserActivityRepository vdmsUserActivityRepository,
                                   WebClientService webClientService,
                                   CacheService cacheService) {
        this.vdmsUserActivityRepository = vdmsUserActivityRepository;
        this.webClientService = webClientService;
        this.cacheService = cacheService;
    }

    /**
     * Adds a VDMS user activity record.
     *
     * @param vdmsUserActivityDTO the data transfer object containing user activity details
     * @param httpServletRequest
     * @return ResponseEntity with the response status
     */
    public ResponseEntity<ResponseDTO> addVdmsUserActivity(VdmsUserActivityDTO vdmsUserActivityDTO, HttpServletRequest httpServletRequest) {
        try {
            String timezone;
            try {

                // get the IP address from the request header
                String ip = httpServletRequest.getHeader("x-real-ip");
                if (ip == null) {
                    ip = "50.194.123.216";
                }
                // get the timezone from the cache or web client
                JSONObject timeZoneObject = cacheService.getIpApiData("IpApiCache", ip);
                log.info("VDMS User activity: timeZone from cache: {}", timeZoneObject);

                if (timeZoneObject == null || timeZoneObject.getString("timezone") == null) {
                    timeZoneObject = webClientService.sendIpApi(ip);

                    log.info("VDMS User activity: timeZone from IP API: {}", timeZoneObject);

                    // add the timezone data to the cache
                    cacheService.addIpApiData("IpApiCache", ip, timeZoneObject);
                }

                timezone = timeZoneObject.getString("timezone");

                if (timezone == null || timezone.isEmpty()) {
                    log.warn("VDMS User activity: Timezone not found for IP");
                    timezone = "UTC"; // Fallback to UTC if timezone is not found
                }

            } catch (Exception e) {
                log.error("VDMS User activity: Error forming timezone: ", e);
                timezone = "UTC"; // Fallback to UTC if there's an error
            }

            log.info("VDMS User activity: timezone: {}", timezone);

            String userId = vdmsUserActivityDTO.getUser_id();
            String vdmsId = vdmsUserActivityDTO.getVdms_id();
            long currentTimeMillis = System.currentTimeMillis();

            // check if the user activity already exists for the current day for the given user and VDMS ID
            // if it does not exist, create a new record else do nothing
            ZonedDateTime now = Instant.ofEpochMilli(currentTimeMillis).atZone(ZoneId.of(timezone));
            // Start of the day
            ZonedDateTime startOfDay = now.truncatedTo(ChronoUnit.DAYS);
            // End of the day
            ZonedDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

            // To epoch millis
            long startMillis = startOfDay.toInstant().toEpochMilli();
            long endMillis = endOfDay.toInstant().toEpochMilli();

            // check if the user activity already exists for the current day for the given user and VDMS ID
            long isPresent = vdmsUserActivityRepository.existsByUserIdAndVdmsIdAndAccessTimeBetween(userId, vdmsId, startMillis, endMillis);

            // if the user activity does not exist, create a new record
            if (isPresent == 0) {
                // generate a unique ID for the VDMS user activity
                String id = Generators.timeBasedGenerator().generate().toString();

                VdmsUserActivity vdmsUserActivity = VdmsUserActivity.builder()
                        .id(id)
                        .user_id(userId)
                        .vdms_id(vdmsId)
                        .access_time(currentTimeMillis)
                        .build();

                // insert the VDMS user activity into the table
                vdmsUserActivityRepository.save(vdmsUserActivity);

                log.info("VDMS User activity: Added VDMS user activity: {}. id: {}", vdmsUserActivityDTO, id);
            }
        } catch (Exception e) {
            log.error("VDMS User activity: Error adding VDMS user activity: ", e);
        }

        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
