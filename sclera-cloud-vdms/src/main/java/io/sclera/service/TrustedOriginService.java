package io.sclera.service;

import io.sclera.cache.CacheService;
import io.sclera.dto.ResponseDTO;
import io.sclera.repository.TrustedOriginRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class TrustedOriginService {

    @Autowired
    private TrustedOriginRepository trustedOriginRepository;
    @Autowired
    private CacheService cacheService;

    public ResponseEntity<?> addOrigin(List<String> origins, HttpServletRequest httpServletRequest) {
        log.info("PayLoad:Origin:{}", origins);
        for (String origin : origins) {
            trustedOriginRepository.addOrigin(origin);
        }
        cacheService.deleteAllCache(httpServletRequest);
        log.info("Added Origins Successfully. Endpoint: {}", httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload("SuccessFully Added Origins", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }



    public List<String> getAllOrigin() {
        return trustedOriginRepository.getAllTrustedOrigin();
    }

    public ResponseEntity<?> getAllOrigins(HttpServletRequest httpServletRequest) {
        List<String> originList = trustedOriginRepository.getAllTrustedOrigin();
        ResponseDTO responseDTO = ScleraUtils.generatePayload(originList, 200, true);
        log.info("Fetching List of Origins. Endpoint: {}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteOrigin(List<String> origins, HttpServletRequest httpServletRequest) {
        log.info("PayLoad:Origin:{}", origins);
        trustedOriginRepository.deleteOrigin(origins);
        cacheService.deleteAllCache(httpServletRequest);
        log.info("Deleted Origins Successfully. Endpoint: {}", httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload("SuccessFully Deleted Origins", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
