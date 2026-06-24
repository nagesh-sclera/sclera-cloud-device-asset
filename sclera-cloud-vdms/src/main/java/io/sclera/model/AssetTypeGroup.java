package io.sclera.model;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Entity
@Getter
@Setter
public class AssetTypeGroup {
    @Id
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "assetTypeGroup")
    private Set<AssetType> assetType;
}
