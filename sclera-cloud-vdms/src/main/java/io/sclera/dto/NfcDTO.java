package io.sclera.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NfcDTO {
    private String id;
    private String uid;
    private String locationId;
    private String deviceId;
    private BigInteger creationTime;
    private String createdBy;
    private String vdmsId;

}
