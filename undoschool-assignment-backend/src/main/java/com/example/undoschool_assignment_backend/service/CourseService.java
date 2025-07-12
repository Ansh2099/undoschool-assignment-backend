package com.example.undoschool_assignment_backend.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.json.JsonData;
import com.example.undoschool_assignment_backend.document.CourseDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private static final String INDEX = "courses";
    private final ElasticsearchClient elasticsearchClient;

    public void bulkIndex(List<CourseDocument> courses) {
        try {
            BulkRequest.Builder br = new BulkRequest.Builder();
            for (CourseDocument course : courses) {
                br.operations(op -> op
                        .index(idx -> idx
                                .index(INDEX)
                                .id(course.getId())
                                .document(course)
                        )
                );
            }

            br.refresh(co.elastic.clients.elasticsearch._types.Refresh.True);

            elasticsearchClient.bulk(br.build());
            log.info("Successfully indexed {} courses.", courses.size());
        } catch (IOException e) {
            log.error("Bulk indexing failed", e);
        }
    }

    public Page<CourseDocument> searchCourses(
            String q,
            Integer minAge, Integer maxAge,
            String category, String type,
            Double minPrice, Double maxPrice,
            ZonedDateTime startDate,
            String sort,
            int page, int size
    ) {
        try {
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            if (q != null && !q.isBlank()) {
                boolQuery.must(MultiMatchQuery.of(m -> m
                        .query(q)
                        .fields("title^2", "description")
                        .fuzziness("AUTO")
                )._toQuery());
            } else {
                boolQuery.must(MatchAllQuery.of(m -> m)._toQuery());
            }

            if (category != null && !category.isBlank()) {
                boolQuery.filter(TermQuery.of(t -> t
                        .field("category.keyword")
                        .value(category)
                )._toQuery());
            }

            if (type != null && !type.isBlank()) {
                boolQuery.filter(TermQuery.of(t -> t
                        .field("type.keyword")
                        .value(type)
                )._toQuery());
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

            final String sortField;
            final SortOrder sortOrder;

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

            SearchRequest request = SearchRequest.of(s -> s
                    .index(INDEX)
                    .from(page * size)
                    .size(size)
                    .query(boolQuery.build()._toQuery())
                    .sort(sb -> sb.field(f -> f
                            .field(sortField)
                            .order(sortOrder)
                    ))
            );

            SearchResponse<CourseDocument> response =
                    elasticsearchClient.search(request, CourseDocument.class);

            List<CourseDocument> hits = new ArrayList<>();
            for (Hit<CourseDocument> hit : response.hits().hits()) {
                hits.add(hit.source());
            }

            long total = response.hits().total() != null ? response.hits().total().value() : 0;
            return new PageImpl<>(hits, PageRequest.of(page, size), total);

        } catch (IOException e) {
            log.error("Search failed", e);
            return Page.empty();
        }
    }

    public List<String> suggestTitles(String partialTitle) {
        if (partialTitle == null || partialTitle.isBlank()) {
            return Collections.emptyList();
        }
        try {
            SearchRequest req = SearchRequest.of(s -> s
                    .index(INDEX)
                    .suggest(sg -> sg
                            .suggesters("course-suggest", sug -> sug
                                    .prefix(partialTitle)
                                    .completion(c -> c
                                            .field("suggest")
                                            .skipDuplicates(true)
                                            .size(10)
                                    )
                            )
                    )
            );

            SearchResponse<CourseDocument> resp =
                    elasticsearchClient.search(req, CourseDocument.class);

            List<Suggestion<CourseDocument>> suggestions =
                    resp.suggest().get("course-suggest");

            log.info("Response from ES: {}", resp);


            return suggestions.stream()
                    .flatMap(s -> s.completion().options().stream())
                    .map(CompletionSuggestOption::text)
                    .toList();


        } catch (IOException e) {
            log.error("Autocomplete suggestion failed", e);
            return Collections.emptyList();
        }
    }
}