package com.example.undoschool_assignment_backend.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.example.undoschool_assignment_backend.document.CourseDocument;
import com.example.undoschool_assignment_backend.service.CourseService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataLoader {

    private final CourseService courseService;
    private final ObjectMapper objectMapper;
    private final ElasticsearchClient elasticsearchClient;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() throws IOException {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(e -> e.index("courses"))
                    .value();

            if (!exists) {
                log.info("Creating 'courses' index with suggest mapping...");
                elasticsearchClient.indices().create(c -> c
                        .index("courses")
                        .mappings(m -> m
                                .properties("title", p -> p.text(t -> t))
                                .properties("description", p -> p.text(t -> t))
                                .properties("category", p -> p.keyword(k -> k))
                                .properties("type", p -> p.keyword(k -> k))
                                .properties("gradeRange", p -> p.keyword(k -> k))
                                .properties("minAge", p -> p.integer(i -> i))
                                .properties("maxAge", p -> p.integer(i -> i))
                                .properties("price", p -> p.double_(d -> d))
                                .properties("nextSessionDate", p -> p.date(d -> d))
                                .properties("suggest", p -> p.completion(cp -> cp))
                        )
                );
            }

            log.info("Loading course data from sample-courses.json...");
            try (InputStream inputStream = new ClassPathResource("sample-courses.json").getInputStream()) {
                List<CourseDocument> courses = objectMapper.readValue(
                        inputStream,
                        new TypeReference<>() {}
                );

                log.info("Successfully deserialized {} courses", courses.size());
                courses.forEach(CourseDocument::buildSuggest);
                courseService.bulkIndex(courses);
                log.info("Data loading complete");
            }

        } catch (IOException e) {
            log.error("Failed to load or index course data", e);
            throw e;
        }
    }
}