package com.mp.challenge.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthCheck_ShouldReturnOkStatus_WhenCalled() throws Exception {
        mockMvc.perform(get("/public/health"))
                .andExpect(status().isOk());
    }

    @Test
    void healthCheck_ShouldHaveCorrectJsonStructure_WhenCalled() throws Exception {
        mockMvc.perform(get("/public/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.service").exists())
                .andExpect(jsonPath("$.invalidField").doesNotExist());
    }
}
