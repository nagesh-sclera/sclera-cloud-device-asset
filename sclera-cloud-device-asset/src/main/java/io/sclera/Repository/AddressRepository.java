package io.sclera.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.sclera.models.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

}
