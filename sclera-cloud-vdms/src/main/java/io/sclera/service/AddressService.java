package io.sclera.service;

import io.sclera.dto.AddressDTO;
import io.sclera.repository.AddressRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class AddressService {

    @Autowired
    private AddressRepository addressrepository;

    public AddressDTO getAddressDetailsById(String id, HttpServletRequest httpServletRequest) {
        log.info("Payload: Id: {}", id);
        log.info("Fetching Address Details By Id:{},EndPoint:{}", id, httpServletRequest.getRequestURI());
        return addressrepository.getAddressDetailsById(id);
    }

    public void addAddress(String id, String address, String city, String country, String state, String zip, HttpServletRequest httpServletRequest) {
        log.info("Payload:Id:{},Address:{},City:{},Country:{},State:{},Zip:{}", id, address, city, country, state, zip);
        log.info("Address Added Successfully.EndPoint:{}", httpServletRequest.getRequestURI());
        addressrepository.addAddress(id, address, city, country, state, zip);
    }

    public void editAddress(String address, String city, String state, String zip, String country, String id, HttpServletRequest httpServletRequest) {
        log.info("Payload:Id:{},Address:{},City:{},Country:{},State:{},Zip:{}", id, address, city, country, state, zip);
        log.info("Address Updated Successfully.EndPoint:{}", httpServletRequest.getRequestURI());
        addressrepository.editAddress(address, city, state, zip, country, id);
    }

    public void deleteAddressById(String id, HttpServletRequest httpServletRequest) {
        log.info("Payload: Id: {}", id);
        log.info("Deleting Address By Id:{}.EndPoint:{}", id, httpServletRequest.getRequestURI());
        addressrepository.deleteAddressById(id);
    }

}
