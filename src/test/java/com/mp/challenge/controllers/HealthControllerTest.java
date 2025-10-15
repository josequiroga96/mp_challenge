package com.mp.challenge.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mp.challenge.dtos.HealthResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void healthCheck_ShouldReturnConsistentResponse_WhenCalledMultipleTimes() throws Exception {
        MvcResult result1 = mockMvc.perform(get("/public/health"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result2 = mockMvc.perform(get("/public/health"))
                .andExpect(status().isOk())
                .andReturn();

        String json1 = result1.getResponse().getContentAsString();
        String json2 = result2.getResponse().getContentAsString();

        HealthResponseDto response1 = objectMapper.readValue(json1, HealthResponseDto.class);
        HealthResponseDto response2 = objectMapper.readValue(json2, HealthResponseDto.class);

        Assertions.assertEquals(response1.getStatus(), response2.getStatus());
        Assertions.assertEquals(response1.getMessage(), response2.getMessage());
        Assertions.assertEquals(response1.getService(), response2.getService());
        Assertions.assertNotEquals(response1.getTimestamp(), response2.getTimestamp());
    }
}
