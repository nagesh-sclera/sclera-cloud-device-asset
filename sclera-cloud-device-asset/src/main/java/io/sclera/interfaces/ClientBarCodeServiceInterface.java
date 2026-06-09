package io.sclera.interfaces;

import com.alibaba.fastjson.JSONArray;
import io.sclera.dto.ClientBarCodeDTO;
import java.util.Set;

/** Service contract for {@link io.sclera.service.ClientBarCodeService}. */
public interface ClientBarCodeServiceInterface {
    Integer getClientBarCodeCountByDeviceId(String deviceId);

    void syncAllClientBarCode(String vdmsId);

    void upsertClientBarCodeInBatch(Set<ClientBarCodeDTO> clientBarCodeDTOS);

    JSONArray getLocationIdsTaggedToClientBarCode(String vdmsid);

    JSONArray getDeviceIdsTaggedToClientBarCode(String vdmsid);

    void syncClientBarCode(String vdmsId);

    Set<ClientBarCodeDTO> getBarCodesByLocationIds(Set<String> locationIds);

    Set<ClientBarCodeDTO> getBarCodesByDeviceIds(Set<String> deviceIds);
}
