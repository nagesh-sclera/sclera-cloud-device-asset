package io.sclera.workorder;

import io.sclera.workorder.config.CacheConfig;
import io.sclera.workorder.dto.MaximoConfigurationDTO;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;

class CacheSerializationTest {

    @Test
    void maximoConfigDto_roundTripsThroughRedisValueSerializer() {
        RedisSerializer<Object> ser = new CacheConfig().redisCacheValueSerializer();
        MaximoConfigurationDTO dto = new MaximoConfigurationDTO();
        dto.setId("cfg-1");

        byte[] bytes = ser.serialize(dto);
        Object back = ser.deserialize(bytes);

        assertThat(back).isInstanceOf(MaximoConfigurationDTO.class);
        assertThat(((MaximoConfigurationDTO) back).getId()).isEqualTo("cfg-1");
    }
}
