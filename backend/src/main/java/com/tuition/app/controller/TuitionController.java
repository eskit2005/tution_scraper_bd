package com.tuition.app.controller;

import com.tuition.app.dto.TuitionPostDto;
import com.tuition.app.service.TuitionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tuitions")
public class TuitionController {

    private final TuitionService tuitionService;

    public TuitionController(TuitionService tuitionService) {
        this.tuitionService = tuitionService;
    }

    @GetMapping
    public ResponseEntity<List<TuitionPostDto>> getAllTuitions(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String salary,
            @RequestParam(required = false) String classLevel,
            @RequestParam(required = false) String subject
    ) {
        return ResponseEntity.ok(tuitionService.getAllTuitions(location, salary, classLevel, subject));
    }

    @GetMapping("/existing-post-ids")
    public ResponseEntity<List<String>> getExistingPostIds(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(tuitionService.getRecentFacebookPostIds(days));
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestTuitions(@RequestBody List<TuitionPostDto> posts) {
        return ResponseEntity.ok(tuitionService.ingestTuitions(posts));
    }
}
