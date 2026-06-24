package io.sclera.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.dto.UserDTO;
import io.sclera.repository.AddressRepository;
import io.sclera.repository.ScleraFXRepository;
import io.sclera.repository.UserRepository;
import io.sclera.service.WebClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Slf4j
public class DbInit {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ScleraFXRepository scleraFXRepository;


    @Autowired
    private WebClientService webClientService;
    @Value("${sclera.super-admin.email}")
    private String email;
    @Value("${sclera.super-admin.password}")
    private String password;
    @PostConstruct
    public void createSuperUser() throws JsonProcessingException {
        Integer count = userRepository.checkSuperAdmin();
        if (count == 0) {
            log.info("No super-admin Found");

            UserDTO userdto = new UserDTO();
            userdto.setEmail(email);
            userdto.setName("Super-Admin");
            userdto.setCompany_name("Access Research Labs");
            userdto.setRole("super-admin");
            userdto.setWebsite("www.accessonline.io");
            userdto.setPassword(password);
            userdto.setActive(1);

            addressRepository.addAddress(userdto.getEmail(), userdto.getAddress(), userdto.getCity(), userdto.getCountry(),
                    userdto.getState(), userdto.getZip());

//            userRepository.createUserByEmail(userdto.getEmail(), userdto.getCompany_name(), userdto.getCreation_timestamp(), userdto.getName(),
//                    userdto.getPhone(), userdto.getPhone_type(), userdto.getValue(), userdto.getWebsite(), userdto.getEmail(),
//                    userdto.getRole(), null, null, "Asia/Kolkata", "EN");
            log.info("Payload: User DTO:{}",userdto);
            webClientService.createSuperAdminInLoginDB(userdto, null,userdto.getEmail());
            log.info("Created super-admin on start up");
        } else {
            log.info("Super-admin is present");
        }
    }


    @PostConstruct
    public void checkScleraFXVersion() {
        Integer db_data = scleraFXRepository.checkScleraFXVersion();
        if (db_data != 3) {
            scleraFXRepository.insertDefaultScleraFXVersions();
        }
    }


}
