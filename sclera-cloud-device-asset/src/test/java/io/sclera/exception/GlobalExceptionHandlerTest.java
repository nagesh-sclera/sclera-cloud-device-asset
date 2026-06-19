package io.sclera.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    @RestController
    static class BoomController {
        @GetMapping("/boom") String boom() { throw new RuntimeException("kaboom"); }
        @GetMapping("/bad")  String bad()  { throw new IllegalArgumentException("nope"); }
        @GetMapping("/missing") String missing() { throw new NoSuchElementException("gone"); }
    }

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new BoomController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void genericException_maps500_withResponseDtoShape() throws Exception {
        mvc.perform(get("/boom").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("kaboom"))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void illegalArgument_maps400() throws Exception {
        mvc.perform(get("/bad").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void noSuchElement_maps404() throws Exception {
        mvc.perform(get("/missing").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
