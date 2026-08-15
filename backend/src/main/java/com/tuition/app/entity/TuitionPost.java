package com.tuition.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tuition_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TuitionPost {

    @Id
    private String compositeKey; 

    private String facebookPostId;
    
    @Column(length = 1000)
    private String postUrl;
    
    private String pageName;
    
    private String classLevel;
    private String subject;
    private String location;
    private String salary;
    private String genderPreference; 
    
    @Column(length = 2000)
    private String description;
    
    private String postedAt;
    
    @Column(updatable = false)
    private LocalDateTime scrapedAt;

    @PrePersist
    protected void onCreate() {
        this.scrapedAt = LocalDateTime.now();
    }
}
