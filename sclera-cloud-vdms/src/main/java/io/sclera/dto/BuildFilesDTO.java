package io.sclera.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BuildFilesDTO {

    private String id;
    private String version;
    private String link;
    private String type;

}
