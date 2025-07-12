package com.example.undoschool_assignment_backend.config;

import com.example.undoschool_assignment_backend.document.CourseDocument;
import com.example.undoschool_assignment_backend.service.CourseService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This component is responsible for loading initial data into Elasticsearch
 * when the Spring Boot application starts up.
 */
@Component
@RequiredArgsConstructor
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final CourseService courseService;
    private final ObjectMapper objectMapper;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() throws IOException {
        try (InputStream inputStream = new ClassPathResource("sample-courses.json").getInputStream()) {
            logger.info("Loading course data from sample-courses.json...");

            List<CourseDocument> courses = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<CourseDocument>>() {}
            );

            logger.info("Successfully deserialized {} courses from JSON.", courses.size());
            logger.info("Populating suggest fields for autocompletion...");

            // Assuming CourseDocument has a buildSuggest() method
            courses.forEach(CourseDocument::buildSuggest);

            logger.info("Starting bulk-indexing of courses into Elasticsearch...");
            courseService.bulkIndex(courses);

            logger.info("Data loading complete. Successfully indexed {} courses.", courses.size());

        } catch (IOException e) {
            logger.error("Failed to load or index course data from 'sample-courses.json'. " +
                    "Please ensure the file exists in src/main/resources and is valid JSON.", e);
            throw e;
        }
    }
}