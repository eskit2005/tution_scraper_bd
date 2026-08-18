package com.tuition.app.repository;

import com.tuition.app.entity.TuitionPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TuitionPostRepository extends JpaRepository<TuitionPost, String>, JpaSpecificationExecutor<TuitionPost> {

    @Query("SELECT DISTINCT t.facebookPostId FROM TuitionPost t WHERE t.scrapedAt >= :since AND t.facebookPostId IS NOT NULL")
    List<String> findFacebookPostIdsScrapedSince(@Param("since") LocalDateTime since);

    @Query("SELECT DISTINCT t.facebookPostId FROM TuitionPost t WHERE t.facebookPostId IS NOT NULL")
    List<String> findAllDistinctFacebookPostIds();
}
