package com.example.undoschool_assignment_backend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.undoschool_assignment_backend.document.CourseDocument;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import co.elastic.clients.json.JsonData;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);
    private static final String INDEX = "course_document"; // ES index name (change if needed)

    private final ElasticsearchClient elasticsearchClient;

    public void bulkIndex(List<CourseDocument> courses) {
        try {
            List<BulkOperation> operations = courses.stream()
                    .map(course -> BulkOperation.of(op -> op
                            .index(idx -> idx
                                    .index(INDEX)
                                    .id(course.getId())  // Ensure ID is set
                                    .document(course)
                            )
                    ))
                    .toList();

            elasticsearchClient.bulk(b -> b.index(INDEX).operations(operations));
            logger.info("Successfully indexed {} courses.", courses.size());
        } catch (IOException e) {
            logger.error("Bulk index failed", e);
        }
    }


    public Page<CourseDocument> searchCourses(
            String q, Integer minAge, Integer maxAge, String category, String type,
            Double minPrice, Double maxPrice, ZonedDateTime startDate,
            String sort, int page, int size
    ) {
        try {
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            // Text search
            if (q != null && !q.isBlank()) {
                boolQuery.must(MultiMatchQuery.of(m -> m
                        .query(q)
                        .fields("title^2", "description")
                        .fuzziness("AUTO")
                )._toQuery());
            } else {
                boolQuery.must(MatchAllQuery.of(m -> m)._toQuery());
            }

            // Filters
            if (category != null && !category.isBlank()) {
                boolQuery.filter(TermQuery.of(t -> t.field("category.keyword").value(category))._toQuery());
            }

            if (type != null && !type.isBlank()) {
                boolQuery.filter(TermQuery.of(t -> t.field("type.keyword").value(type))._toQuery());
            }

            if (startDate != null) {
                boolQuery.filter(RangeQuery.of(r -> r
                        .field("nextSessionDate")
                        .gte(JsonData.of(startDate.toString()))
                )._toQuery());
            }

            if (minPrice != null) {
                boolQuery.filter(RangeQuery.of(r -> r
                        .field("price")
                        .gte(JsonData.of(minPrice))
                )._toQuery());
            }

            if (maxPrice != null) {
                boolQuery.filter(RangeQuery.of(r -> r
                        .field("price")
                        .lte(JsonData.of(maxPrice))
                )._toQuery());
            }

            if (minAge != null) {
                boolQuery.filter(RangeQuery.of(r -> r
                        .field("maxAge")
                        .gte(JsonData.of(minAge))
                )._toQuery());
            }

            if (maxAge != null) {
                boolQuery.filter(RangeQuery.of(r -> r
                        .field("minAge")
                        .lte(JsonData.of(maxAge))
                )._toQuery());
            }

            // Sorting
            String sortField;
            SortOrder sortOrder;
            if ("priceAsc".equalsIgnoreCase(sort)) {
                sortField = "price";
                sortOrder = SortOrder.Asc;
            } else if ("priceDesc".equalsIgnoreCase(sort)) {
                sortField = "price";
                sortOrder = SortOrder.Desc;
            } else {
                sortField = "nextSessionDate";
                sortOrder = SortOrder.Asc;
            }

            // Execute search
            SearchRequest request = SearchRequest.of(s -> s
                    .index(INDEX)
                    .from(page * size)
                    .size(size)
                    .query(boolQuery.build()._toQuery())
                    .sort(sortBuilder -> sortBuilder
                            .field(f -> f
                                    .field(sortField)
                                    .order(sortOrder)
                            )
                    )
            );

            SearchResponse<CourseDocument> response = elasticsearchClient.search(request, CourseDocument.class);

            List<CourseDocument> results = new ArrayList<>();
            for (Hit<CourseDocument> hit : response.hits().hits()) {
                results.add(hit.source());
            }

            assert response.hits().total() != null;
            return new PageImpl<>(results, PageRequest.of(page, size), response.hits().total().value());

        } catch (IOException e) {
            logger.error("Search failed", e);
            return Page.empty();
        }
    }

    public List<String> suggestTitles(String partialTitle) {
        if (partialTitle == null || partialTitle.isBlank()) {
            return Collections.emptyList();
        }

        try {
            PrefixQuery prefixQuery = PrefixQuery.of(p -> p
                    .field("title.keyword")
                    .value(partialTitle.toLowerCase())
            );

            SearchRequest request = SearchRequest.of(s -> s
                    .index(INDEX)
                    .query(prefixQuery._toQuery())
                    .size(10)
                    .source(src -> src
                            .filter(f -> f
                                    .includes("title")
                            )
                    )
            );

            SearchResponse<CourseDocument> response = elasticsearchClient.search(request, CourseDocument.class);

            return response.hits().hits().stream()
                    .map(Hit::source).filter(Objects::nonNull)
                    .map(CourseDocument::getTitle)
                    .filter(title -> title != null && !title.isBlank())
                    .distinct()
                    .toList();

        } catch (IOException e) {
            logger.error("Suggestion failed for: {}", partialTitle, e);
            return Collections.emptyList();
        }
    }
}
