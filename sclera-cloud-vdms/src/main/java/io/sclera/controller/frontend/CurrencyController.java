package io.sclera.controller.frontend;

import io.sclera.service.CurrencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @GetMapping(value = "/getAllCurrency")
    public ResponseEntity<?> getAllCurrency( @RequestParam(required = false, defaultValue = "all") String key,
                                             @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                             @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int pageSize,
                                             @RequestParam(name = "loggedInUser") String loggedInUser,
                                            HttpServletRequest httpServletRequest) {
        return currencyService.getAllCurrency(key, pageNo, pageSize, loggedInUser, httpServletRequest);
    }
}
