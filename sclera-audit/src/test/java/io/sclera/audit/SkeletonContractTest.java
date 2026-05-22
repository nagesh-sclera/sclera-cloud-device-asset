package io.sclera.audit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SkeletonContractTest {

    @Autowired MockMvc mvc;

    @Test
    void history_updateHistoryDeviceId_returnsOk() throws Exception {
        // Smoke: updateHistoryDeviceId on HistoryController returns 200.
        // void methods return an empty body with 200 OK.
        mvc.perform(post("/history/updateHistoryDeviceId")
                .param("oldId", "old1")
                .param("newId", "new1"))
           .andExpect(status().isOk());
    }
}
