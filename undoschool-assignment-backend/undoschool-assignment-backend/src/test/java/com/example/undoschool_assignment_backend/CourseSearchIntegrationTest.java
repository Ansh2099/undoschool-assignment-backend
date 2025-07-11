package com.example.undoschool_assignment_backend;

import  org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CourseSearchIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchCourses_returnsResults() throws Exception {
        mockMvc.perform(get("/api/search")
                .param("q", "math")
                .param("minAge", "6")
                .param("maxAge", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").isNumber())
            .andExpect(jsonPath("$.courses").isArray());
    }

    @Test
    void suggestTitles_returnsSuggestions() throws Exception {
        mockMvc.perform(get("/api/search/suggest")
                .param("q", "math"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").value("Math Explorers"));
    }
} 