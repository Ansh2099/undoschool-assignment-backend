package com.example.undoschool_assignment_backend.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchInitializer {

    private final ElasticsearchClient client;

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexIfNotExists() {
        try {
            boolean exists = client.indices().exists(e -> e.index("courses")).value();

            if (!exists) {
                client.indices().create(c -> c
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
                                .properties("suggest", p -> p.completion(cmp -> cmp))
                        )
                );
                log.info("Created Elasticsearch index 'courses'");
            }
        } catch (IOException e) {
            log.error("Index creation failed", e);
        }
    }
}