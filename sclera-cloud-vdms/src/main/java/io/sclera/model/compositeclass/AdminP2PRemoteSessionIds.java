package io.sclera.model.compositeclass;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AdminP2PRemoteSessionIds implements Serializable {

    private static final long serialVersionUID = 1L;
    private String devuid;
    private String admin_email;
}
