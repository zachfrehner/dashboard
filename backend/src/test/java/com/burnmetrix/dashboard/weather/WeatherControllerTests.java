package com.burnmetrix.dashboard.weather;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.burnmetrix.dashboard.config.SecurityConfig;

@WebMvcTest(WeatherController.class)
@Import({WeatherControllerTests.TestWeatherConfig.class, SecurityConfig.class})
class WeatherControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void currentWeatherReturnsConditions() throws Exception {
        mockMvc.perform(get("/api/weather/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("test"))
                .andExpect(jsonPath("$.condition").value("Clear"));
    }

    @TestConfiguration
    static class TestWeatherConfig {
        @Bean
        WeatherService weatherService() {
            return () -> new WeatherCurrentResponse("test", "Clear", 72.0, 70.0, 32, 8.4, 5, null, "5:42 AM", "8:31 PM");
        }
    }
}
