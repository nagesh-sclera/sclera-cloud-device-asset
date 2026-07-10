package io.sclera.controller.internal;

import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.SensorAlertDTO;
import io.sclera.service.MeasuringInstrumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MiConditionsBridgeController.class)
class MiConditionsBridgeControllerTest {

    @Autowired MockMvc mvc;
    @MockBean MeasuringInstrumentService measuringInstrumentService;

    @Test
    void alertMessagesByDeviceIds() throws Exception {
        ConditionsDTO c = new ConditionsDTO();
        c.setAlert_message("High temp");
        when(measuringInstrumentService.listMeasuringIntrumentDevicesAlertMessagesByDeviceIds(eq(List.of("dev-1"))))
            .thenReturn(List.of(c));
        mvc.perform(post("/api/v1/device-asset-service/internal/conditions/alertmessages")
                .contentType(MediaType.APPLICATION_JSON).content("[\"dev-1\"]"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].alert_message").value("High temp"));
    }

    @Test
    void alertDetailsByMiId() throws Exception {
        SensorAlertDTO a = new SensorAlertDTO();
        a.setAlert_message("Leak");
        when(measuringInstrumentService.getMeasuringInstrumentAlertDetails(eq("mi-1"))).thenReturn(a);
        mvc.perform(get("/api/v1/device-asset-service/internal/conditions/alertdetails/mi-1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.alert_message").value("Leak"));
    }
}
