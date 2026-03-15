package ru.rbpo.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenLogin() throws Exception {
        String unique = "testuser_" + System.currentTimeMillis();
        String body = objectMapper.writeValueAsString(Map.of(
                "username", unique,
                "password", "Test123!@#",
                "email", unique + "@example.com",
                "firstName", "Test",
                "lastName", "User"
        ));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        String loginBody = objectMapper.writeValueAsString(Map.of("username", unique, "password", "Test123!@#"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk());
    }

    @Test
    void registerValidationFails() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "username", "ab",
                "password", "short",
                "email", "not-email",
                "firstName", "A",
                "lastName", "B"
        ));
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
