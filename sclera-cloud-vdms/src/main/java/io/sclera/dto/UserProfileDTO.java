package io.sclera.dto;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserProfileDTO {

    private String id;
    private String name;
    private String privileges;
    private String description;
    private String org_id;

}
