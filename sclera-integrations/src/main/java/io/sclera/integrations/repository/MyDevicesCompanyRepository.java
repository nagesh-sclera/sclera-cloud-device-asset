package io.sclera.integrations.repository;

import io.sclera.integrations.model.MyDevicesCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyDevicesCompanyRepository extends JpaRepository<MyDevicesCompany, String> {
}