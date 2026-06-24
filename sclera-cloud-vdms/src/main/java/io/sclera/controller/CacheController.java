package io.sclera.controller;

import io.sclera.cache.CacheService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cache")
public class CacheController {
    @Autowired
    private CacheService cacheService;

    @DeleteMapping(value = "/deleteAllCache")
    public ResponseEntity<?>deleteCache(HttpServletRequest httpServletRequest){
        return cacheService.deleteAllCache(httpServletRequest);
    }
}
