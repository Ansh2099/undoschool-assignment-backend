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
    private final ObjectMapper objectMapper; // Injected by Spring for proper configuration

    /**
     * This method is triggered once the application is ready. It reads course data
     * from a JSON file, prepares it for indexing, and then bulk-indexes it.
     *
     * @throws IOException if the sample data file cannot be read.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadData() throws IOException {
        // Use try-with-resources to ensure the InputStream is closed automatically
        try (InputStream inputStream = new ClassPathResource("sample-courses.json").getInputStream()) {
            logger.info("Loading course data from sample-courses.json...");

            // 1. Read the JSON file and map it to a list of CourseDocument objects
            List<CourseDocument> courses = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<CourseDocument>>() {}
            );

            logger.info("Successfully deserialized {} courses from JSON.", courses.size());

            // 2. CRITICAL STEP: Populate the 'suggest' field for each course document.
            // This must be done before indexing for the autocomplete feature to work.
            logger.info("Populating suggest fields for autocompletion...");
            courses.forEach(CourseDocument::buildSuggest);

            // 3. Bulk-index the prepared list of documents into Elasticsearch
            logger.info("Starting bulk-indexing of courses into Elasticsearch...");
            courseService.bulkIndex(courses);

            logger.info("Data loading complete. Successfully indexed {} courses.", courses.size());

        } catch (IOException e) {
            logger.error("Failed to load or index course data from 'sample-courses.json'. " +
                    "Please ensure the file exists in src/main/resources and is valid JSON.", e);
            // Re-throwing the exception will cause the application startup to fail,
            // which is often desirable if essential data cannot be loaded.
            throw e;
        }
    }
}