package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.ClientBarCodeDTO;
import io.sclera.integration.dto.ResponseDTO;
import org.springframework.http.ResponseEntity;
import java.util.Set;

/** Service contract for {@link io.sclera.service.ClientBarCodeService}. */
public interface ClientBarCodeService {
    Integer getClientBarCodeCountByDeviceId(String deviceId);

    // -------------------------------------------------------------------------
    // Tagging surface (mirrors ClientQrCodeService)
    // -------------------------------------------------------------------------

    ResponseEntity<ResponseDTO> tagClientBarCode(String orgId, String email, ClientBarCodeDTO clientBarCodeDTO, String loggedInUser);

    ResponseEntity<ResponseDTO> getClientBarCodeDetailsByVdmsIdAndDeviceId(String vdmsId, String deviceId);

    ResponseEntity<ResponseDTO> getUnTaggedClientBarCode(String orgId, String email, String vdmsId, String loggedInUser, int pageNo, int pageSize);

    ResponseEntity<ResponseDTO> getAdcCheckByClientBarCodeId(String clientBarCodeId);

    ResponseEntity<ResponseDTO> getClientBarCodeDetailsByClientBarCodeId(String clientBarCodeId);

    void syncAllClientBarCode(String vdmsId);

    void upsertClientBarCodeInBatch(Set<ClientBarCodeDTO> clientBarCodeDTOS);

    JSONArray getLocationIdsTaggedToClientBarCode(String vdmsid);

    JSONArray getDeviceIdsTaggedToClientBarCode(String vdmsid);

    void syncClientBarCode(String vdmsId);

    Set<ClientBarCodeDTO> getBarCodesByLocationIds(Set<String> locationIds);

    Set<ClientBarCodeDTO> getBarCodesByDeviceIds(Set<String> deviceIds);
}
