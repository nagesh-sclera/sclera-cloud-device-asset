package io.sclera.service;


import io.sclera.dto.ResponseDTO;
import io.sclera.repository.UserRepository;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoginService {
    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<?> getLanguageByEmail(String email) {
        String language = userRepository.getLanguageByEmail(email);
        log.info("Fetching Language:" + language + " For Email:" + email);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(language, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
