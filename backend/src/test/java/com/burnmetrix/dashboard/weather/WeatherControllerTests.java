package com.burnmetrix.dashboard.weather;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.burnmetrix.dashboard.config.SecurityConfig;

@WebMvcTest(WeatherController.class)
@Import({MockWeatherService.class, SecurityConfig.class})
class WeatherControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void currentWeatherReturnsMockConditions() throws Exception {
        mockMvc.perform(get("/api/weather/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("mock"))
                .andExpect(jsonPath("$.condition").value("Clear"));
    }
}
