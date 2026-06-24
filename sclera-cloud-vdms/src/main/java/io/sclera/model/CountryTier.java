package io.sclera.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "country_tier")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountryTier {

    @Id
    private String country;

    @Column(nullable = false)
    private String tier;

}
