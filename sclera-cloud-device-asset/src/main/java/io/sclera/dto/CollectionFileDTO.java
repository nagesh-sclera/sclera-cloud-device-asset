package io.sclera.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a file attachment belonging to a collection, carrying its name and storage URL.
 * Used to transfer collection file references through the asset-management API.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CollectionFileDTO {
    private String id;
    private String fileName;
    private String fileUrl;
    private String collectionId;
}
