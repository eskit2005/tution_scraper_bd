package com.tuition.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TuitionPostDto {
    private String compositeKey;
    private String facebookPostId;
    private String postUrl;
    private String pageName;
    private String classLevel;
    private String subject;
    private String location;
    private String salary;
    private String genderPreference;
    private String description;
    private LocalDateTime scrapedAt;
}
