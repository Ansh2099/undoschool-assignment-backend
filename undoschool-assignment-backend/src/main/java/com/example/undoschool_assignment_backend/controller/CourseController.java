package com.example.undoschool_assignment_backend.controller;

import com.example.undoschool_assignment_backend.document.CourseDocument;
import com.example.undoschool_assignment_backend.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public Page<CourseDocument> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) ZonedDateTime startDate,
            @RequestParam(defaultValue = "upcoming") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return courseService.searchCourses(q, minAge, maxAge, category, type, minPrice, maxPrice, startDate, sort, page, size);
    }

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam String q) {
        return courseService.suggestTitles(q);
    }
}