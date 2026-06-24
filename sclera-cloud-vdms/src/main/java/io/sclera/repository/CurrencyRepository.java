package io.sclera.repository;

import io.sclera.dto.CurrencyDTO;
import io.sclera.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {

    @Query(nativeQuery = true)
    List<CurrencyDTO> getCurrency(String key, int pageSize, int offset);


}
