package com.example.undoschool_assignment_backend.controller;

import com.example.undoschool_assignment_backend.document.CourseDocument;
import com.example.undoschool_assignment_backend.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping("")
    public Map<String, Object> searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(defaultValue = "upcoming") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CourseDocument> result = courseService.searchCourses(q, minAge, maxAge, category, type, minPrice, maxPrice, startDate, sort, page, size);
        Map<String, Object> response = new HashMap<>();
        response.put("total", result.getTotalElements());
        response.put("courses", result.getContent());
        return response;
    }

    @GetMapping("/suggest")
    public List<String> suggest(@RequestParam String q) {
        return courseService.suggestTitles(q);
    }
} 