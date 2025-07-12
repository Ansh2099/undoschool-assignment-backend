package com.example.undoschool_assignment_backend.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDocument {
    private String id;
    private String title;
    private String description;
    private String category;
    private String type;
    private String gradeRange;
    private Integer minAge;
    private Integer maxAge;
    private Double price;
    private ZonedDateTime nextSessionDate;

    @JsonProperty("suggest")
    private SuggestField suggest;

    public void buildSuggest() {
        if (title != null && !title.isBlank()) {
            this.suggest = new SuggestField(List.of(title));
        }
    }
}