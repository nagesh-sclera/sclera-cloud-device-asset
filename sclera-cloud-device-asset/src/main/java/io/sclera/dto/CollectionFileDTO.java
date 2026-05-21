package io.sclera.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
