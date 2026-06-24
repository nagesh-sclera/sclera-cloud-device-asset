package io.sclera.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrencyDTO {

    private String currencyId;
    private String code;
    private String country;

    public CurrencyDTO(String currencyId, String code, String country) {
        this.currencyId = currencyId;
        this.code = code;
        this.country = country;
    }
}
