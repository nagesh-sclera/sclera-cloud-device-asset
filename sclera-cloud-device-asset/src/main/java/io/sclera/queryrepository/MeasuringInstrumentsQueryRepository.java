package io.sclera.queryrepository;
import org.springframework.stereotype.Component;

/**
 * Provides SQL statements for persisting and updating measuring instrument records.
 */
@Component
public class MeasuringInstrumentsQueryRepository {
    /**
     * Returns the SQL statement that updates a measuring instrument's value, timestamp, and attribute by id.
     *
     * @return the parameterized update SQL for a measuring instrument
     */
    public String getQueryForUpdateInstrumentValueAndAttributeById() {
        return "UPDATE measuring_instrument SET value = ?, timestamp = ?, attribute = ? WHERE id = ?";
    }
}
