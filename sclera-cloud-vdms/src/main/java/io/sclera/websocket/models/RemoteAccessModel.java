package io.sclera.websocket.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RemoteAccessModel {

    private String vdms_id;
    private Integer remote_access_port;

}
