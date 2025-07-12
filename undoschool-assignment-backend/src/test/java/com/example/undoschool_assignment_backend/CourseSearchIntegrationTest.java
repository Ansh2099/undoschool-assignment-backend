package com.example.undoschool_assignment_backend;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.example.undoschool_assignment_backend.document.CourseDocument;
import com.example.undoschool_assignment_backend.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
public class CourseSearchIntegrationTest {

    private final MockMvc mockMvc;
    private final ElasticsearchClient client;
    private final CourseService courseService;

    @BeforeEach
    void setupTestIndex() throws Exception {
        boolean exists = client.indices().exists(ExistsRequest.of(e -> e.index("courses"))).value();
        if (exists) {
            client.indices().delete(d -> d.index("courses"));
        }
        client.indices().create(CreateIndexRequest.of(c -> c
                .index("courses")
                .mappings(mb -> mb
                        .properties("suggest", p -> p.completion(cp -> cp))
                )
        ));
        CourseDocument course = new CourseDocument();
        course.setId(UUID.randomUUID().toString());
        course.setTitle("Math Explorers");
        course.setDescription("Fun math activities");
        course.setCategory("Mathematics");
        course.setType("Online");
        course.setGradeRange("Grade 1-3");
        course.setMinAge(6);
        course.setMaxAge(10);
        course.setPrice(100.0);
        course.setNextSessionDate(ZonedDateTime.now().plusDays(5));
        course.buildSuggest();
        courseService.bulkIndex(List.of(course));
        Thread.sleep(800);
    }

    @Test
    void shouldReturnCoursesForSimpleQuery() throws Exception {
        mockMvc.perform(get("/api/search").param("q", "math"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Math Explorers"));
    }

    @Test
    void shouldReturnCoursesMatchingAgeRange() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("minAge", "6")
                        .param("maxAge", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Math Explorers"));
    }

    @Test
    void shouldReturnCoursesWithCategoryFilter() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("category", "Mathematics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].category").value("Mathematics"));
    }

    @Test
    void shouldReturnEmptyWhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/search").param("q", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldSupportPagination() throws Exception {
        mockMvc.perform(get("/api/search")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldReturnSuggestionsForPartialTitle() throws Exception {
        mockMvc.perform(get("/api/search/suggest").param("q", "math"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]").value("Math Explorers"));
    }

    @Test
    void shouldReturnEmptySuggestionsForNoMatch() throws Exception {
        mockMvc.perform(get("/api/search/suggest").param("q", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}