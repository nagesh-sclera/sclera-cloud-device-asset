package io.sclera.integration.service;

import io.sclera.integration.repository.IntegrationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IntegrationUserService {

    @Autowired
    private IntegrationUserRepository integrationUserRepository;

    public String getOrgIdByUsername(String username) {
        return integrationUserRepository.getOrgIdByUsername(username);
    }
}
