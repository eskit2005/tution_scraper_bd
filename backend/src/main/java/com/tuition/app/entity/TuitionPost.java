package com.tuition.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
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
    
    private LocalDateTime publishedAt;
    
    @Formula("""
        CASE
            WHEN EXTRACT(EPOCH FROM (NOW() - COALESCE(published_at, scraped_at))) < 60 THEN 'Just now'
            WHEN EXTRACT(EPOCH FROM (NOW() - COALESCE(published_at, scraped_at))) < 3600 THEN FLOOR(EXTRACT(EPOCH FROM (NOW() - COALESCE(published_at, scraped_at))) / 60)::TEXT || 'm'
            WHEN EXTRACT(EPOCH FROM (NOW() - COALESCE(published_at, scraped_at))) < 86400 THEN FLOOR(EXTRACT(EPOCH FROM (NOW() - COALESCE(published_at, scraped_at))) / 3600)::TEXT || 'h'
            ELSE FLOOR(EXTRACT(EPOCH FROM (NOW() - COALESCE(published_at, scraped_at))) / 86400)::TEXT || 'd'
        END
    """)
    private String timeElapsed;
    
    @Column(updatable = false)
    private LocalDateTime scrapedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (this.scrapedAt == null) {
            this.scrapedAt = LocalDateTime.now();
        }
        if (this.publishedAt == null) {
            this.publishedAt = this.scrapedAt;
        }
    }
}
