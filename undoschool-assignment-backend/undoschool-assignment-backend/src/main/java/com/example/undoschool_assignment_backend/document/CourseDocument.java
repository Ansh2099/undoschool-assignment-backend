package com.example.undoschool_assignment_backend.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    private List<String> suggest;

    public void buildSuggest() {
        if (title != null && !title.isBlank()) {
            this.suggest = Collections.singletonList(title);
        }
    }
}
