package io.sclera.queryrepository;
import org.springframework.stereotype.Component;

@Component
public class MeasuringInstrumentsQueryRepository {
    public String getQueryForUpdateInstrumentValueAndAttributeById() {
        return "UPDATE measuring_instrument SET value = ?, timestamp = ?, attribute = ? WHERE id = ?";
    }
}
