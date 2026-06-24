package io.sclera.model;

import io.sclera.dto.VersionDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@SqlResultSetMapping(
        name = "assetVersionMapping",
        classes = {
                @ConstructorResult(
                        targetClass = VersionDTO.class,
                        columns = {
                                @ColumnResult(name = "version", type = String.class),
                                @ColumnResult(name = "link", type = String.class)
                        }
                )
        }
)


@NamedNativeQuery(
        name = "AssetVersion.getAssetVersion",
        query = "SELECT version, link FROM asset_version ",
        resultSetMapping = "assetVersionMapping"
)

@Entity
@Getter
@Setter
public class AssetVersion {

    @Id
    private String version;

    private String link;
}
