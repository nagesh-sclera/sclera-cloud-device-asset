package io.sclera.service;

import io.sclera.dto.CurrencyDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.repository.CurrencyRepository;
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
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;

    public ResponseEntity<?> getAllCurrency(String key, int pageNo, int pageSize, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Key:{},PageNo:{},PageSize:{},LoggedInUser:{}", key, pageNo, pageSize, loggedInUser);
        int offset = pageSize * (pageNo - 1);
        List<CurrencyDTO> currency = currencyRepository.getCurrency(key, pageSize, offset);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(currency, 200, true);
        log.info("Successfully Fetching Currency.EndPoint:{}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
