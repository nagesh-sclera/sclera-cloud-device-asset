package io.sclera.util;


import io.sclera.dto.ResponseDTO;
import io.sclera.integration.dto.IntegrationResponseDTO;

import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScleraUtils {

    public static int calculateOffset(int pageNo ,int pageSize){
        return pageSize * (pageNo - 1);
    }


    public static <T> ResponseDTO generatePayload(T payload, Integer status, boolean success) {
        return new ResponseDTO(payload, status, success, BigInteger.valueOf(System.currentTimeMillis()));
    }



    public static <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> holder = new ArrayList<>();
        for (int i = 0; i < list.size(); i = i + batchSize) {
            if (i + batchSize > list.size()) {
                List<T> assetList = list.subList(i, list.size());
                holder.add(assetList);
                break;
            }
            List<T> assetList = list.subList(i, i + batchSize);
            holder.add(assetList);
        }
        return holder;
    }

    public static <T> IntegrationResponseDTO generateIntegrationPayload(T payload, Integer status, boolean success) {
        return IntegrationResponseDTO.builder()
                .data(payload)
                .errorCode(status)
                .success(success)
                .timestamp(String.valueOf(System.currentTimeMillis()))
                .build();
    }


}
