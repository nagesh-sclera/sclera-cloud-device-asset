package io.sclera.cache;

import com.alibaba.fastjson2.JSONObject;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.exception.ServerException;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.cors.CorsConfiguration;

@Service
@Slf4j
public class CacheService {

  private final CacheManager cacheManager;

  @Autowired
  public CacheService(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  public ResponseEntity<?> deleteAllCache(HttpServletRequest httpServletRequest) {
    for (String cacheName : cacheManager.getCacheNames()) {
      Cache cache = cacheManager.getCache(cacheName);
      if (cache != null) {
        cache.clear();
      }
    }
    ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
    log.info(
        "Successfully Deleting All Cached Data From Cache.EndPoint:{}",
        httpServletRequest.getRequestURI());
    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
  }

  public CorsConfiguration getOriginData(String cacheName, String key) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      Cache.ValueWrapper valueWrapper = cache.get(key);
      if (valueWrapper != null) {
        return (CorsConfiguration) valueWrapper.get();
      }
    } else {
      log.error("Cache Not Found.");
    }
    return null;
  }

  public void addOriginToCache(
      String cacheKey,
      String cacheName,
      CorsConfiguration config,
      HttpServletRequest httpServletRequest) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.put(cacheKey, config);
    } else {
      log.error("Cache Not Found.EndPoint:{}", httpServletRequest.getRequestURI());
      throw new ClientException("Cache Not Found", 404, httpServletRequest.getRequestURI());
    }
  }

    public JSONObject getIpApiData(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper valueWrapper = cache.get(key);
            if (valueWrapper != null) {
                return (JSONObject) valueWrapper.get();
            }
        } else {
            log.error("Cache Not Found.");
        }
        return null;
    }

    public void addIpApiData(String cacheName, String key, JSONObject jsonObject) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, jsonObject);
        } else {
            log.error("Cache Not Found.EndPoint:{}", cacheName);
        }
    }
}
